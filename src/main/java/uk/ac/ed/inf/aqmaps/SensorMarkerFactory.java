package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

/**
 * A factory which constructs sensor markers which displays the location, status and reading of a
 * sensor. This factory does not produce hypothetical SensorMarker instances, but Feature instances
 * instead, since Feature cannot be subclassed due to its lack of a public constructor.
 */
public class SensorMarkerFactory {

  /**
   * Creates a marker located at the position of this sensor. If a sensor reading was taken
   * successfully the marker is coloured and assigned a symbol based on the reading, and if it has
   * low battery or was not visited, assigns different symbols.
   *
   * @param w3w the location of the sensor as a W3W
   * @param sensor the Sensor containing the sensor data, or null if the sensor was not visited
   * @return a Feature containing a Point and various attributes describing the marker
   */
  public Feature getSensorMarker(W3W w3w, Sensor sensor) {
    if (sensor != null) {
      var battery = sensor.getBattery();

      if (battery < 10) {
        // If the battery is less than 10 then we do not display the unreliable sensor reading and
        // display a black cross instead
        return createPoint(w3w, "#000000", "cross");
      } else {
        // If the battery level is sufficient we create a marker that displays the pollution level
        // as a colour and a symbol
        double pollutionLevel = Double.parseDouble(sensor.getReading());
        return createPoint(w3w, getRgbString(pollutionLevel), getMarkerSymbol(pollutionLevel));
      }

    } else {
      // If the drone did not visit the sensor at this location we use a gray marker with no symbol
      return createPoint(w3w, "#aaaaaa");
    }
  }

  /**
   * Create a Feature with a position, colour, and marker symbol.
   *
   * @param w3w the W3W location of the sensor
   * @param rgbString the RGB colour string of the point
   * @param markerSymbol the marker symbol string
   * @return a Feature containing a Point
   */
  private Feature createPoint(W3W w3w, String rgbString, String markerSymbol) {
    var feature = createPoint(w3w, rgbString);
    feature.addStringProperty("marker-symbol", markerSymbol);
    return feature;
  }

  /**
   * Create a Feature containing a Point marker with a position, colour, and no marker symbol.
   *
   * @param w3w the W3W location of the sensor
   * @param rgbString the RGB colour string of the point
   * @return a Feature containing a Point
   */
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
   * @param pollutionLevel the pollution level as a double between 0 and 256
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
   * @param pollutionLevel the pollution level as a double between 0 and 256
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
