package uk.ac.ed.inf.aqmaps;

/**
 * Used only for deserializing the sensor information from JSON. The real Sensor class stores the
 * location as W3W instead of a String.
 */
public class SensorDeserializer {
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
