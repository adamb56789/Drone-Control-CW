package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class ResultsTest {

  @Test
  public void flightpathStringCorrect() {
    var w3w = new W3W(new Coords(0, 0), "seiso.yubi.yabai");
    var move1 =
        new Move(TestPaths.MIDDLE_OF_NOWHERE.start, TestPaths.MIDDLE_OF_NOWHERE.end, 110, w3w);
    var move2 = new Move(TestPaths.NEAR_BUILDINGS.start, TestPaths.NEAR_BUILDINGS.end, 0, null);

    var results = new Results(List.of(w3w));
    results.recordFlightpath(List.of(move1, move2));

    assertEquals(
        "1,-3.190433,55.945481,110,-3.189062,55.94351,seiso.yubi.yabai\n"
            + "2,-3.186153,55.944796,0,-3.187708,55.944453,null\n",
        results.getFlightpathString());
  }

  // Helper methods for avoiding repetition in tests of various sensor states
  private Results createResultsWithSensors(
      float battery1,
      String reading1,
      float battery2,
      String reading2,
      boolean addUnvisitedSensor) {
    var sensor1Location = new W3W(new Coords(-3.189062, 55.94351), "seiso.yubi.yabai");
    var sensor2Location = new W3W(new Coords(-3.187708, 55.944453), "zero.ichi.ni");

    var move1 =
        new Move(
            TestPaths.MIDDLE_OF_NOWHERE.start,
            TestPaths.MIDDLE_OF_NOWHERE.end,
            110,
            sensor1Location);
    var move2 =
        new Move(TestPaths.MIDDLE_OF_NOWHERE.end, TestPaths.NEAR_BUILDINGS.start, 300, null);
    var move3 =
        new Move(TestPaths.NEAR_BUILDINGS.start, TestPaths.NEAR_BUILDINGS.end, 60, sensor2Location);

    var sensor1 = new Sensor(sensor1Location, battery1, reading1);
    var sensor2 = new Sensor(sensor2Location, battery2, reading2);

    Results results;
    if (addUnvisitedSensor) {
      // For testing that the results method puts unvisited sensors onto the map
      var sensor3Location = new W3W(new Coords(-3.189, 55.943), "a.b.c");
      results = new Results(List.of(sensor1Location, sensor2Location, sensor3Location));
    } else {
      results = new Results(List.of(sensor1Location, sensor2Location));
    }

    results.recordFlightpath(List.of(move1, move2, move3));
    results.recordSensorReading(sensor1);
    results.recordSensorReading(sensor2);
    return results;
  }

  private void assertPropertyCorrect(Feature f, String propertyName, String expected) {
    assertEquals(propertyName + " correct", expected, f.getStringProperty(propertyName));
  }

  @Test
  public void mapGeoJSONFlightpathLineCorrect() {
    var results = createResultsWithSensors(90, "42", 50, "50", false);
    var json = results.getMapGeoJSON();
    var map = FeatureCollection.fromJson(json);

    var lineStringOptional =
        map.features().stream()
            .map(Feature::geometry)
            .filter(g -> g.type().equals("LineString"))
            .findAny();
    assertTrue("The map should have a LineString", lineStringOptional.isPresent());

    var lineString = (LineString) lineStringOptional.get();
    assertEquals("The LineString should have 4 coordinates", 4, lineString.coordinates().size());
  }

  @Test
  public void mapGeoJSONGoodReadingsCorrect() {
    var results = createResultsWithSensors(90, "42", 50, "250", false);
    var json = results.getMapGeoJSON();
    var map = FeatureCollection.fromJson(json);

    var features = map.features();
    assertEquals("Map should have 3 features - 1 line string and 2 points", 3, features.size());

    var pointFeatures =
        features.stream()
            .filter(f -> f.geometry().type().equals("Point"))
            .collect(Collectors.toList());

    assertEquals("The map should have 2 points", 2, pointFeatures.size());

    var points =
        pointFeatures.stream()
            .map(Feature::geometry)
            .map(g -> (Point) g)
            .collect(Collectors.toList());

    assertEquals("Point longitude", -3.189062, points.get(0).longitude(), 1e-14);
    assertEquals("Point latitude", 55.94351, points.get(0).latitude(), 1e-14);

    assertPropertyCorrect(pointFeatures.get(0), "location", "seiso.yubi.yabai");
    assertPropertyCorrect(pointFeatures.get(0), "rgb-string", "#40ff00");
    assertPropertyCorrect(pointFeatures.get(0), "marker-color", "#40ff00");
    assertPropertyCorrect(pointFeatures.get(0), "marker-symbol", "lighthouse");

    assertPropertyCorrect(pointFeatures.get(1), "location", "zero.ichi.ni");
    assertPropertyCorrect(pointFeatures.get(1), "rgb-string", "#ff0000");
    assertPropertyCorrect(pointFeatures.get(1), "marker-color", "#ff0000");
    assertPropertyCorrect(pointFeatures.get(1), "marker-symbol", "danger");
  }

  @Test
  public void mapGeoJSONBadReadingsCorrect() {
    // "If the battery has less than 10% charge then [ignore the reading]" so exactly 10 should not
    // be ignored
    var results = createResultsWithSensors(10, "150", 9.9f, "NaN", true);

    var json = results.getMapGeoJSON();
    var map = FeatureCollection.fromJson(json);

    var pointFeatures =
        map.features().stream()
            .filter(f -> f.geometry().type().equals("Point"))
            .collect(Collectors.toList());

    assertEquals("The map should have 3 points", 3, pointFeatures.size());

    assertPropertyCorrect(pointFeatures.get(0), "rgb-string", "#ffc000");
    assertPropertyCorrect(pointFeatures.get(0), "marker-symbol", "danger");

    assertPropertyCorrect(pointFeatures.get(1), "rgb-string", "#000000");
    assertPropertyCorrect(pointFeatures.get(1), "marker-symbol", "cross");

    assertPropertyCorrect(pointFeatures.get(2), "rgb-string", "#aaaaaa");
    assertNull(
        "Unvisited point should have no marker-symbol",
        pointFeatures.get(2).getProperty("marker-symbol"));
  }
}
