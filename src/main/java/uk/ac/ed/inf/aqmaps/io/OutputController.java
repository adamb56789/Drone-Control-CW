package uk.ac.ed.inf.aqmaps.io;

/**
 * Handles interaction with all output locations. Implementations may output to any source, such as
 * to a file or to a server.
 */
public interface OutputController {

  /**
   * Outputs the flightpath planned by the drone
   *
   * @param flightpathText the String containing the flightpath data
   */
  void outputFlightpath(String flightpathText);

  /**
   * Outputs the GeoJSON map containing the flightpath and the sensor readings collected by the
   * drone
   *
   * @param json the GeoJSON String
   */
  void outputMapGeoJSON(String json);
}
