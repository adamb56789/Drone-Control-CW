package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

/** Holds the settings derived from the command line arguments. */
public class Settings {
  private final int day;
  private final int month;
  private final int year;
  private final Coords startCoords;
  private final int randomSeed;
  private final int port;
  private final double maxRunTime;

  /** @param args the input command line args */
  public Settings(String[] args) {
    this.day = Integer.parseInt(args[0]);
    this.month = Integer.parseInt(args[1]);
    this.year = Integer.parseInt(args[2]);
    this.startCoords = new Coords(Double.parseDouble(args[4]), Double.parseDouble(args[3]));
    this.randomSeed = Integer.parseInt(args[5]);
    this.port = Integer.parseInt(args[6]);

    if (args.length > 7) {
      maxRunTime = Double.parseDouble(args[7]);
    } else {
      maxRunTime = -1;
    }
  }

  /** @return the day to generate the map for */
  public int getDay() {
    return day;
  }

  /** @return the month to generate the map for */
  public int getMonth() {
    return month;
  }

  /** @return the year to generate the map for */
  public int getYear() {
    return year;
  }

  /** @return the starting coordinates of the drone */
  public Coords getStartCoords() {
    return startCoords;
  }

  /** @return the random seed to use in the algorithms */
  public int getRandomSeed() {
    return randomSeed;
  }

  /** @return the port number of the server */
  public int getPort() {
    return port;
  }

  /**
   * @return the maximum run time of the flight planner in seconds. If no value was provided and the
   *     default is to be used, is equal to -1. 0 means that the time limit is to be turned off.
   */
  public double getMaxRunTime() {
    return maxRunTime;
  }
}
