package uk.ac.ed.inf.aqmaps;

import java.util.List;

/**
 * Handles interaction with all output locations. Implementations may output to any source, such as
 * to a file or to a server.
 */
public interface OutputController {

  /**
   * Outputs the flightpath planned by the drone
   * @param moves a list of Moves
   */
  void flightpath(List<Move> moves);

  /**
   * Outputs the sensor readings collected by the drone
   * @param moves a list of Moves
   * @param sensors a list of Sensors containing readings collected by the drone
   */
  void readings(List<Move> moves, List<Sensor> sensors);
}
