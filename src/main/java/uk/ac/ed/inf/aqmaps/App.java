package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.io.FileOutputController;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;

/**
 * Flies a drone around Edinburgh to collect air quality data from sensors and create a map. This
 * code follows the Google Java Style Guide at https://google.github.io/styleguide/javaguide.html
 */
public class App {

  public static void main(String[] args) {
    (new Testing()).run();
//    if (args.length != 7) {
//      System.out.println("Incorrect number of arguments: should be 7");
//    }
//    var settings = new Settings(args);
//    var drone =
//        new Drone(
//            settings, new ServerInputController(settings), new FileOutputController(settings));
//    drone.start();
  }
}
