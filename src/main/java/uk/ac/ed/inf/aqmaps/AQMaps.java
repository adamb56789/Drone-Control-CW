package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.io.FileOutputController;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;

/**
 * A program which does stuff This code follows the Google Java Style Guide at
 * https://google.github.io/styleguide/javaguide.html
 */
public class AQMaps {

  static final String[] TEST_ARGS = {"1", "1", "2020", "55.9444", "-3.1878", "0", "80"};

  /**
   * Does stuff
   *
   * @param args an array of arguments.
   */
  public static void main(String[] args) {
    var settings = new Settings(TEST_ARGS);
    var drone =
        new Drone(settings, new ServerInputController(settings), new FileOutputController(settings));
    drone.start();
  }
}
