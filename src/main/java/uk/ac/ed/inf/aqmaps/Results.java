package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.util.*;
import java.util.stream.Collectors;

/** Holds and processes the calculated flightpath and collected sensor data */
public class Results {
  // This is a map since we need to know which planned sensors haven't been visited. It allows us to
  // easily scan through the planned locations and get the associated sensor if there is one.
  private final Map<W3W, Sensor> sensorsVisited = new HashMap<>();
  private final List<W3W> locationsPlanned;
  private List<Move> flightpath;

  /** @param locationsPlanned a list of sensor locations that have a planned visit today */
  public Results(List<W3W> locationsPlanned) {
    this.locationsPlanned = locationsPlanned;
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

  public String getMapGeoJSON() {
    var features = new ArrayList<Feature>();
    features.add(createFlightpathLineString());
    features.addAll(createSensorPoints());
    return FeatureCollection.fromFeatures(features).toJson();
  }

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

  private List<Feature> createSensorPoints() {
    return locationsPlanned.stream().map(this::createSensorMarker).collect(Collectors.toList());
  }

  private Feature createSensorMarker(W3W w3w) {
    var sensor = Optional.ofNullable(sensorsVisited.get(w3w));

    if (sensor.isPresent()) {
      var battery = sensor.get().getBattery();

      if (battery < 10) {
        // If the battery is less than 10 then we do not display the unreliable sensor reading and
        // display a black cross instead
        return createPoint(w3w, "#000000", "cross");
      } else {
        // If the battery level is sufficient we create a marker that displays the pollution level
        // as a colour and a symbol
        double pollutionLevel = Double.parseDouble(sensor.get().getReading());
        return createPoint(w3w, getRgbString(pollutionLevel), getMarkerSymbol(pollutionLevel));
      }

    } else {
      // If the drone did not visit the sensor at this location we use a gray marker with no symbol
      return createPoint(w3w, "#aaaaaa");
    }
  }

  /** Create a point with its position, colour, and marker symbol */
  private Feature createPoint(W3W w3w, String rgbString, String markerSymbol) {
    var feature = createPoint(w3w, rgbString);
    feature.addStringProperty("marker-symbol", markerSymbol);
    return feature;
  }

  /** Create a point with its position, colour, and no marker symbol */
  private Feature createPoint(W3W w3w, String rgbString) {
    var point = Point.fromLngLat(w3w.getCoordinates().x, w3w.getCoordinates().y);
    var feature = Feature.fromGeometry(point);
    feature.addStringProperty("location", w3w.getWords());
    feature.addStringProperty("rgb-string", rgbString);
    feature.addStringProperty("marker-color", rgbString);
    return feature;
  }

  /**
   * Converts a pollution level to a corresponding RGB string.
   *
   * @return the rgb string #xxxxxx of the colour representing the pollution level
   */
  private String getRgbString(double pollutionLevel) {
    if (0 <= pollutionLevel && pollutionLevel < 32) {
      return "#00ff00";
    } else if (32 <= pollutionLevel && pollutionLevel < 64) {
      return "#40ff00";
    } else if (64 <= pollutionLevel && pollutionLevel < 96) {
      return "#80ff00";
    } else if (96 <= pollutionLevel && pollutionLevel < 128) {
      return "#c0ff00";
    } else if (128 <= pollutionLevel && pollutionLevel < 160) {
      return "#ffc000";
    } else if (160 <= pollutionLevel && pollutionLevel < 192) {
      return "#ff8000";
    } else if (192 <= pollutionLevel && pollutionLevel < 224) {
      return "#ff4000";
    } else if (224 <= pollutionLevel && pollutionLevel < 256) {
      return "#ff0000";
    } else {
      throw new IllegalArgumentException("pollution level not in range");
    }
  }

  /**
   * Gets the marker symbol string for the given pollution level
   *
   * @return a String describing the marker symbol
   */
  private String getMarkerSymbol(double pollutionLevel) {
    if (pollutionLevel < 128) {
      return "lighthouse";
    } else {
      return "danger";
    }
  }
}
