package uk.ac.ed.inf.aqmaps.pathfinding;

import org.jgrapht.alg.tour.TwoOptHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds a weighted graph containing the sensor locations and the shortest paths between them.
 * Computes a short, though not optimal tour (travelling salesman problem). Uses JGraphT's
 * TwoOptHeuristicTSP algorithm, which was the best performing of JGraphT's Hamiltonian Cycle
 * algorithms, however this could be changed easily.
 */
public class SensorGraph {

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

  private final ObstacleEvader obstacleEvader;
  private final long randomSeed;
  private final FlightPlanner flightPlanner;

  /**
   * Initialise the graph using a list of sensor locations and an obstacle graph for pathfinding.
   * Computes the shortest distance between every pair of points and adds them as an edge.
   *
   * @param obstacleEvader the obstacle graph
   * @param flightPlanner the FlightPlanner for finding drone flight plans
   * @param randomSeed the random seed to use for the graph algorithms
   */
  public SensorGraph(ObstacleEvader obstacleEvader, FlightPlanner flightPlanner, long randomSeed) {
    this.obstacleEvader = obstacleEvader;
    this.randomSeed = randomSeed;
    this.flightPlanner = flightPlanner;
  }

  /**
   * Computes a tour which visits all sensors and returns to the starting point.
   *
   * @param startPosition a Coords containing the starting point of the tour
   * @param sensorCoords a list of Coords with the locations of the sensors to visit
   * @return a tour represented as a list of Coords specifying the path from each sensor to the
   *     next, and back to the start.
   */
  public List<Coords> createSensorTour(Coords startPosition, List<Coords> sensorCoords) {
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

      int tourLength = flightPlanner.createFlightPlan(tour).size();
      if (tourLength < shortestLength) {
        shortestTour = tour;
        shortestLength = tourLength;
      }
    }

    // Remove the start point from the graph
    sensorGraph.removeVertex(startPosition);
    return shortestTour;
  }

  /**
   * Creates a complete weighted graph with the points of all of the sensors and the starting
   * position. The edge weights are the shortest distance between the points, avoiding obstacles if
   * necessary.
   *
   * @return a complete SimpleWeightedGraph of sensors and the starting position
   */
  private SimpleWeightedGraph<Coords, DefaultWeightedEdge> createSensorGraph(
      Coords startPosition, List<Coords> sensorCoords) {
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
}
