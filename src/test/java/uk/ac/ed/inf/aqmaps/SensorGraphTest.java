package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;
import uk.ac.ed.inf.aqmaps.pathfinding.FlightPlanner;
import uk.ac.ed.inf.aqmaps.pathfinding.ObstacleEvader;
import uk.ac.ed.inf.aqmaps.pathfinding.Obstacles;
import uk.ac.ed.inf.aqmaps.pathfinding.SensorGraph;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SensorGraphTest {
  SensorGraph sensorGraph;
  List<Coords> sensorCoords;

  @Before
  public void setup() {
    var testServer = ServerInputControllerTest.getFakeServer();
    var input = new ServerInputController(testServer, 1, 1, 2020, 80);
    var obstacles = new Obstacles(input.getNoFlyZones());
    var obstacleGraph = new ObstacleEvader(obstacles);
    sensorCoords = W3W.convertToCoords(input.getSensorW3Ws());
    sensorGraph =
        new SensorGraph(
            obstacleGraph, new FlightPlanner(obstacles, obstacleGraph, input.getSensorW3Ws()), 0);
  }

  @Test
  public void tourLengthCorrect() {
    var testCoords = new Coords(-3.1878, 55.9444);
    assertEquals(35, sensorGraph.createSensorTour(testCoords, sensorCoords).size());
  }

  @Test
  public void tourStartingPositionCorrect() {
    var testCoords = new Coords(-3.1878, 55.9444);
    assertEquals(
        testCoords.x, sensorGraph.createSensorTour(testCoords, sensorCoords).get(0).x, 0.00000001);
  }
}
