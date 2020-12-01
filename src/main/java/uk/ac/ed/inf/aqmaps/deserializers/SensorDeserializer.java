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

  /** @return the location of the sensor as a W3W string */
  public String getLocation() {
    return location;
  }

  /** @return the battery level of this sensor as a percentage */
  public float getBattery() {
    return battery;
  }

  /** @return the reading of the sensor, as a String */
  public String getReading() {
    return reading;
  }
}
