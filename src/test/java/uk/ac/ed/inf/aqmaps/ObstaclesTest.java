package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObstaclesTest {
  private Obstacles obstacles;

  @Before
  public void setup() {
    var testServer = ServerControllerTest.getServer();
    var input = new ServerController(testServer, 1, 1, 2020, 80);
    obstacles = new Obstacles(input.getNoFlyZones());
  }

  @Test
  public void numberOfPointsCorrect() {
    var points = obstacles.getPoints();
    assertEquals("The obstacle polygons should have a total of 36 vertices", 36, points.size());
  }

  @Test
  public void numberOfSegmentsCorrect() {
    var segments = obstacles.getLineSegments();
    assertEquals("The obstacle polygons should have a total of 32 line segments", 32, segments.size());
  }
}
