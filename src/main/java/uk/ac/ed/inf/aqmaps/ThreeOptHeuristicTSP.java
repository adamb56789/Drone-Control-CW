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
package uk.ac.ed.inf.aqmaps;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm;
import org.jgrapht.alg.interfaces.HamiltonianCycleImprovementAlgorithm;
import org.jgrapht.alg.tour.HamiltonianCycleAlgorithmBase;
import org.jgrapht.alg.tour.RandomTourTSP;

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
 * {@link #improveTour(GraphPath)}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Dimitrios Michail
 */
public class ThreeOptHeuristicTSP<V, E> extends HamiltonianCycleAlgorithmBase<V, E>
    implements HamiltonianCycleImprovementAlgorithm<V, E> {
  private final int passes;
  private final HamiltonianCycleAlgorithm<V, E> initializer;
  private final double minCostImprovement;

  private Graph<V, E> graph;
  private int n;
  private double[][] dist;
  private Map<V, Integer> index;
  private Map<Integer, V> revIndex;

  /** Constructor. By default one initial random tour is used. */
  public ThreeOptHeuristicTSP() {
    this(1, new Random());
  }

  /**
   * Constructor
   *
   * @param passes how many initial random tours to check
   */
  public ThreeOptHeuristicTSP(int passes) {
    this(passes, new Random());
  }

  /**
   * Constructor
   *
   * @param passes how many initial random tours to check
   * @param seed seed for the random number generator
   */
  public ThreeOptHeuristicTSP(int passes, long seed) {
    this(passes, new Random(seed));
  }

  /**
   * Constructor
   *
   * @param passes how many initial random tours to check
   * @param rng random number generator
   */
  public ThreeOptHeuristicTSP(int passes, Random rng) {
    this(passes, new RandomTourTSP<>(rng));
  }

  /**
   * Constructor
   *
   * @param passes how many initial random tours to check
   * @param rng random number generator
   * @param minCostImprovement Minimum cost improvement per iteration
   */
  public ThreeOptHeuristicTSP(int passes, Random rng, double minCostImprovement) {
    this(passes, new RandomTourTSP<>(rng), minCostImprovement);
  }

  /**
   * Constructor
   *
   * @param initializer Algorithm to generate initial tour
   */
  public ThreeOptHeuristicTSP(HamiltonianCycleAlgorithm<V, E> initializer) {
    this(1, initializer);
  }

  /**
   * Constructor
   *
   * @param passes how many initial tours to check
   * @param initializer Algorithm to generate initial tour
   */
  public ThreeOptHeuristicTSP(int passes, HamiltonianCycleAlgorithm<V, E> initializer) {
    this(passes, initializer, 1e-8);
  }

  /**
   * Constructor
   *
   * @param passes how many initial tours to check
   * @param initializer Algorithm to generate initial tours
   * @param minCostImprovement Minimum cost improvement per iteration
   */
  public ThreeOptHeuristicTSP(
      int passes, HamiltonianCycleAlgorithm<V, E> initializer, double minCostImprovement) {
    if (passes < 1) {
      throw new IllegalArgumentException("passes must be at least one");
    }
    this.passes = passes;
    this.initializer =
        Objects.requireNonNull(initializer, "Initial solver algorithm cannot be null");
    this.minCostImprovement = Math.abs(minCostImprovement);
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
  @Override
  public GraphPath<V, E> getTour(Graph<V, E> graph) {
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
  @Override
  public GraphPath<V, E> improveTour(GraphPath<V, E> tour) {
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
    this.dist = new double[n][n];
    this.index = new HashMap<>();
    this.revIndex = new HashMap<>();
    int i = 0;
    for (V v : graph.vertexSet()) {
      index.put(v, i);
      revIndex.put(i, v);
      i++;
    }

    for (E e : graph.edgeSet()) {
      V s = graph.getEdgeSource(e);
      int si = index.get(s);
      V t = graph.getEdgeTarget(e);
      int ti = index.get(t);
      double weight = graph.getEdgeWeight(e);
      dist[si][ti] = weight;
      dist[ti][si] = weight;
    }
  }

  /**
   * Create an initial tour
   *
   * @return a complete tour
   */
  private int[] createInitialTour() {
    var oldA = pathToTour(initializer.getTour(graph));
    var newA = new int[n];
    System.arraycopy(oldA, 0, newA, 0, n);
    return newA;
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
  private int[] improve(int[] tour) {
    var allSegments = allSegments();
    int counter = 0;
    while (true) {
      double delta = 0;
      for (var combination : allSegments) {
        delta += reverseSegmentIfBetter(tour, combination[0], combination[1], combination[2]);
      }
      System.out.println(delta);
      System.out.println(Arrays.toString(tour));
      if (delta > 0) {
        break;
      }
    }
    var completeTour = new int[n + 1];
    System.arraycopy(tour, 0, completeTour, 0, n);
    completeTour[n] = completeTour[0];
    return completeTour;
  }

  private List<int[]> allSegments() {
    var list = new ArrayList<int[]>();
    for (int i = 0; i < n; i++) {
      for (int j = i + 2; j < n; j++) {
        int temp = 0;
        if (i > 0) {
          temp = 1;
        }
        for (int k = j + 2; k < n; k++) {
          int[] array = new int[3];
          array[0] = i;
          array[1] = j;
          array[2] = k;
          list.add(array);
        }
      }
    }
    return list;
  }

  private double reverseSegmentIfBetter(int[] tour, int i, int j, int k) {
    int a = i;
    int b = (i + 1) % n;
    int c = j;
    int d = (j + 1) % n;
    int e = k;
    int f = (k + 1) % n;
    double d0 = dist[a][b] + dist[c][d] + dist[e][f];
    double d1 = dist[a][c] + dist[b][d] + dist[e][f];
    double d2 = dist[a][b] + dist[c][e] + dist[d][f];
    double d3 = dist[a][d] + dist[e][b] + dist[c][f];
    double d4 = dist[f][b] + dist[c][d] + dist[e][a];
    if (d0 > d1) {
      reverseSegment(tour, i, j);
      return -d0 + d1;
    } else if (d0 > d2) {
      reverseSegment(tour, j, k);
      return -d0 + d2;
    } else if (d0 > d4) {
      reverseSegment(tour, i, k);
      return -d0 + d4;
    } else if (d0 > d3) {
      var temp = new int[k - i];
      int tempI = 0;
      for (int l = j; l < k; l++) {
        temp[tempI++] = tour[l];
      }
      for (int l = i; l < j; l++) {
        temp[tempI++] = tour[l];
      }
      for (int l = 0; l < k - i; l++) {
        tour[i + l] = temp[l];
      }
      return -d0 + d3;
    }
    return 0;
  }

  private void reverseSegment(int[] tour, int i, int j) {
    for (int k = i; k < j; k++) {
      int temp = tour[i];
      tour[i] = tour[j - 1 - i];
      tour[j - 1 - i] = temp;
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
}
