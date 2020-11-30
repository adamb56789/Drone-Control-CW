package uk.ac.ed.inf.aqmaps.flightplanning;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Handles the creation of a flight plan for the drone. Uses JGraphT's TwoOptHeuristicTSP algorithm
 * as part of process, which was the best performing of JGraphT's Hamiltonian Cycle algorithms,
 * however this could be changed easily.
 */
public class FlightPlanner {
  /**
   * The approximate maximum run time for flight planning. The algorithm will repeat as many times
   * as possible with different random seeds within this time frame, up to a maximum of {@value
   * MAX_ITERATIONS} and then the best flight path will be chosen.
   */
  private static final int MAX_RUNTIME_MILLIS = 500;
  /**
   * The time limit can be turned off to run for a specified {@link #ITERATIONS} number of
   * iterations to produce consistent output.
   */
  private static final boolean TIME_LIMIT_ON = true;
  /**
   * When the time limit is off, this is the number of times to run the algorithm before picking the
   * shortest. Can be changed to trade off for speed and efficacy. Increasing it more has almost no
   * effect.
   */
  private static final int ITERATIONS = 120;
  /**
   * When the time limit is on, this is the maximum possible number of iterations. If this many are
   * run before the time is up, it stops and does not wait for the timer. The current value is high
   * enough for this to never happen.
   */
  private static final int MAX_ITERATIONS = 50000;
  /**
   * The number of initial tours to try when running 2-opt. Unless {@link #ITERATIONS} is low,
   * increasing this reduces the average move length.
   */
  private static final int TWO_OPT_PASSES = 5;

  /** See {@link #cutCorner(Coords, Coords, Coords)} This value performed the best in testing. */
  private static final double CORNER_CUT_RADIUS_FRACTION = 0.634;
  /**
   * Increasing the value of this constant reduces run time but slightly increases average path
   * length. See {@link #cutCorner(Coords, Coords, Coords)}
   */
  private final Obstacles obstacles;

  private final Map<Coords, W3W> sensorCoordsW3WMap;
  /**
   * Note: we need the sensor Coords as a list instead of using keySet() on the map since the set
   * has an undetermined ordering (specifically, not determined by the random seed), which changes
   * the initial random tours generated by the 2-opt algorithm and produces different results with
   * the same seed.
   */
  private final List<Coords> sensorCoords;

  private final long randomSeed;
  /**
   * Caching the results of {@link #computeLengthOfFlight} resulted in a speedup of 60-70%. Using a
   * ConcurrentHashMap was about 60% faster than a Hashtable when using parallelStream() (on 6
   * cores/12 threads).
   */
  private final Map<FlightCacheKeys, FlightCacheValues> cache = new ConcurrentHashMap<>();

  private int misses = 0;
  private int hits = 0;
  private int iterationCount = 0;
  private double startTime;

  /**
   * Constructor
   *
   * @param obstacles the Obstacles containing the no-fly zones
   * @param sensorW3Ws the W3W locations of the sensors
   * @param randomSeed the random seed to use for the graph algorithm
   */
  public FlightPlanner(Obstacles obstacles, List<W3W> sensorW3Ws, long randomSeed) {
    this.obstacles = obstacles;
    sensorCoordsW3WMap = new HashMap<>();
    sensorW3Ws.forEach(w3w -> sensorCoordsW3WMap.put(w3w.getCoordinates(), w3w));
    sensorCoords = sensorW3Ws.stream().map(W3W::getCoordinates).collect(Collectors.toList());
    this.randomSeed = randomSeed;
  }

  /**
   * Create a flight plan for the drone which visits all sensors and returns to the start. Runs the
   * algorithm a large number of times with different random seeds, in parallel, and chooses the
   * shortest.
   *
   * @param startPosition the starting position of the drone
   * @return a list of Moves representing the flight plan
   */
  public List<Move> createFlightPlan(Coords startPosition) {
    startTime = System.nanoTime();
    var sensorGraph = createSensorGraph(startPosition, sensorCoords);

    // Generate a list of seeds [randomSeed, randomSeed + 1, ..., randomSeed + ITERATIONS - 1]
    // See the description of the constants MAX_ITERATIONS and ITERATIONS
    var seeds =
        LongStream.range(randomSeed, randomSeed + (TIME_LIMIT_ON ? MAX_ITERATIONS : ITERATIONS))
            .boxed()
            .collect(Collectors.toList());

    var flightPlan =
        seeds.parallelStream() // Run in parallel to decrease run time
            .map(seed -> createPlanWithSeed(startPosition, sensorGraph, seed))
            .filter(Objects::nonNull) // Once the max runtime has elapsed they will be null
            .min(Comparator.comparing(List::size)) // Get the tour with the minimal number of moves
            .orElse(null);
    System.out.printf("Cache hits=%d, misses=%d. Iterations = %d%n", hits, misses, iterationCount);
    return flightPlan;
  }

  /**
   * Creates a flight plan for the drone which visits all sensors and returns to the start. First
   * uses 2-opt heuristic to generate a tour, then constructs a flight plan for the drone along the
   * route.
   *
   * @param startPosition the starting position of the drone
   * @param sensorGraph the graph containing all of the sensors and distances
   * @param seed the random seed
   * @return a list of Moves representing the flight plan
   */
  private List<Move> createPlanWithSeed(
      Coords startPosition,
      SimpleWeightedGraph<Coords, DefaultWeightedEdge> sensorGraph,
      long seed) {
    if (TIME_LIMIT_ON && (System.nanoTime() - startTime) / 1000000 > MAX_RUNTIME_MILLIS) {
      // If more than the max runtime has elapsed, stop the algorithm by returning null
      return null;
    }
    iterationCount++;

    var twoOpt = new TwoOptFlightPlanImprover(TWO_OPT_PASSES, seed, startPosition, this);
    var graphPath = twoOpt.getTour(sensorGraph);

    var tour = graphPath.getVertexList();

    tour.remove(0); // The first and last elements are duplicates, so remove the duplicate

    // Rotate the list backwards so the starting position is at the front
    Collections.rotate(tour, -tour.indexOf(startPosition));
    tour.add(tour.get(0)); // Put the starting position as the ending position as well
    return constructFlightAlongTour(tour);
  }

  /**
   * Creates a complete weighted graph with the points of all of the sensors and the starting
   * position. The edge weights are the shortest distance between the points, avoiding obstacles if
   * necessary.
   *
   * @return a complete SimpleWeightedGraph of sensors and the starting position
   */
  private SimpleWeightedGraph<Coords, DefaultWeightedEdge> createSensorGraph(
      Coords startPosition, Collection<Coords> sensorCoords) {
    var obstacleEvader = obstacles.getObstacleEvader();
    var graph = new SimpleWeightedGraph<Coords, DefaultWeightedEdge>(DefaultWeightedEdge.class);

    for (var coords : sensorCoords) {
      graph.addVertex(coords);
    }
    graph.addVertex(startPosition);

    // Create edges between all pairs of vertices
    var vertexList = new ArrayList<>(graph.vertexSet());
    for (int i = 0; i < vertexList.size() - 1; i++) {
      for (int j = i + 1; j < vertexList.size(); j++) {
        var edge = graph.addEdge(vertexList.get(i), vertexList.get(j));
        graph.setEdgeWeight(
            edge, obstacleEvader.getPathLength(vertexList.get(i), vertexList.get(j)));
      }
    }
    return graph;
  }

  /**
   * Computes the length of a flight plan which follows the given sensor coordinate tour.
   *
   * @param tour a list of Coords specifying the order to visit the sensors
   * @return the number of moves in the flight plan
   */
  public int computeLengthOfFlight(List<Coords> tour) {
    var obstacleEvader = obstacles.getObstacleEvader();
    var length = 0;
    var currentPosition = tour.get(0);

    // Plan the flight from each sensor to the next
    for (int i = 1; i < tour.size(); i++) {
      var currentTarget = tour.get(i);

      // Check the cache to see if we have already made a similar computation, and if so use the
      // cached output instead.
      var cacheEntry =
          new FlightCacheKeys(
              currentPosition, currentTarget, i < tour.size() - 1 ? tour.get(i + 1) : null);
      if (cache.containsKey(cacheEntry)) {
        hits++;
        var cacheValue = cache.get(cacheEntry);
        length += cacheValue.getLength();
        currentPosition = cacheValue.getEndPosition();
        continue;
      }
      misses++;
      W3W targetSensorOrNull = sensorCoordsW3WMap.get(currentTarget);

      // If the target is not the end, shorten the route by using the sensor range to cut the corner
      if (i < tour.size() - 1) {
        var nextTarget = tour.get(i + 1);
        currentTarget = cutCorner(currentPosition, currentTarget, nextTarget);
      }
      // Compute a list of waypoints from the current position to the target, avoiding obstacles
      var waypoints = obstacleEvader.getPath(currentPosition, currentTarget);

      // Compute a list of Moves from the current position to the target
      var waypointNavigation = new WaypointNavigation(obstacles);
      var movesToTarget =
          waypointNavigation.navigateToLocation(currentPosition, waypoints, targetSensorOrNull);

      if (movesToTarget == null) {
        // In case there is no valid flightpath, we give up here
        System.out.println("Gave up searching for path"); // TODO
        return Integer.MAX_VALUE;
      }
      // Update the current position to the end of the sequence of moves
      var newPosition = movesToTarget.get(movesToTarget.size() - 1).getAfter();
      currentPosition = newPosition;

      length += movesToTarget.size();

      cache.put(cacheEntry, new FlightCacheValues(movesToTarget.size(), newPosition));
    }
    return length;
  }

  /**
   * Create a flight plan for the drone along the given sensor tour.
   *
   * @param tour a list of Coords specifying the order to visit the sensors
   * @return a list of Moves representing the flight plan
   */
  private List<Move> constructFlightAlongTour(List<Coords> tour) {
    var obstacleEvader = obstacles.getObstacleEvader();
    var moves = new ArrayList<Move>();
    var currentPosition = tour.get(0);

    // Plan the flight from each sensor to the next
    for (int i = 1; i < tour.size(); i++) {
      var currentTarget = tour.get(i);
      W3W targetSensorOrNull = sensorCoordsW3WMap.get(currentTarget);

      // If the target is not the end, shorten the route by using the sensor range to cut the corner
      if (i < tour.size() - 1) {
        var nextTarget = tour.get(i + 1);
        currentTarget = cutCorner(currentPosition, currentTarget, nextTarget);
      }
      // Compute a list of waypoints from the current position to the target, avoiding obstacles
      var waypoints = obstacleEvader.getPath(currentPosition, currentTarget);

      // Compute a list of Moves from the current position to the target
      var waypointNavigation = new WaypointNavigation(obstacles);
      var movesToTarget =
          waypointNavigation.navigateToLocation(currentPosition, waypoints, targetSensorOrNull);

      if (movesToTarget == null) {
        // In case there is no valid flightpath, we give up here
        System.out.println("Gave up searching for path"); // TODO
        return moves;
      }
      // Update the current position to the end of the sequence of moves
      currentPosition = movesToTarget.get(movesToTarget.size() - 1).getAfter();

      moves.addAll(movesToTarget);
    }
    return moves;
  }

  /**
   * Attempts to cut the corner by moving from the target a distance of {@value
   * WaypointNavigation#SENSOR_RANGE} * {@link #CORNER_CUT_RADIUS_FRACTION} in a direction between
   * the current position and the next target. It tries 3 directions: the bisector between the
   * directions, and the 2 recursive bisectors (3 equally spaced directions). Picks the target which
   * minimises the new path that goes through the new point, and does not collide with an obstacle.
   * If none of the new targets are an improvement, outputs the original target.
   */
  private Coords cutCorner(Coords currPos, Coords target, Coords nextTarget) {
    var newTargets = new ArrayList<Coords>();
    var newTarget = createPointOnBisector(target, nextTarget, currPos);
    newTargets.add(createPointOnBisector(target, newTarget, currPos));
    newTargets.add(createPointOnBisector(target, nextTarget, newTarget));

    // The distance to beat of the already existing target
    var minDistance = currPos.distance(target) + target.distance(nextTarget);
    for (var candidate : newTargets) {
      // It is not worth it if the new route now needs to avoid an obstacle. The move may also have
      // put the point inside an obstacle.
      if (!obstacles.lineCollision(currPos, newTarget)
          && !obstacles.lineCollision(candidate, nextTarget)) {

        // If the new path is shorter than the old path, replace the target with the new one
        var distance = currPos.distance(candidate) + candidate.distance(nextTarget);
        if (distance < minDistance) {
          target = candidate;
        }
      }
    }
    return target;
  }

  /**
   * Creates a point a distance of {@value WaypointNavigation#SENSOR_RANGE} * {@link
   * #CORNER_CUT_RADIUS_FRACTION} in the direction of the acute bisector between the lines PA and
   * PB.
   *
   * @param P point P
   * @param A point A
   * @param B point B
   * @return the new point
   */
  private Coords createPointOnBisector(Coords P, Coords A, Coords B) {
    return P.getPositionAfterMoveRadians(
        P.bisectorDirection(B, A), WaypointNavigation.SENSOR_RANGE * CORNER_CUT_RADIUS_FRACTION);
  }
}
