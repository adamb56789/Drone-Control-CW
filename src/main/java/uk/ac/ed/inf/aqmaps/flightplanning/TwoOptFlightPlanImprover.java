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
package uk.ac.ed.inf.aqmaps.flightplanning;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.tour.TwoOptHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.*;

/**
 * A modified version of TwoOptHeuristicTSP from JGraphT which is used to compute tours which visit
 * all sensors and return to the starting point. Source code of TwoOptHeuristicTSP can be found <a
 * href="https://github.com/jgrapht/jgrapht/blob/master/jgrapht-core/src/main/java/org/jgrapht/alg/tour/TwoOptHeuristicTSP.java">here
 * (GitHub)</a>.
 *
 * <p><a href="https://jgrapht.org/">JGraphT main website</a>, <a
 * href="https://github.com/jgrapht/jgrapht">GitHub source</a>, accessed 30/11/2020
 *
 * <p>The following is unchanged from the the original
 *
 * <p>The 2-opt heuristic algorithm for the TSP problem.
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
 * {@link #improveTour(GraphPath)}}.
 *
 * @author Dimitrios Michail
 */
public class TwoOptFlightPlanImprover extends TwoOptHeuristicTSP<Coords, DefaultWeightedEdge> {
  private final double minCostImprovement;
  private final Coords start;
  private final FlightPlanner flightPlanner;
  private Graph<Coords, DefaultWeightedEdge> graph;
  private int n;
  private Map<Coords, Integer> index;
  private Map<Integer, Coords> revIndex;

  public TwoOptFlightPlanImprover(
      int passes, long seed, Coords start, FlightPlanner flightPlanner) {
    super(passes, seed);
    this.minCostImprovement = Math.abs(1e-8);
    this.start = start;
    this.flightPlanner = flightPlanner;
  }

  /**
   * (Reduced version of library code as we aren't using dist[][])
   *
   * <p>Initialize graph and mapping to integer vertices.
   *
   * @param graph the input graph
   */
  private void init(Graph<Coords, DefaultWeightedEdge> graph) {
    this.graph = graph;
    this.n = graph.vertexSet().size();
    this.index = new HashMap<>();
    this.revIndex = new HashMap<>();
    int i = 0;
    for (var v : graph.vertexSet()) {
      index.put(v, i);
      revIndex.put(i, v);
      i++;
    }
  }

  /**
   * A modified version of the improve() method in the library. The main difference is that this
   * version does not have a distance matrix due to the fact that the distance between a pair of
   * sensors depends heavily on the rest of the tour that it is in. Instead of calculating the
   * distance change with
   *
   * <p><code>
   * double change = dist[ci][cj] + dist[ci1][cj1] - dist[ci][ci1] - dist[cj][cj1];</code>
   *
   * <p>we instead use the FlightPlanner to plan a route along the tour, and compare its length to
   * the unmodified tour. Since the tours being improved by this algorithm have already been through
   * 2-opt with direct distance measures, drastic changes to the tour are unlikely to improve it,
   * and are also computationally expensive since they are longer and may collide with more
   * buildings and so on. To avoid this, only tours which are close in distance to the original will
   * be tried.
   *
   * <p>Original JGraphT JavaDoc:
   *
   * <p>Improve the tour using the 2-opt heuristic. In each iteration it applies the best possible
   * 2-opt move which means to find the best pair of edges $(i,i+1)$ and $(j,j+1)$ such that
   * replacing them with $(i,j)$ and $(i+1,j+1)$ minimizes the tour length.
   *
   * <p>The returned array instance might or might not be the input array.
   *
   * @param tour the input tour
   * @return a possibly improved tour
   */
  private int[] improve(int[] tour) {
    int[] newTour = new int[n + 1];
    boolean moved;
    double minChange;
    do {
      // Calculate the direct and drone lengths of the current state of the tour
      var originalList = getTourAsList(tour);
      var originalDirectLength = getDirectLength(originalList);
      int originalLength = flightPlanner.computeLengthOfFlight(originalList);

      moved = false;
      minChange = -minCostImprovement;
      int mini = -1;
      int minj = -1;
      for (int i = 0; i < n - 2; i++) {
        for (int j = i + 2; j < n; j++) {
          // Apply the move to a temporary tour
          int[] tempTour = new int[n + 1];
          applyMove(tour, tempTour, i, j);
          var vertexList = getTourAsList(tempTour);

          // The input graph has already been through 2-opt with the direct length calculation,
          // so only try to swap when the change is relatively minor
          var newDirectLength = getDirectLength(vertexList);
          if (newDirectLength < originalDirectLength * 1.1) {
            int change = flightPlanner.computeLengthOfFlight(vertexList) - originalLength;
            if (change < minChange) {
              minChange = change;
              mini = i;
              minj = j;
            }
          }
        }
      }
      if (mini != -1 && minj != -1) {
        // apply move
        applyMove(tour, newTour, mini, minj);
        // swap tours
        int[] tmp = tour;
        tour = newTour;
        newTour = tmp;
        moved = true;
      }
    } while (moved);

    return tour;
  }

  /**
   * New method, not in the library. Converts a tour as an array to the corresponding list of
   * Coords.
   *
   * @param tour a tour as an int[]
   * @return a List of Coords
   */
  private List<Coords> getTourAsList(int[] tour) {
    List<Coords> originalList = tourToPath(tour).getVertexList();
    // The first element in the vertex list seems to be random, but we want it to be the start
    originalList.remove(0); // The first and last elements are duplicates, so remove the duplicate

    // Rotate the list backwards so the starting position is at the front
    Collections.rotate(originalList, -originalList.indexOf(start));
    originalList.add(
        originalList.get(0)); // Put the starting position as the ending position as well
    return originalList;
  }

  /**
   * New method, not in the library.
   *
   * @return The length of the tour in degrees, using euclidean distance (not using the flight
   *     planner)
   */
  private double getDirectLength(List<Coords> list) {
    double directLength = 0;
    for (int i = 0; i < list.size() - 1; i++) {
      directLength += list.get(i).distance(list.get(i + 1));
    }
    return directLength;
  }

  /**
   * This code is part of the original library's improve(), but it is extracted into a method to
   * avoid duplication as this class uses it in more than one place.
   */
  private void applyMove(int[] tour, int[] newTour, int i, int j) {
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
   * (Code and Javadoc unchanged from library other than type parameters)
   *
   * <p>Transform from an array representation to a graph path.
   *
   * @param tour an array containing the index of the vertices of the tour
   * @return a graph path
   */
  private GraphPath<Coords, DefaultWeightedEdge> tourToPath(int[] tour) {
    List<Coords> tourVertices = new ArrayList<>(n + 1);
    for (int vi : tour) {
      Coords v = revIndex.get(vi);
      tourVertices.add(v);
    }
    return closedVertexListToTour(tourVertices, graph);
  }

  /**
   * (Code and Javadoc unchanged from library other than type parameters)
   *
   * <p>Transform from a path representation to an array representation.
   *
   * @param path graph path
   * @return an array containing the index of the vertices of the tour
   */
  private int[] pathToTour(GraphPath<Coords, DefaultWeightedEdge> path) {
    int i = 0;
    int[] tour = new int[n + 1];
    var v = path.getStartVertex();
    tour[i++] = index.get(v);
    for (var e : path.getEdgeList()) {
      v = Graphs.getOppositeVertex(graph, e, v);
      tour[i++] = index.get(v);
    }
    return tour;
  }

  /**
   * Computes a tour by first using JGraphT's TwoOptHeuristicTSP (the superclass of this) to find a
   * short tour using the edge weights in the provided graph, and then running a modified version of
   * JGraphT's TwoOptHeuristicTSP.improveTour() which uses the drone FlightPlanner to compute tour
   * weight.
   *
   * @param graph the input graph containing the start location and the sensors, and edge weights of
   *     the shortest path between two points which avoids obstacles.
   * @return the tour as a GraphPath
   */
  @Override
  public GraphPath<Coords, DefaultWeightedEdge> getTour(Graph<Coords, DefaultWeightedEdge> graph) {
    var directDistanceTour = super.getTour(graph);
    return improveTour(directDistanceTour);
  }

  /**
   * (Code unchanged from library code other than type parameters)
   *
   * <p>Try to improve a tour by running the 2-opt heuristic using the FlightPlanner to measure the
   * length of tours.
   *
   * @param graphPath a tour
   * @return a possibly improved tour
   */
  @Override
  public GraphPath<Coords, DefaultWeightedEdge> improveTour(
      GraphPath<Coords, DefaultWeightedEdge> graphPath) {
    init(graphPath.getGraph());
    return tourToPath(improve(pathToTour(graphPath)));
  }
}
