package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PolygonTest {
  private List<Polygon> noFlyZones;

  @Before
  public void setup() {
    var testServer = ServerInputControllerTest.getFakeServer();
    var input = new ServerInputController(testServer, 1, 1, 2020, 80);
    noFlyZones = input.getNoFlyZones();
  }

  @Test
  public void segmentListCorrect() {
    // Run the test for each of the polygons we have in the test mapbox
    for (Polygon polygon : noFlyZones) {
      var segments = polygon.getSegments();
      assertEquals(
          "The polygon should have the same number of sides and vertices",
          polygon.getPoints().size(),
          segments.size());
      assertEquals(
          "The segments should wrap around correctly",
          segments.get(0).x1,
          segments.get(segments.size() - 1).x2,
          1e-10);
    }
  }

  @Test
  public void outlinePolygonCorrect() {
    for (Polygon polygon : noFlyZones) { // Run the test for all of the examples we have
      var outline = polygon.generateOutline();

      for (int j = 0; j < polygon.getPoints().size(); j++) {
        assertEquals(
            "The points of the outline polygon should be 1e-13 from the original",
            Polygon.OUTLINE_MARGIN,
            polygon.getPoints().get(j).distance(outline.getPoints().get(j)),
            1e-14);
      }

      for (var l1 : polygon.getSegments()) {
        for (var l2 : outline.getSegments()) {
          assertFalse(
              "The segments of the outline polygon should not intercept with the original",
              l1.intersectsLine(l2));
        }
      }
    }
  }
}
