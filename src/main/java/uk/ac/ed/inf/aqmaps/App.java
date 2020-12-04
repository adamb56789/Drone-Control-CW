package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.io.FileOutputController;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;

/**
 * Flies a drone around Edinburgh to collect air quality data from sensors and create a map. This
 * code follows the Google Java Style Guide at https://google.github.io/styleguide/javaguide.html
 */
public class App {

  /**
   * Main method
   *
   * @param args a list of arguments in the form: day month year latitude longitude randomSeed
   *     portNumber [timeLimit]
   */
  public static void main(String[] args) {
    if (args.length < 7) {
      System.out.println("Incorrect number of arguments: should be at least 7");
      System.exit(-1);
    }
    var settings = new Settings(args);

    System.out.printf(
        "Starting aqmaps for date %s/%s/%s and start position %s with random seed %d. Flight planning mode: %s.%n",
        settings.getDay(),
        settings.getMonth(),
        settings.getYear(),
        settings.getStartCoords(),
        settings.getRandomSeed(),
        settings.getMaxRunTime() == 0
            ? "fixed iteration count"
            : "stop after " + settings.getMaxRunTime() + " s");

    var inputController = new ServerInputController(settings);
    System.out.println("Finished getting data from server");
    var drone =
        new Drone(
            settings, inputController, new FileOutputController(settings));
    drone.start();
  }
}
