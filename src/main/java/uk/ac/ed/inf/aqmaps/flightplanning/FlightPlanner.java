package uk.ac.ed.inf.aqmaps.flightplanning;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Handles the creation of a flight plan for the drone. Uses JGraphT's TwoOptHeuristicTSP algorithm
 * as part of process, which was the best performing of JGraphT's Hamiltonian Cycle algorithms,
 * however this could be changed easily.
 */
public class FlightPlanner {
  /**
   * The number of initial tours to try when running 2-opt. Increasing this does not necessarily
   * reduce average path length, and may increase it, since it will introduce less variability in
   * what goes into the flight-plan mode 2-opt. We do not want the tours given to the improver to
   * all be similarly optimal, since the two tour weight measures are different. There is a trade
   * off to be had between this constant and the number of times the algorithm runs
   */
  private static final int TWO_OPT_PASSES = 5;
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
  /** See {@link #cutCorner(Coords, Coords, Coords)} This value performed the best in testing. */
  private static final double CORNER_CUT_RADIUS_FRACTION = 0.634;
  /**
   * The approximate maximum run time for flight planning in seconds. The algorithm will repeat as
   * many times as possible with different random seeds within this time frame, up to a maximum of
   * {@value MAX_ITERATIONS} and then the best flight path will be chosen.
   */
  private static final double DEFAULT_TIME_LIMIT_SECONDS = 0.5;

  private final Obstacles obstacles;

  /** A map from coordinates to the W3W of a sensor at that location. */
  private final Map<Coords, W3W> sensorCoordsW3WMap;

  /**
   * Caches the number of moves and end position of navigating from a point to a target, with a
   * particular following target. Used in {@link #computeFlightLength}. This does not cache the
   * actual moves that would be needed to use the cache in {@link #constructFlightAlongTour} since
   * the memory use would be too high and almost all of the moves stored would not be used since we
   * only need to construct the tour once. Using a cache in testing resulted in a speedup of 60-70%.
   * Since {@link #computeFlightLength} will be run in parallel, we use a ConcurrentHashMap to
   * prevent blocking, which was about 60% faster than a Hashtable in testing (on 6 cores/12
   * threads).
   */
  private final Map<FlightCacheKey, FlightCacheValue> cache = new ConcurrentHashMap<>();

  /**
   * Holds the next random seed to be used when creating the next plan. This is used so when the
   * time limit is being used to cut off execution, it will have run using the first seeds in the
   * planned sequence (seed, seed+1, seed+2, ...). The alternative is to create this sequence in
   * advance and then run through it with map(), however when executed in parallel they would not be
   * executed starting from the front, and the seeds that were chosen would end up being essentially
   * random.
   *
   * <p>Using this allows to say with confidence that if the algorithm produced a flight plan in n
   * iterations, that flight plan used a random seed between the command line input seed and
   * (seed+n), and you would be able to generate the same flight plan again.
   */
  private final AtomicInteger atomicSeedCounter;

  /**
   * The number of times that the flight planning algorithm is ran with a different seed. Collected
   * for logging purposes.
   */
  private final AtomicInteger iterationCount = new AtomicInteger(0);

  /**
   * The time limit can be turned off to run for a specified {@link #ITERATIONS} number of
   * iterations to produce consistent output.
   */
  private final boolean timeLimitOn;

  /**
   * The approximate maximum run time for flight planning in nanoseconds (to work with
   * System.nanoTime()). The algorithm will repeat as many times as possible with different random
   * seeds within this time frame, up to a maximum of {@value MAX_ITERATIONS} and then the best
   * flight path will be chosen.
   */
  private long timeLimitNanos;

  /** The System.nanoTime() at which we started running flight planning algorithms. */
  private double startTime;

  /**
   * Construct a flight planner with the given time limit in seconds. If the time limit is not
   * greater than 0, turns it off and uses a maximum number of iterations instead.
   *
   * @param obstacles the Obstacles containing the no-fly zones
   * @param sensorW3Ws the W3W locations of the sensors
   * @param randomSeed the initial random seed to use
   * @param timeLimit the time limit for the algorithm in seconds. If -1 uses the default of {@link
   *     #DEFAULT_TIME_LIMIT_SECONDS}, and if 0 then disables the time limit and runs for a fixed
   *     number of iterations.
   */
  public FlightPlanner(
      Obstacles obstacles, List<W3W> sensorW3Ws, int randomSeed, double timeLimit) {
    this.obstacles = obstacles;
    // Prepare the map from sensor coords to their W3Ws
    sensorCoordsW3WMap = new HashMap<>();
    sensorW3Ws.forEach(w3w -> sensorCoordsW3WMap.put(w3w.getCoordinates(), w3w));
    // Set the first random seed to the user-provided seed in the settings
    this.atomicSeedCounter = new AtomicInteger(randomSeed);

    if (timeLimit == 0) {
      // Run with no time limit
      timeLimitOn = false;
    } else if (timeLimit == -1) {
      // Run with default time limit
      timeLimitOn = true;
      timeLimitNanos = (long) (DEFAULT_TIME_LIMIT_SECONDS * 1e9);
    } else {
      timeLimitNanos = (long) (timeLimit * 1e9);
      timeLimitOn = true;
    }
  }

  /**
   * Create a flight plan for the drone which visits all sensors and returns to the start. Runs the
   * algorithm a large number of times with different random seeds, in parallel, and chooses the
   * shortest.
   *
   * @param startPosition the starting position of the drone
   * @return a list of Moves representing the flight plan
   */
  public List<Move> createBestFlightPlan(Coords startPosition) {
    startTime = System.nanoTime();
    var sensorGraph =
        SensorGraph.createWithStartLocation(startPosition, sensorCoordsW3WMap.keySet(), obstacles);

    // Run flight planning either ITERATIONS or MAX_ITERATIONS times in a parallel stream
    // Note that if time limit is on all results after the time has ended will be null
    var bestFlightPlan =
        IntStream.range(0, timeLimitOn ? MAX_ITERATIONS : ITERATIONS)
            .parallel() // Run in parallel to decrease run time
            .mapToObj(i -> createPlan(startPosition, sensorGraph))
            .filter(Objects::nonNull) // Once max runtime has elapsed they will be null so filter
            .min(Comparator.comparing(List::size)) // Get the tour with the minimal number of moves
            .orElse(null);
    System.out.printf("Number of flight planning iterations completed: %d%n", iterationCount.get());
    if (bestFlightPlan == null) {
      System.out.println("Error: valid flight plan could not be found");
      System.exit(1);
    }
    return bestFlightPlan;
  }

  /**
   * Creates a flight plan for the drone which visits all sensors and returns to the start. First
   * uses 2-opt heuristic to generate a tour, then constructs a flight plan for the drone along the
   * route.
   *
   * @param startPosition the starting position of the drone
   * @param sensorGraph the graph containing all of the sensors and distances
   * @return a list of Moves representing the flight plan
   */
  private List<Move> createPlan(
      Coords startPosition, SensorGraph sensorGraph) {
    if (timeLimitOn && (System.nanoTime() - startTime) > timeLimitNanos) {
      // If more than the max runtime has elapsed, stop the algorithm by returning null
      return null;
    }
    iterationCount.incrementAndGet(); // Increment the iteration counter (for logging)

    var seed = atomicSeedCounter.getAndIncrement(); // Get the next random seed

    // Get a short tour which visits
    var twoOpt = new EnhancedTwoOptTSP(TWO_OPT_PASSES, seed, startPosition, this);
    var graphPath = twoOpt.getTour(sensorGraph);

    var tour = graphPath.getVertexList();

    tour.remove(0); // The first and last elements are duplicates, so remove the duplicate

    // Rotate the list backwards so the starting position is at the front
    Collections.rotate(tour, -tour.indexOf(startPosition));
    tour.add(tour.get(0)); // Put the starting position as the ending position as well
    return constructFlightAlongTour(tour);
  }

  /**
   * Computes the length of a flight plan which follows the given sensor coordinate tour.
   *
   * @param tour a list of Coords specifying the order to visit the sensors
   * @return the number of moves in the flight plan
   */
  public int computeFlightLength(List<Coords> tour) {
    var obstacleEvader = obstacles.getObstacleEvader();
    var length = 0;
    var currentPosition = tour.get(0);

    // Plan the flight from each sensor to the next
    for (int i = 1; i < tour.size(); i++) {
      var currentTarget = tour.get(i);

      // Check the cache to see if we have already made a similar computation, and if so use the
      // cached output instead. If there is no next target, use null in the keu
      var cacheKey =
          new FlightCacheKey(
              currentPosition, currentTarget, i < tour.size() - 1 ? tour.get(i + 1) : null);
      if (cache.containsKey(cacheKey)) {
        var cacheValue = cache.get(cacheKey);
        length += cacheValue.getLength();
        currentPosition = cacheValue.getEndPosition();
        continue;
      }
      W3W targetSensorOrNull = sensorCoordsW3WMap.get(currentTarget);

      // If the target is not the end, shorten the route by using the sensor range to cut the corner
      if (i < tour.size() - 1) {
        var nextTarget = tour.get(i + 1);
        currentTarget = cutCorner(currentPosition, currentTarget, nextTarget);
      }
      // Compute a list of waypoints from the current position to the target, avoiding obstacles
      var waypoints = obstacleEvader.getPathBetweenPoints(currentPosition, currentTarget);

      // Compute a list of Moves from the current position to the target
      var waypointNavigation = new WaypointNavigation(obstacles);
      var movesToTarget =
          waypointNavigation.navigateToLocation(currentPosition, waypoints, targetSensorOrNull);

      if (movesToTarget == null) {
        // In the exceptional case that there is no valid flightpath, we give up here
        // This never happened in testing
        return Integer.MAX_VALUE;
      }
      // Update the current position to the end of the sequence of moves
      var newPosition = movesToTarget.get(movesToTarget.size() - 1).getAfter();
      currentPosition = newPosition;

      length += movesToTarget.size();

      cache.put(cacheKey, new FlightCacheValue(movesToTarget.size(), newPosition));
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
      var waypoints = obstacleEvader.getPathBetweenPoints(currentPosition, currentTarget);

      // Compute a list of Moves from the current position to the target
      var waypointNavigation = new WaypointNavigation(obstacles);
      var movesToTarget =
          waypointNavigation.navigateToLocation(currentPosition, waypoints, targetSensorOrNull);

      if (movesToTarget == null) {
        // In the exceptional case that there is no valid flightpath, we give up here
        // This never happened in testing
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
   *
   * @param currPos the position of the drone before it will move towards the target
   * @param target the location of the target
   * @param nextTarget the location of the target after the current target
   * @return a new target, which has potentially been moved in order to cut the corner
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
