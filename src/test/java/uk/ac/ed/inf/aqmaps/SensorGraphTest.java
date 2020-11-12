package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.ServerController;

import static org.junit.Assert.assertEquals;

public class SensorGraphTest {
  @Test
  public void getTourWorks() {
    var testServer = ServerControllerTest.getFakeServer();
    var input = new ServerController(testServer, 1, 1, 2020, 80);
    var obstacleGraph = new ObstacleGraph(new Obstacles(input.getNoFlyZones()));
    var sensorGraph = new SensorGraph(input.getSensorLocations(), obstacleGraph, 0);

    assertEquals(35, sensorGraph.getTour(new Coords(-3.1878, 55.9444)).size());
  }
}
