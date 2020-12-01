package uk.ac.ed.inf.aqmaps;

/** A sensor with a battery level and reading. */
public class Sensor {
  private final W3W location;
  private final float battery;
  private final String reading;

  /**
   * Constructor
   *
   * @param w3wLocation the W3W location of the sensor
   * @param battery the current battery level of the sensor, as a percentage
   * @param reading the reading of the sensor as a String
   */
  public Sensor(W3W w3wLocation, float battery, String reading) {
    this.location = w3wLocation;
    this.battery = battery;
    this.reading = reading;
  }

  /** @return the W3W location of this sensor */
  public W3W getLocation() {
    return location;
  }

  /** @return the battery level of this sensor as a percentage */
  public float getBattery() {
    return battery;
  }

  /**
   * @return the reading of the sensor, as a String. If the battery level is 10% or greater this
   *     should contain a float value, but if it is less than 10% the reading cannot be trusted and
   *     may be incorrect, null or NaN.
   */
  public String getReading() {
    return reading;
  }
}
