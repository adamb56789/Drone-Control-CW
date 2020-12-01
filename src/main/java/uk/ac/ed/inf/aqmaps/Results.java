package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.util.*;
import java.util.stream.Collectors;

/** Holds and processes the calculated flightpath and collected sensor data */
public class Results {

  /** A map from W3W sensor locations to their corresponding Sensor data. */
  private final Map<W3W, Sensor> sensorsVisited = new HashMap<>();

  /** A list of the W3W of the sensors that are planned to be visited. */
  private final List<W3W> sensorW3Ws;

  /** The planned flightpath of the drone as a list of Moves */
  private List<Move> flightpath;

  /**
   * Constructor
   *
   * @param sensorW3Ws a list of sensor locations as W3W that the drone is visiting
   */
  public Results(List<W3W> sensorW3Ws) {
    this.sensorW3Ws = sensorW3Ws;
  }

  /**
   * Adds a calculated flight to the results
   *
   * @param flightpath a list of Moves representing the flightpath
   */
  public void recordFlightpath(List<Move> flightpath) {
    this.flightpath = flightpath;
  }

  /**
   * Add a sensor with its readings to the results
   *
   * @param sensor the Sensor
   */
  public void recordSensorReading(Sensor sensor) {
    sensorsVisited.put(sensor.getLocation(), sensor);
  }

  /**
   * Gets a flightpath String of the following format:
   * 1,[startLng],[startLat],[angle],[endLng],[endLat],[sensor w3w or null]\n
   * 2,[startLng],[startLat],[angle],[endLng],[endLat],[sensor w3w or null]\n ...
   *
   * @return the flightpath String
   */
  public String getFlightpathString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int moveNumber = 1; moveNumber <= flightpath.size(); moveNumber++) {
      stringBuilder.append(
          String.format("%d,%s\n", moveNumber, flightpath.get(moveNumber - 1).toString()));
    }
    return stringBuilder.toString();
  }

  /**
   * Creates a GeoJSON string of a map which displays the flightpath of the drone and markers
   * displaying the readings or status of the sensors.
   *
   * @return a String of the GeoJSON
   */
  public String getMapGeoJSON() {
    Objects.requireNonNull(flightpath, "cannot create a map without a flightpath");

    // Create a list of features and add the line string and all of the markers
    var features = new ArrayList<Feature>();
    features.add(createFlightpathLineString());
    features.addAll(createSensorMarkers());

    // Use mapbox.geojson to create the JSON string
    return FeatureCollection.fromFeatures(features).toJson();
  }

  /**
   * Creates a GeoJSON LineString which shows the path that shows the path taken by the drone.
   *
   * @return a Feature containing the LineString
   */
  private Feature createFlightpathLineString() {
    // Insert the position before each move into the list
    var pointList =
        flightpath.stream()
            .map(move -> Point.fromLngLat(move.getBefore().x, move.getBefore().y))
            .collect(Collectors.toList());

    // Manually add the final position at the end of the last move
    var finalPosition = flightpath.get(flightpath.size() - 1).getAfter();
    pointList.add(Point.fromLngLat(finalPosition.x, finalPosition.y));

    return Feature.fromGeometry(LineString.fromLngLats(pointList));
  }

  /**
   * Creates Features containing Points which mark the position and reading/status of the sensors.
   *
   * @return a List of Features
   */
  private List<Feature> createSensorMarkers() {
    var markerFactory = new SensorMarkerFactory();
    return sensorW3Ws.stream()
        .map(w3w -> markerFactory.getSensorMarker(w3w, sensorsVisited.get(w3w)))
        .collect(Collectors.toList());
  }
}
