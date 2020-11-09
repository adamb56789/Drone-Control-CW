package uk.ac.ed.inf.aqmaps;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Holds a weighted graph containing the sensor locations and the shortest paths between them.
 * Computes a short, though not necessarily optimal tour (travelling salesman problem).
 */
public class SensorGraph {
  private SimpleWeightedGraph<Point2D, DefaultWeightedEdge> graph;

  public SensorGraph(ObstacleGraph obstacleGraph) {}

  /**
   * Add a list of sensors to the graph. Computes the shortest distance between every pair of points
   * and adds them as an edge.
   *
   * @param sensorLocations a list of W3W with the locations of the sensors
   */
  public void addSensors(List<W3W> sensorLocations) {}//TODO

  /**
   * Computes an approximate solution to the travelling salesman problem, which is a tour visiting
   * all sensors and returning to the starting point. Uses the TODO algorithm
   *
   * @param start a Point2D containing the starting point of the tour, which is separate from the
   *     sensors
   * @return a list of points specifying the tour
   */
  public List<Point2D> getTour(Point2D start) {
    return null;//TODO
  }
}
