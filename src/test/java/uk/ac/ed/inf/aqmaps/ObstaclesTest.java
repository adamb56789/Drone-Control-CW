package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Polygon;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.ServerController;

import java.util.stream.Collectors;

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
    var points = obstacles.getAllPoints();
    assertEquals("The obstacle polygons should have a total of 36 vertices", 32, points.size());
  }

  @Test
  public void numberOfSegmentsCorrect() {
    var segments = obstacles.getLineSegments();
    assertEquals(
        "The obstacle polygons should have a total of 32 line segments", 32, segments.size());
  }

  @Test
  public void boundingBoxesCorrect() {
    var mapbox = obstacles.getMapbox();
    var boundingBoxes = obstacles.getBoundingBoxes();

    // Get a list of Points for each obstacle
    //noinspection ConstantConditions
    var pointsLists =
        mapbox.features().stream()
            .map(f -> ((Polygon) f.geometry()).coordinates().get(0))
            .collect(Collectors.toList());

    for (int i = 0; i < boundingBoxes.size(); i++) {
      // Convert the list of Points to list of Coords
      var coordsList =
          pointsLists.get(i).stream().map(Coords::fromMapboxPoint).collect(Collectors.toList());
      coordsList.remove(0); // remove the duplicate

      // Count the number of points that are in the bounding box
      int count = 0;
      for (var coords : coordsList) {
        if (boundingBoxes.get(i).contains(coords)) {
          count++;
        }
      }
      // Points on the very edge may not be considered to be in the box
      assertTrue(
          "At least all but the 2 most outer corners of an obstacle's points should be inside its bounding box",
          count >= coordsList.size() - 2);
    }
  }

  @Test
  public void meetsCornersLineNoCollision() {
    var start = obstacles.getAllPoints().get(0);
    var end = obstacles.getAllPoints().get(1);
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
    var line = TestPaths.COLLIDES_BUILDING;
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
