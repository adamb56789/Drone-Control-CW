package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.FeatureCollection;

import java.util.List;

/**
 * Handles interaction with all remote information sources and devices. Gets information on sensor
 * locations, no-fly zones, W3W locations, and sensor readings. Implementations of the interface can
 * gather the information from any source, such as a simple web server for testing purposes, or from
 * a full system where data is also read from real sensors.
 */
public interface Remote {
  /**
   * Reads information from the sensor at the provided W3W location
   *
   * @param location the location of the sensor as a W3W class
   * @return a Sensor object representing the current status of the sensor
   */
  Sensor readSensor(W3W location);

  /**
   * Gets the list of sensors that need to be visited from a remote source
   *
   * @return a list of W3W locations of the sensors
   */
  List<W3W> getSensorLocations();

  /**
   * Gets information about no-fly zones from a remote source
   *
   * @return a FeatureCollection containing the locations of the no-fly zones
   */
  FeatureCollection getNoFlyZones();
}
