package uk.ac.ed.inf.aqmaps.pathfinding;

import org.jgrapht.alg.tour.TwoOptHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.W3W;
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
  private final SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;
  /** We keep a separate list of vertices to make scanning through all vertices faster */
  private final List<Coords> vertices;

  private final ObstacleEvader obstacleEvader;
  private final long randomSeed;
  private final List<W3W> sensorLocations;

  /**
   * Initialise the graph using a list of sensor locations and an obstacle graph for pathfinding.
   * Computes the shortest distance between every pair of points and adds them as an edge.
   *
   * @param sensorLocations a list of W3W with the locations of the sensors
   * @param obstacleEvader the obstacle graph
   * @param randomSeed the random seed to use for the graph algorithms
   */
  public SensorGraph(List<W3W> sensorLocations, ObstacleEvader obstacleEvader, long randomSeed) {
    this.sensorLocations = sensorLocations;
    this.obstacleEvader = obstacleEvader;
    this.randomSeed = randomSeed;
    graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    vertices = new ArrayList<>();

    // Add all of the vertices from the sensors
    for (var w3w : sensorLocations) {
      addVertex(w3w.getCoordinates());
    }

    // Create edges between all pairs of points
    for (int i = 0; i < vertices.size(); i++) {
      for (int j = 0; j < i; j++) {
        addEdge(vertices.get(i), vertices.get(j));
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
    addVertex(startPosition);
    for (var vertex : vertices) {
      if (startPosition != vertex) {
        addEdge(startPosition, vertex);
      }
    }

    // Try several times and keep the best tour. Also stop if too much time passes
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

      int tourLength =
          (new FlightPlanner(obstacleEvader, sensorLocations)).createFlightPlan(tour).size();
      if (tourLength < shortestLength) {
        shortestTour = tour;
        shortestLength = tourLength;
      }
    }

    removeVertex(startPosition); // Remove the start point from the graph
    return shortestTour;
  }

  private void addVertex(Coords vertex) {
    graph.addVertex(vertex);
    vertices.add(vertex);
  }

  private void removeVertex(Coords vertex) {
    graph.removeVertex(vertex);
    vertices.remove(vertex);
  }

  private void addEdge(Coords start, Coords end) {
    DefaultWeightedEdge e = graph.addEdge(start, end);
    // Calculate the length of the shortest path between the two points, including diversions to
    // avoid obstacles
    graph.setEdgeWeight(e, obstacleEvader.getShortestPathLength(start, end));
  }
}
