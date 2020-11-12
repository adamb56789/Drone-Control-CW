package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.ServerController;

import static org.junit.Assert.assertEquals;

public class SensorGraphTest {
  SensorGraph sensorGraph;

  @Before
  public void setup() {
    var testServer = ServerControllerTest.getFakeServer();
    var input = new ServerController(testServer, 1, 1, 2020, 80);
    var obstacleGraph = new ObstacleGraph(new Obstacles(input.getNoFlyZones()));
    sensorGraph = new SensorGraph(input.getSensorLocations(), obstacleGraph, 0);
  }

  @Test
  public void tourLengthCorrect() {
    var testCoords = new Coords(-3.1878, 55.9444);
    assertEquals(35, sensorGraph.getTour(testCoords).size());
  }

  @Test
  public void tourStartingPositionCorrect() {
    var testCoords = new Coords(-3.1878, 55.9444);
    assertEquals(testCoords.x, sensorGraph.getTour(testCoords).get(0).x, 0.00000001);
  }
}
