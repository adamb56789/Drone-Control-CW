package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.io.ServerController;

import static org.junit.Assert.*;

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
    var points = obstacles.getOutlinePoints();
    assertEquals("The obstacle polygons should have a total of 36 vertices", 32, points.size());
  }

  @Test
  public void numberOfSegmentsCorrect() {
    var segments = obstacles.getSegments();
    assertEquals(
        "The obstacle polygons should have a total of 32 line segments", 32, segments.size());
  }

  @Test
  public void meetsCornersLineNoCollision() {
    var start = obstacles.getOutlinePoints().get(0);
    var end = obstacles.getOutlinePoints().get(1);
    assertFalse(obstacles.collidesWith(start, end));
  }

  @Test
  public void middleOfNowhereLineNoCollision() {
    var line = TestPaths.MIDDLE_OF_NOWHERE;
    assertFalse(obstacles.collidesWith(line.start, line.end));
  }

  @Test
  public void nearBuildingsLineNoCollision() {
    var line = TestPaths.NEAR_BUILDINGS;
    assertFalse(obstacles.collidesWith(line.start, line.end));
  }

  @Test
  public void leavesConfinementLineCollides() {
    var line = TestPaths.LEAVES_CONFINEMENT;
    assertTrue(obstacles.collidesWith(line.start, line.end));
  }

  @Test
  public void collidesWith1BuildingLineCollides() {
    var line = TestPaths.COLLIDES_1_BUILDING;
    assertTrue(obstacles.collidesWith(line.start, line.end));
  }

  @Test
  public void trickyPathThroughBuildingLineCollides() {
    var line = TestPaths.TRICKY_PATH_THROUGH_BUILDINGS;
    assertTrue(obstacles.collidesWith(line.start, line.end));
  }

  @Test
  public void collidesWith3BuildingsLineCollides() {
    var line = TestPaths.COLLIDES_3_BUILDINGS;
    assertTrue(obstacles.collidesWith(line.start, line.end));
  }

  @Test
  public void shortestRouteLeavesConfinementLineCollides() {
    var line = TestPaths.SHORTEST_ROUTE_LEAVES_CONFINEMENT;
    assertTrue(obstacles.collidesWith(line.start, line.end));
  }
}
