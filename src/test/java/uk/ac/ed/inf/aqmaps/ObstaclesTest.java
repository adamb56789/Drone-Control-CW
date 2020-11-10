package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Polygon;
import org.junit.Before;
import org.junit.Test;
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
    var points = obstacles.getPoints();
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
  public void middleOfNowhereLineNoCollision() {
    var start = new Coords(-3.190433, 55.945481);
    var end = new Coords(-3.189062, 55.94351);

    assertFalse(obstacles.collidesWith(start, end));
  }

  @Test
  public void nearBuildingsLineNoCollision() {
    var start = new Coords(-3.186153, 55.944796);
    var end = new Coords(-3.187708, 55.944453);

    assertFalse(obstacles.collidesWith(start, end));
  }

  @Test
  public void leavesConfinementLineCollides() {
    var start = new Coords(-3.187011, 55.942771);
    var end = new Coords(-3.187011, 55.942446);

    assertTrue(obstacles.collidesWith(start, end));
  }

  @Test
  public void collidesWith1BuildingLineCollides() {
    var start = new Coords(-3.186126, 55.943297);
    var end = new Coords(-3.186925, 55.943152);

    assertTrue(obstacles.collidesWith(start, end));
  }

  @Test
  public void trickyPathThroughBuildingLineCollides() {
    var start = new Coords(-3.186391, 55.944383);
    var end = new Coords(-3.18692, 55.944946);

    assertTrue(obstacles.collidesWith(start, end));
  }

  @Test
  public void collidesWith3BuildingsLineCollides() {
    var start = new Coords(-3.186324, 55.942891);
    var end = new Coords(-3.187494, 55.945703);

    assertTrue(obstacles.collidesWith(start, end));
  }

  @Test
  public void shortestRouteAroundBuildingLeavesConfinementLineCollides() {
    var start = new Coords(-3.189626, 55.942625);
    var end = new Coords(-3.188306, 55.942624);

    assertTrue(obstacles.collidesWith(start, end));
  }
}
