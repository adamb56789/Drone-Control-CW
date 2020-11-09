package uk.ac.ed.inf.aqmaps;

public class Settings {
  private final int day;
  private final int month;
  private final int year;
  private final Coords startCoords;
  private final int randomSeed;
  private final int port;

  public Settings(String[] args) {
    this(
        Integer.parseInt(args[0]),
        Integer.parseInt(args[1]),
        Integer.parseInt(args[2]),
        new Coords(Double.parseDouble(args[4]), Double.parseDouble(args[3])),
        Integer.parseInt(args[5]),
        Integer.parseInt(args[6]));
  }

  public Settings(int day, int month, int year, Coords startCoords, int randomSeed, int port) {
    this.day = day;
    this.month = month;
    this.year = year;
    this.startCoords = startCoords;
    this.randomSeed = randomSeed;
    this.port = port;
  }

  public int getDay() {
    return day;
  }

  public int getMonth() {
    return month;
  }

  public int getYear() {
    return year;
  }

  public Coords getStartCoords() {
    return startCoords;
  }

  public int getRandomSeed() {
    return randomSeed;
  }

  public int getPort() {
    return port;
  }
}
