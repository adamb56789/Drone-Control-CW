package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

/** Holds and processes the calculated flightpath and collected sensor data */
public class Results {
  private final List<Sensor> sensors = new ArrayList<>();
  private List<Move> flightpath;

  /**
   * Adds a calculated flight to the results
   *
   * @param flightpath a list of Moves representing the flightpath
   */
  public void addFlightpath(List<Move> flightpath) {
    this.flightpath = flightpath;
  }

  /**
   * Add a sensor with its readings to the results
   *
   * @param sensor the Sensor
   */
  public void addSensorReading(Sensor sensor) {
    sensors.add(sensor);
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
    return "";
  }
}
