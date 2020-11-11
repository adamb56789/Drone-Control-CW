package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;
import uk.ac.ed.inf.aqmaps.geometry.Segment;
import uk.ac.ed.inf.aqmaps.io.ServerController;

import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class PolygonTest {
  private Obstacles obstacles;

  @Before
  public void setup() {
    var testServer = ServerControllerTest.getServer();
    var input = new ServerController(testServer, 1, 1, 2020, 80);
    obstacles = new Obstacles(input.getNoFlyZones());
  }

  @Test
  public void segmentListCorrect() {
    // Run the test for each of the polygons we have in the test mapbox
    var features = obstacles.getMapbox().features();
    for (int i = 0; i < features.size(); i++) {
      var polygon = new Polygon(obstacles.getMapbox().features().get(i));
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
    var features = obstacles.getMapbox().features();

    for (int i = 0; i < features.size(); i++) { // Run the test for all of the examples we have
      var polygon = new Polygon(obstacles.getMapbox().features().get(i));
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
