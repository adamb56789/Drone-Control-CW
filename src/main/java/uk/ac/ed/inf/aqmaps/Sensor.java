package uk.ac.ed.inf.aqmaps;

/** Contains information about a sensor. */
public class Sensor {
  private String location;
  private Double battery;
  private String reading;

  public String getLocation() {
    return location;
  }

  public Double getBattery() {
    return battery;
  }

  public String getReading() {
    return reading;
  }
}
