package uk.ac.ed.inf.aqmaps.flightplanning;

import org.jgrapht.alg.tour.TwoOptHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.Testing;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Angle;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Handles the creation of a flight plan for the drone. Uses JGraphT's TwoOptHeuristicTSP algorithm
 * as part of process, which was the best performing of JGraphT's Hamiltonian Cycle algorithms,
 * however this could be changed easily.
 */
public class FlightPlanner {
  /**
   * The number of initial tours to try when running 2-opt. Unless ITERATIONS is low, increasing
   * this reduces the average move length.
   */
  public static final int TWO_OPT_PASSES = 1;

  public static final int CORNER_CUT_STEP = 100;
  /**
   * The number of times to run the algorithm before picking the shortest. Can be changed to trade
   * off for speed and efficacy. Increasing it more has almost no effect.
   */
  private static final int ITERATIONS = 1000;

  private final Obstacles obstacles;
  private final ObstacleEvader obstacleEvader;
  private final Map<Coords, W3W> sensorCoordsW3WMap;
  private final long randomSeed;

  /**
   * Constructor
   *
   * @param obstacles the Obstacles for collision detection
   * @param obstacleEvader the ObstacleEvader for finding paths between points around obstacles
   * @param sensorW3Ws the W3W locations of the sensors
   * @param randomSeed the random seed to use for the graph algorithm
   */
  public FlightPlanner(
      Obstacles obstacles, ObstacleEvader obstacleEvader, List<W3W> sensorW3Ws, long randomSeed) {
    this.obstacles = obstacles;
    this.obstacleEvader = obstacleEvader;
    sensorCoordsW3WMap = new HashMap<>();
    sensorW3Ws.forEach(w3w -> sensorCoordsW3WMap.put(w3w.getCoordinates(), w3w));
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
    var sensorGraph = createSensorGraph(startPosition, sensorCoordsW3WMap.keySet());

    // Generate a list of seeds [randomSeed, randomSeed + 1, ..., randomSeed + ITERATIONS - 1]
    var seeds =
        LongStream.range(randomSeed, randomSeed + ITERATIONS).boxed().collect(Collectors.toList());

    return seeds.parallelStream() // Run in parallel to decrease run time
        .map(seed -> createPlanWithSeed(startPosition, sensorGraph, seed))
        .min(Comparator.comparing(List::size)) // Get the tour with the minimal number of moves
        .orElse(null); // min() returns an Optional so get the value out
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
    var twoOpt = new TwoOptHeuristicTSP<Coords, DefaultWeightedEdge>(TWO_OPT_PASSES, seed);
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
   * Create a flight plan for the drone along the given sensor tour.
   *
   * @param tour a list of Coords specifying the order to visit the sensors
   * @return a list of Moves representing the flight plan
   */
  private List<Move> constructFlightAlongTour(List<Coords> tour) {
    var obstacleEv = obstacleEvader.getCopy();
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
      var waypoints = obstacleEv.getPath(currentPosition, currentTarget);

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
   * Let BA be the line from the current target to the current position, and AC be the line from the
   * current target to the next target.
   */
  private Coords cutCorner(Coords currentPosition, Coords currentTarget, Coords nextTarget) {

    var dirToPos = Angle.lineDirection(currentTarget, currentPosition);
    var dirToNext = Angle.lineDirection(currentTarget, nextTarget);
    var minAngle = Math.min(dirToPos, dirToNext);
    var maxAngle = Math.max(dirToPos, dirToNext);
    var startDir = maxAngle - minAngle > Math.PI ? maxAngle : minAngle;
    var endDir = maxAngle - minAngle > Math.PI ? minAngle + 2 * Math.PI : maxAngle;
    double minDistance = Double.POSITIVE_INFINITY;
    Coords bestTarget = currentTarget;
    for (double dir = startDir; dir < endDir; dir += (endDir - startDir) / CORNER_CUT_STEP) {
      var newTarget =
          currentTarget.getPositionAfterMoveRadians(
              dir,
              WaypointNavigation.SENSOR_RANGE
                  * Testing.a); // TODO tune this, explain bisection, direction -> bearing?

      // It is not worth it if the new route now needs to avoid an obstacle. The move may also have
      // put the point inside an obstacle.
      if (!obstacles.lineCollision(currentPosition, newTarget)
          && !obstacles.lineCollision(newTarget, nextTarget)) {

        var distance = currentPosition.distance(newTarget) + newTarget.distance(nextTarget);
        if (distance < minDistance) {
          minDistance = distance;
          bestTarget = newTarget;
        }
      }
    }
    currentTarget = bestTarget;
    return currentTarget;
  }
}
