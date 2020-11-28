package uk.ac.ed.inf.aqmaps.pathfinding;

import org.jgrapht.alg.tour.TwoOptHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Angle;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.*;

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
  /**
   * The number of times to run the algorithm before picking the shortest. Can be changed to trade
   * off for speed and efficacy.
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
   * Create a flight plan for the drone which visits all sensors and returns to the start.
   *
   * @param startPosition the starting position of the drone
   * @return a list of Moves representing the flight plan
   */
  public List<Move> createFlightPlan(Coords startPosition) {
    var tour = createSensorTour(startPosition, sensorCoordsW3WMap.keySet());
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
   * Computes a tour which visits all sensors and returns to the starting point.
   *
   * @param startPosition a Coords containing the starting point of the tour
   * @param sensorCoords a collection of Coords with the locations of the sensors to visit
   * @return a list of Coords which specifies the order to visit the sensors, and starts and ends
   *     with the starting position
   */
  private List<Coords> createSensorTour(Coords startPosition, Collection<Coords> sensorCoords) {
    var sensorGraph = createSensorGraph(startPosition, sensorCoords);

    // Try several times and keep the best tour.
    List<Coords> shortestTour = null;
    int shortestLength = Integer.MAX_VALUE;
    for (int i = 0; i < ITERATIONS; i++) {

      var twoOpt =
          new TwoOptHeuristicTSP<Coords, DefaultWeightedEdge>(TWO_OPT_PASSES, randomSeed + i);
      var graphPath = twoOpt.getTour(sensorGraph);
      var tour = graphPath.getVertexList();

      tour.remove(0); // The first and last elements are duplicates, so remove the duplicate

      // Rotate the list backwards so the starting position is at the front
      Collections.rotate(tour, -tour.indexOf(startPosition));
      tour.add(tour.get(0)); // Put the starting position as the ending position as well

      int tourLength = constructFlightAlongTour(tour).size();
      if (tourLength < shortestLength) {
        shortestTour = tour;
        shortestLength = tourLength;
      }
    }
    return shortestTour;
  }

  /**
   * Create a flight plan for the drone along the given sensor tour.
   *
   * @param tour a list of Coords specifying the order to visit the sensors
   * @return a list of Moves representing the flight plan
   */
  private List<Move> constructFlightAlongTour(List<Coords> tour) {
    var moves = new ArrayList<Move>();
    var currentPosition = tour.get(0);

    for (int i = 1; i < tour.size(); i++) {
      var currentTarget = tour.get(i);

      // If the target is not a sensor this will be null
      W3W targetSensorOrNull = sensorCoordsW3WMap.get(currentTarget);

      // If we are on any but the last leg of the tour, attempt to optimize the target location to
      // cut the corner.
      if (i < tour.size() - 1) {
        var nextTarget = tour.get(i + 1);

        // Calculate the direction of the bisector from the target to the current position and
        // the target to the next target
        var bisector = Angle.bisectorDirection(currentTarget, currentPosition, nextTarget);

        // Move the target 0.5 * (sensor range) in that direction to cut the corner. 0.5 was chosen
        // as it performed the best in testing. 1.0 doesn't work because it will often miss the
        // sensor range by a small amount and then go in circles for a bit.
        var newTarget =
            currentTarget.getPositionAfterMoveRadians(
                bisector, WaypointNavigation.SENSOR_RANGE * 0.5);

        // Check the the new target is not inside an obstacle
        if (!obstacles.pointCollision(newTarget)) {
          // Update the target to the optimised one
          currentTarget = newTarget;
        }
      }
      // Compute a list of waypoints from the current position to the target
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
}
