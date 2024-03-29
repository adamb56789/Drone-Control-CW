package uk.ac.ed.inf.aqmaps.io;

import uk.ac.ed.inf.aqmaps.Sensor;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;

import java.util.List;

/**
 * Handles interaction with input from all remote information sources and devices. Gets information
 * on sensor locations, no-fly zones, W3W locations, and sensor readings. Implementations of the
 * interface can gather the information from any source, such as a simple web server for testing
 * purposes, or from a full system where data is also read from real sensors.
 */
public interface InputController {
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
  List<W3W> getSensorW3Ws();

  /**
   * Gets information about no-fly zones from a remote source
   *
   * @return a FeatureCollection containing the locations of the no-fly zones
   */
  List<Polygon> getNoFlyZones();
}
