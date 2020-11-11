package uk.ac.ed.inf.aqmaps;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm;
import org.jgrapht.alg.tour.*;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Holds a weighted graph containing the sensor locations and the shortest paths between them.
 * Computes a short, though not necessarily optimal tour (travelling salesman problem).
 */
public class SensorGraph {
  private final SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;

  /** We keep a separate list of vertices to make scanning through all vertices faster */
  private final List<Coords> vertices;

  private final ObstacleGraph obstacleGraph;

  private final long randomSeed;
  private static final int PASSES = 1000;

  /**
   * Initialise the graph using a list of sensor locations and an obstacle graph for pathfinding.
   * Computes the shortest distance between every pair of points and adds them as an edge.
   *
   * @param sensorLocations a list of W3W with the locations of the sensors
   * @param obstacleGraph the obstacle graph
   * @param randomSeed the random seed to use for the graph algorithms
   */
  public SensorGraph(List<W3W> sensorLocations, ObstacleGraph obstacleGraph, long randomSeed) {
    this.obstacleGraph = obstacleGraph;
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
   * Computes an approximate solution to the travelling salesman problem, which is a tour visiting
   * all sensors and returning to the starting point. Uses the TODO algorithm
   *
   * @param start a Point2D containing the starting point of the tour, which is separate from the
   *     sensors
   * @return a list of points specifying the tour
   */
  public List<Coords> getTour(Coords start) {
    // Add the start point and all possible edges to and from
    addVertex(start);
    for (var vertex : vertices) {
      if (start != vertex) {
        addEdge(start, vertex);
      }
    }

    var path = getPath(new RandomTourTSP<>(new Random(randomSeed)));
    Drone.results[0].add(path.getWeight());
    path = getPath(new GreedyHeuristicTSP<>());
    Drone.results[1].add(path.getWeight());
    path = getPath(new ChristofidesThreeHalvesApproxMetricTSP<>());
    Drone.results[2].add(path.getWeight());
    path = getPath(new NearestInsertionHeuristicTSP<>());
    Drone.results[3].add(path.getWeight());
    path = getPath(new NearestNeighborHeuristicTSP<>(randomSeed));
    Drone.results[4].add(path.getWeight());
    path = getPath(new TwoApproxMetricTSP<>());
    Drone.results[5].add(path.getWeight());
    path = getPath(new TwoOptHeuristicTSP<>(PASSES, randomSeed));
    Drone.results[6].add(path.getWeight());

    // Remove the start and end points from the graph for later reuse
    removeVertex(start);

    return path.getVertexList();
  }

  private GraphPath<Coords, DefaultWeightedEdge> getPath(
      HamiltonianCycleAlgorithm<Coords, DefaultWeightedEdge> alg) {
    var twoOptAlg = new TwoOptHeuristicTSP<Coords, DefaultWeightedEdge>(PASSES, randomSeed);
    return twoOptAlg.improveTour(alg.getTour(graph));
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
    graph.setEdgeWeight(e, obstacleGraph.getShortestPathLength(start, end));
  }
}
