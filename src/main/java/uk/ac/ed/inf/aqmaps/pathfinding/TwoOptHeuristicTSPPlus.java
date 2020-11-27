/*
 * (C) Copyright 2018-2020, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package uk.ac.ed.inf.aqmaps.pathfinding;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm;
import org.jgrapht.alg.interfaces.HamiltonianCycleImprovementAlgorithm;
import org.jgrapht.alg.tour.HamiltonianCycleAlgorithmBase;
import org.jgrapht.alg.tour.RandomTourTSP;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.*;

/**
 * The 2-opt heuristic algorithm for the TSP problem.
 *
 * <p>The travelling salesman problem (TSP) asks the following question: "Given a list of cities and
 * the distances between each pair of cities, what is the shortest possible route that visits each
 * city exactly once and returns to the origin city?".
 *
 * <p>This is an implementation of the 2-opt improvement heuristic algorithm. The algorithm
 * generates <em>passes</em> initial tours and then iteratively improves the tours until a local
 * minimum is reached. In each iteration it applies the best possible 2-opt move which means to find
 * the best pair of edges $(i,i+1)$ and $(j,j+1)$ such that replacing them with $(i,j)$ and
 * $(i+1,j+1)$ minimizes the tour length. The default initial tours use RandomTour, however an
 * alternative algorithm can be provided to create the initial tour. Initial tours generated using
 * NearestNeighborHeuristicTSP give good results and performance.
 *
 * <p>See <a href="https://en.wikipedia.org/wiki/2-opt">wikipedia</a> for more details.
 *
 * <p>This implementation can also be used in order to try to improve an existing tour. See method
 * {@link #improveTourPlus(GraphPath)}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Dimitrios Michail
 */
public class TwoOptHeuristicTSPPlus<V, E> extends HamiltonianCycleAlgorithmBase<V, E>
    implements HamiltonianCycleImprovementAlgorithm<V, E> {
  private final int passes;
  private final HamiltonianCycleAlgorithm<V, E> initializer;
  private final double minCostImprovement;
  private final Coords start;
  private final FlightPlanner flightPlanner;
  private Graph<V, E> graph;
  private int n;
  private Map<V, Integer> index;
  private Map<Integer, V> revIndex;

  /**
   * Constructor
   *
   * @param passes how many initial random tours to check
   * @param seed seed for the random number generator
   */
  public TwoOptHeuristicTSPPlus(int passes, long seed, Coords start, FlightPlanner flightPlanner) {
    if (passes < 1) {
      throw new IllegalArgumentException("passes must be at least one");
    }
    this.passes = passes;
    this.initializer =
        Objects.requireNonNull(
            (HamiltonianCycleAlgorithm<V, E>) new RandomTourTSP<V, E>(new Random(seed)),
            "Initial solver algorithm cannot be null");
    this.minCostImprovement = Math.abs(1e-8);
    this.start = start;
    this.flightPlanner = flightPlanner;
  }

  // algorithm

  /**
   * Computes a 2-approximate tour.
   *
   * @param graph the input graph
   * @return a tour
   * @throws IllegalArgumentException if the graph is not undirected
   * @throws IllegalArgumentException if the graph is not complete
   * @throws IllegalArgumentException if the graph contains no vertices
   */
  public GraphPath<V, E> getTourPlus(Graph<V, E> graph) throws InterruptedException {
    checkGraph(graph);
    if (graph.vertexSet().size() == 1) {
      return getSingletonTour(graph);
    }

    // Initialize vertex index and distances
    init(graph);

    // Execute 2-opt for the specified number of passes and a new permutation in each pass
    GraphPath<V, E> best = tourToPath(improve(createInitialTour()));
    for (int i = 1; i < passes; i++) {
      GraphPath<V, E> other = tourToPath(improve(createInitialTour()));
      if (other.getWeight() < best.getWeight()) {
        best = other;
      }
    }
    return best;
  }

  /**
   * Try to improve a tour by running the 2-opt heuristic.
   *
   * @param tour a tour
   * @return a possibly improved tour
   */
  public GraphPath<V, E> improveTourPlus(GraphPath<V, E> tour) throws InterruptedException {
    init(tour.getGraph());
    return tourToPath(improve(pathToTour(tour)));
  }

  /**
   * Initialize graph and mapping to integer vertices.
   *
   * @param graph the input graph
   */
  private void init(Graph<V, E> graph) {
    this.graph = graph;
    this.n = graph.vertexSet().size();
    this.index = new HashMap<>();
    this.revIndex = new HashMap<>();
    int i = 0;
    for (V v : graph.vertexSet()) {
      index.put(v, i);
      revIndex.put(i, v);
      i++;
    }
  }

  /**
   * Create an initial tour
   *
   * @return a complete tour
   */
  private int[] createInitialTour() {
    return pathToTour(initializer.getTour(graph));
  }

  /**
   * Improve the tour using the 2-opt heuristic. In each iteration it applies the best possible
   * 2-opt move which means to find the best pair of edges $(i,i+1)$ and $(j,j+1)$ such that
   * replacing them with $(i,j)$ and $(i+1,j+1)$ minimizes the tour length.
   *
   * <p>The returned array instance might or might not be the input array.
   *
   * @param tour the input tour
   * @return a possibly improved tour
   */
  private int[] improve(int[] tour) throws InterruptedException {
    int[] newTour = new int[n + 1];
    boolean moved;
    double minChange;
    do {
      List<Coords> originalList = (List<Coords>) tourToPath(tour).getVertexList();
      // The first element in the vertex list seems to be random, but we want it to be the start
      originalList.remove(0); // The first and last elements are duplicates, so remove the duplicate

      // Rotate the list backwards so the starting position is at the front
      Collections.rotate(originalList, -originalList.indexOf(start));
      originalList.add(
          originalList.get(0)); // Put the starting position as the ending position as well

      int originalLength = flightPlanner.createFlightPlan(originalList).size();

      moved = false;
      minChange = -minCostImprovement;
      int mini = -1;
      int minj = -1;
      for (int i = 0; i < n - 2; i++) {
        for (int j = i + 2; j < n; j++) {
          int[] tempTour = new int[n + 1];
          swap(tour, tempTour, i, j);
          List<Coords> vertexList = (List<Coords>) tourToPath(tempTour).getVertexList();
          // The first element in the vertex list seems to be random, but we want it to be the start
          vertexList.remove(
              0); // The first and last elements are duplicates, so remove the duplicate

          // Rotate the list backwards so the starting position is at the front
          Collections.rotate(vertexList, -vertexList.indexOf(start));
          vertexList.add(
              vertexList.get(0)); // Put the starting position as the ending position as well

          int change = flightPlanner.createFlightPlan(vertexList).size() - originalLength;
          if (change < minChange) {
            minChange = change;
            mini = i;
            minj = j;
          }
        }
      }
      if (mini != -1 && minj != -1) {
        // apply move
        swap(tour, newTour, mini, minj);
        // swap tours
        int[] tmp = tour;
        tour = newTour;
        newTour = tmp;
        moved = true;
      }
    } while (moved);

    return tour;
  }

  private void swap(int[] tour, int[] newTour, int i, int j) {
    int a = 0;
    for (int k = 0; k <= i; k++) {
      newTour[a++] = tour[k];
    }
    for (int k = j; k >= i + 1; k--) {
      newTour[a++] = tour[k];
    }
    for (int k = j + 1; k < n + 1; k++) {
      newTour[a++] = tour[k];
    }
  }

  /**
   * Transform from an array representation to a graph path.
   *
   * @param tour an array containing the index of the vertices of the tour
   * @return a graph path
   */
  private GraphPath<V, E> tourToPath(int[] tour) {
    List<V> tourVertices = new ArrayList<>(n + 1);
    for (int vi : tour) {
      V v = revIndex.get(vi);
      tourVertices.add(v);
    }
    return closedVertexListToTour(tourVertices, graph);
  }

  /**
   * Transform from a path representation to an array representation.
   *
   * @param path graph path
   * @return an array containing the index of the vertices of the tour
   */
  private int[] pathToTour(GraphPath<V, E> path) {
    Set<V> visited = new HashSet<>();
    int i = 0;
    int[] tour = new int[n + 1];
    V v = path.getStartVertex();
    tour[i++] = index.get(v);
    for (E e : path.getEdgeList()) {
      v = Graphs.getOppositeVertex(graph, e, v);
      if (!visited.add(v)) {
        throw new IllegalArgumentException("Not a valid tour");
      }
      tour[i++] = index.get(v);
    }
    if (i < n + 1) {
      throw new IllegalArgumentException("Not a valid tour");
    }
    return tour;
  }

  @Override
  public GraphPath<V, E> getTour(Graph<V, E> graph) {
    return null;
  }

  @Override
  public GraphPath<V, E> improveTour(GraphPath<V, E> graphPath) {
    return null;
  }
}
