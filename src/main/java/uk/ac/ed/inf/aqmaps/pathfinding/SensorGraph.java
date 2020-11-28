package uk.ac.ed.inf.aqmaps.pathfinding;

import org.jgrapht.alg.tour.TwoOptHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

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

  private final SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;

  private final ObstacleEvader obstacleEvader;
  private final long randomSeed;
  private final FlightPlanner flightPlanner;

  /**
   * Initialise the graph using a list of sensor locations and an obstacle graph for pathfinding.
   * Computes the shortest distance between every pair of points and adds them as an edge.
   *
   * @param sensorLocations a list of Coords with the locations of the sensors
   * @param obstacleEvader the obstacle graph
   * @param flightPlanner the FlightPlanner for finding drone flight plans
   * @param randomSeed the random seed to use for the graph algorithms
   */
  public SensorGraph(
      List<Coords> sensorLocations,
      ObstacleEvader obstacleEvader,
      FlightPlanner flightPlanner,
      long randomSeed) {
    this.obstacleEvader = obstacleEvader;
    this.randomSeed = randomSeed;
    this.flightPlanner = flightPlanner;
    graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    // Add all of the vertices from the sensors
    for (var coords : sensorLocations) {
      graph.addVertex(coords);
    }

    // Create edges between all pairs of points
    for (int i = 0; i < sensorLocations.size(); i++) {
      for (int j = 0; j < i; j++) {
        addEdge(sensorLocations.get(i), sensorLocations.get(j));
      }
    }
  }

  /**
   * Computes a tour which visits all sensors and returns to the starting point.
   *
   * @param startPosition a Coords containing the starting point of the tour, which is separate from
   *     the sensors
   * @return a tour represented as a list of Coords specifying the path from each sensor to the
   *     next, and back to the start.
   */
  public List<Coords> getTour(Coords startPosition) {
    // Add the start point and all possible edges to and from
    graph.addVertex(startPosition);
    for (var vertex : graph.vertexSet()) {
      if (startPosition != vertex) {
        addEdge(startPosition, vertex);
      }
    }

    // Try several times and keep the best tour.
    List<Coords> shortestTour = null;
    int shortestLength = Integer.MAX_VALUE;
    for (int i = 0; i < ITERATIONS; i++) {

      var twoOpt =
          new TwoOptHeuristicTSP<Coords, DefaultWeightedEdge>(TWO_OPT_PASSES, randomSeed + i);
      var graphPath = twoOpt.getTour(graph);
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
    graph.removeVertex(startPosition);
    return shortestTour;
  }

  /**
   * Add an edge between the two points with the weight of the length of the shortest path between
   * the two points, including flying around any obstacles.
   */
  private void addEdge(Coords start, Coords end) {
    DefaultWeightedEdge e = graph.addEdge(start, end);
    graph.setEdgeWeight(e, obstacleEvader.getShortestPathLength(start, end));
  }
}
