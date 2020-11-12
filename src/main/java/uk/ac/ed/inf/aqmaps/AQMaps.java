package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

/**
 * A program which does stuff This code follows the Google Java Style Guide at
 * https://google.github.io/styleguide/javaguide.html
 */
public class AQMaps {
  /** A Point representing the northwest corner of the confinement area. */
  static final Point NORTHWEST = Point.fromLngLat(-3.192473, 55.946233);
  /** A Point representing the southeast corner of the confinement area. */
  static final Point SOUTHEAST = Point.fromLngLat(-3.184319, 55.942617);
  /** The width of the confinement area in degrees. */
  static final double AREA_WIDTH = SOUTHEAST.longitude() - NORTHWEST.longitude();
  /**
   * The height of the confinement area in degrees. This is negative since we are moving southerly
   * in the northern hemisphere.
   */
  static final double AREA_HEIGHT = SOUTHEAST.latitude() - NORTHWEST.latitude();

  static final String[] TEST_ARGS = {"1", "1", "2020", "55.9444", "-3.1878", "0", "80"};

  /**
   * Does stuff
   *
   * @param args an array of arguments.
   */
  public static void main(String[] args) {
    Testing.test();
//    var settings = new Settings(TEST_ARGS);
//    var drone = new Drone(settings, new ServerController(settings), new OutputToFile());
//    long startTime = System.currentTimeMillis();
//    drone.start();
//    long endTime = System.currentTimeMillis();
//    System.out.printf("Took %d ms", endTime - startTime);
  }
}
