package uk.ac.ed.inf.aqmaps;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.tour.HamiltonianCycleAlgorithmBase;
import org.jgrapht.graph.GraphWalk;

import java.util.*;

public class AntColonyTSP<V, E> extends HamiltonianCycleAlgorithmBase<V, E> {
  private final Random rng;
  private Graph<V, E> graph;
  private double[][] pheromone;
  private double[][] dist;
  private int n;
  private Map<V, Integer> index;
  private Map<Integer, V> revIndex;

  public AntColonyTSP() {
    this.rng = new Random();
  }

  @Override
  public GraphPath<V, E> getTour(Graph<V, E> graph) {
    this.checkGraph(graph);
    if (graph.vertexSet().size() == 1) {
      return this.getSingletonTour(graph);
    } else {
      init(graph);
      int l = 100; // number of ants
      int TIME_MAX = 100;
      double alph = 10;
      double beta = 10;
      double rho = 0.8;
      double Q = 40;
      double c = 0.1;
      double[][] eta = new double[n][n]; // desirability from vertex a to b
      double mean = 0;
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          pheromone[i][j] = c;
          if (i == j) {
            eta[i][j] = 0;
          } else {
            eta[i][j] = 1 / dist[i][j];
          }
          mean += eta[i][j];
        }
      }
      mean /= n * n;
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          eta[i][j] /= mean;
        }
      }
      for (int t = 0; t < TIME_MAX; t++) { // passes
        double[][] pheromoneDelta = new double[n][n];
        for (int k = 0; k < l; k++) { // ants
          boolean[] visited = new boolean[n];
          int i = rng.nextInt(n); // current vertex
          visited[i] = true;
          var pathList = new ArrayList<>(List.of(i));
          for (int m = 0; m < n - 1; m++) { // build the path
            double[] p = new double[n];
            for (int j = 0; j < n; j++) {
              if (!visited[j]) {
                p[j] = Math.pow(pheromone[i][j], alph) * Math.pow(eta[i][j], beta);
                //                System.out.println(Math.pow(pheromone[i][j], alph) + " " +
                // Math.pow(eta[i][j], beta));
                double sum = 0;
                for (int s = 0; s < n; s++) {
                  if (!visited[s]) {
                    sum += Math.pow(pheromone[i][s], alph) * Math.pow(eta[i][s], beta);
                  }
                }
                p[j] /= sum;
              }
            }
            int j = chooseRandom(p);
            visited[j] = true;
            pathList.add(j);
          }
          var path = listToPath(pathList);

          double length = path.getWeight();
          for (int index = 0; index < pathList.size() - 1; index++) {
            pheromoneDelta[pathList.get(index)][pathList.get(index + 1)] = Q / length;
          }
        }
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < n; j++) {
            pheromone[i][j] = rho * pheromone[i][j] + pheromoneDelta[i][j];
          }
        }
      }
      System.out.println(Arrays.deepToString(pheromone));
      boolean[] visited = new boolean[n];
      int i = 0; // current vertex
      visited[i] = true;
      var pathList = new ArrayList<>(List.of(i));
      for (int m = 0; m < n - 1; m++) { // build the path
        double[] p = new double[n];
        for (int j = 0; j < n; j++) {
          if (!visited[j]) {
            p[j] = Math.pow(pheromone[i][j], alph) * Math.pow(eta[i][j], beta);
            double sum = 0;
            for (int s = 0; s < n; s++) {
              if (!visited[s]) {
                sum += Math.pow(pheromone[i][s], alph) * Math.pow(eta[i][s], beta);
              }
            }
            p[j] /= sum;
          }
        }
        int j = chooseRandom(p);
        visited[j] = true;
        pathList.add(j);
      }
      System.out.println(
          listToPath(pathList).getWeight() + " " + listToPath(pathList).getVertexList());
      return listToPath(pathList);
    }
  }

  private void init(Graph<V, E> graph) {
    this.graph = graph;
    this.n = graph.vertexSet().size();
    this.pheromone = new double[n][n];
    this.dist = new double[n][n];
    this.index = new HashMap<>();
    this.revIndex = new HashMap<>();
    var iteratorV = graph.vertexSet().iterator();
    for (int i = 0; iteratorV.hasNext(); ++i) {
      V v = iteratorV.next();
      this.index.put(v, i);
      this.revIndex.put(i, v);
    }

    int si;
    int ti;
    double weight;
    for (var iteratorE = graph.edgeSet().iterator(); iteratorE.hasNext(); dist[ti][si] = weight) {
      E e = iteratorE.next();
      V s = graph.getEdgeSource(e);
      si = index.get(s);
      V t = graph.getEdgeTarget(e);
      ti = index.get(t);
      weight = graph.getEdgeWeight(e);
      dist[si][ti] = weight;
    }
  }

  private GraphPath<V, E> listToPath(List<Integer> list) {
    var tourVertices = new ArrayList<V>(n + 1);

    for (int vi : list) {
      V v = revIndex.get(vi);
      tourVertices.add(v);
    }

    return this.vertexListToPath(tourVertices, this.graph);
  }

  private GraphPath<V, E> vertexListToPath(List<V> tour, Graph<V, E> graph) {

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

  private int chooseRandom(double[] weights) {
    // Compute the total weight of all items together
    double totalWeight = 0.0d;
    for (var w : weights) {
      totalWeight += w;
    }
    // Now choose a random item
    int randomIndex = -1;
    double random = Math.random() * totalWeight;
    for (int i = 0; i < weights.length; ++i) {
      random -= weights[i];
      if (random <= 0.0d) {
        randomIndex = i;
        break;
      }
    }
    return randomIndex;
  }
}
