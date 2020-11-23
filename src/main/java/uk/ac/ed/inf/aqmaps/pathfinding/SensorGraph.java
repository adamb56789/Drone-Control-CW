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
   * The number of initial tours tried by the 2-opt TSP. 400 was chosen since in testing, with all
   * days from 2020 and 2021 and the random seed 0, increasing it further yielded no further
   * improvement. It can be adjusted depending on the needs of the scenario. Note that in testing,
   * reducing it from 400 to 10 only increased the average path length by about 0.03 %, with a
   * performance improvement of about 10 ms per day, which is negligible in this case. Decreasing it
   * to 1 increased path length by about 4.0 %.
   */
  private static final int INITIAL_TOURS = 400;

  private final SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;
  /** We keep a separate list of vertices to make scanning through all vertices faster */
  private final List<Coords> vertices;

  private final ObstacleGraph obstacleGraph;
  private final long randomSeed;

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
   * Computes a tour which visits all sensors and returns to the starting point.
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

    var algorithm = new TwoOptHeuristicTSP<Coords, DefaultWeightedEdge>(INITIAL_TOURS, randomSeed);
    var path = algorithm.getTour(graph);

    // Remove the start and end points from the graph for later reuse
    removeVertex(start);

    var vertexList = path.getVertexList();
    // The first element in the vertex list seems to be random, but we want it to be the start
    vertexList.remove(0); // The first and last elements are duplicates, so remove the duplicate

    // Rotate the list backwards so the starting position is at the front
    Collections.rotate(vertexList, -vertexList.indexOf(start));
    vertexList.add(vertexList.get(0)); // Put the starting position as the ending position as well
    return vertexList;
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
