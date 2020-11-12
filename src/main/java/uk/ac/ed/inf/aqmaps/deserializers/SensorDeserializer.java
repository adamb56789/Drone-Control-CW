package uk.ac.ed.inf.aqmaps.deserializers;

/**
 * Used only for deserializing the sensor information from JSON. The real Sensor class stores the
 * location as W3W instead of a String.
 */
@SuppressWarnings("unused") // It says fields are unused but they are still used by GSON
public class SensorDeserializer {
  private String location;
  private float battery;
  private String reading;

  public String getLocation() {
    return location;
  }

  public float getBattery() {
    return battery;
  }

  public String getReading() {
    return reading;
  }
}
