package uk.ac.ed.inf.aqmaps.pathfinding;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.tour.HamiltonianCycleAlgorithmBase;
import org.jgrapht.alg.tour.RandomTourTSP;
import org.jgrapht.graph.GraphWalk;
import uk.ac.ed.inf.aqmaps.Testing;

import java.util.*;

public class SimulatedAnnealingTSP<V, E> extends HamiltonianCycleAlgorithmBase<V, E> {
  public static final int INT = 10000;
  private final Random rng;
  private Graph<V, E> graph;
  private int n;

  private List<V> tourVList = new ArrayList<>();
  private int lastSwappedIndex1;
  private int lastSwappedIndex2;

  public SimulatedAnnealingTSP(long randomSeed) {
    this.rng = new Random(randomSeed);
  }

  @Override
  public GraphPath<V, E> getTour(Graph<V, E> graph) {
    init(graph);

    double startingTemperature = 125;
    int numberOfIterations = 10000;
    double coolingRate = 0.9997;
    double t = startingTemperature;

    var initialPath = (new RandomTourTSP<V, E>(rng)).getTour(graph);
    tourVList = initialPath.getVertexList();
    tourVList.remove(0);

    double bestDistance = getLength();
//    System.out.println("Initial distance of travel: " + bestDistance);

    for (int i = 0; i < numberOfIterations; i++) {
      if (t > 0.1) {
        swapRandomVertices();
        double currentDistance = getLength();
        if (currentDistance < bestDistance) {
          bestDistance = currentDistance;
        } else if (Math.exp((bestDistance - currentDistance) / t) < rng.nextDouble()) {
          undoLastSwap();
        }
        t *= coolingRate;
      }
//      if (i % 1000 == 0) {
//        System.out.println("Iteration #" + i);
//      }
    }
//    System.out.println(getLength());
    Testing.numbers.add(getLength() / INT);
    return vertexListToPath(tourVList);
  }

  private double getLength() {
    double weight = 0;
    for (int i = 0; i < n; i++) {
      weight += graph.getEdgeWeight(graph.getEdge(tourVList.get(i), tourVList.get((i + 1) % n))) * INT;
    }
    return weight;
  }

  private void swapRandomVertices() {
    int index1 = rng.nextInt(n);
    int index2 = rng.nextInt(n);
    lastSwappedIndex1 = index1;
    lastSwappedIndex2 = index2;
    var vertex1 = tourVList.get(index1);
    var vertex2 = tourVList.get(index2);
    tourVList.set(index1, vertex2);
    tourVList.set(index2, vertex1);
  }

  private void undoLastSwap() {
    var vertex1 = tourVList.get(lastSwappedIndex1);
    var vertex2 = tourVList.get(lastSwappedIndex2);
    tourVList.set(lastSwappedIndex1, vertex2);
    tourVList.set(lastSwappedIndex2, vertex1);
  }

  private void init(Graph<V, E> graph) {
    this.graph = graph;
    this.n = graph.vertexSet().size();
  }

  private GraphPath<V, E> vertexListToPath(List<V> tour) {

    var edges = new ArrayList<E>(tour.size() - 1);
    double tourWeight = 0.0D;
    V startVertex = tour.get(0);

    V v;
    for (Iterator<V> vIterator = tour.subList(1, tour.size()).iterator();
        vIterator.hasNext();
        startVertex = v) {
      v = vIterator.next();
      E e = graph.getEdge(startVertex, v);
      edges.add(e);
      tourWeight += graph.getEdgeWeight(e);
    }

    return new GraphWalk<V, E>(
        graph, tour.get(0), tour.get(tour.size() - 1), tour, edges, tourWeight);
  }
}
