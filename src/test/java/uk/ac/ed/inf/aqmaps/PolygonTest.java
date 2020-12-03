package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;
import uk.ac.ed.inf.aqmaps.noflyzone.Obstacles;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PolygonTest {
  private final Obstacles obstacles =
      new Obstacles(
          (new ServerInputController(ServerInputControllerTest.getFakeServer(), 1, 1, 2020, 80))
              .getNoFlyZones());
  private List<Polygon> noFlyZones;

  @Before
  public void setup() {
    var testServer = ServerInputControllerTest.getFakeServer();
    var input = new ServerInputController(testServer, 1, 1, 2020, 80);
    noFlyZones = input.getNoFlyZones();
  }

  @Test
  public void segmentListCorrect() {
    // Run the test for each of the polygons we have in the test geojson
    for (Polygon polygon : noFlyZones) {
      var segments = polygon.getSegments();
      assertEquals(
          "The polygon should have the same number of sides and vertices",
          polygon.getPoints().size(),
          segments.size());
      assertEquals(
          "The segments should wrap around correctly",
          segments.get(0).getP1(),
          segments.get(segments.size() - 1).getP2());
    }
  }

  @Test
  public void outlinePolygonCorrect() {
    for (var polygon : noFlyZones) { // Run the test for all of the examples we have
      var outlinePoints = polygon.generateOutlinePoints();

      for (int i = 0; i < outlinePoints.size(); i++) {
        assertEquals(
            "The points of the outline polygon should be 1e-14 from the original",
            Polygon.OUTLINE_MARGIN,
            polygon.getPoints().get(i).distance(outlinePoints.get(i)),
            1e-14);

        // If the point is not in confinement then it will always collide, so ignore it
        assertFalse(
            "The points of the outline polygon should not collide with an obstacle",
            obstacles.isInConfinement(outlinePoints.get(i))
                && obstacles.pointCollides(outlinePoints.get(i)));
      }
    }
  }

  // Polygon.lineCollision() is not tested directly since it is covered by Obstacles.lineCollision()
}
