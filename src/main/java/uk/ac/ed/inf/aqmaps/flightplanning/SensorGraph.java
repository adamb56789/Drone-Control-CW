package uk.ac.ed.inf.aqmaps.flightplanning;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.noflyzone.Obstacles;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A graph of the sensors and their distances to each other, taking into account obstacle evasion.
 */
public class SensorGraph extends SimpleWeightedGraph<Coords, DefaultWeightedEdge> {

  /**
   * Private Constructor
   *
   * @param startPosition the starting position of the drone
   * @param sensorCoords a Collection of the Coords of the sensors to be visited
   * @param obstacles the Obstacles that need to be avoided
   */
  private SensorGraph(Coords startPosition, Collection<Coords> sensorCoords, Obstacles obstacles) {
    super(DefaultWeightedEdge.class);
    var obstaclePathfinder = obstacles.getObstaclePathfinder();
    for (var coords : sensorCoords) {
      addVertex(coords);
    }
    addVertex(startPosition);

    // Create edges between all pairs of vertices
    var vertexList = new ArrayList<>(vertexSet());
    for (int i = 0; i < vertexList.size() - 1; i++) {
      for (int j = i + 1; j < vertexList.size(); j++) {
        var edge = addEdge(vertexList.get(i), vertexList.get(j));
        setEdgeWeight(edge, obstaclePathfinder.getPathLength(vertexList.get(i), vertexList.get(j)));
      }
    }
  }

  /**
   * Creates a complete weighted graph with the points of all of the sensors and the starting
   * position. The edge weights are the shortest distance between the points, avoiding obstacles if
   * necessary.
   *
   * @param startPosition the starting position of the drone
   * @param sensorCoords a Collection of the Coords of the sensors to be visited
   * @param obstacles the Obstacles that need to be avoided
   * @return a SensorGraph
   */
  public static SensorGraph createWithStartLocation(
      Coords startPosition, Collection<Coords> sensorCoords, Obstacles obstacles) {
    return new SensorGraph(startPosition, sensorCoords, obstacles);
  }
}
