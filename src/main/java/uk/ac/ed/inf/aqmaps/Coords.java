package uk.ac.ed.inf.aqmaps;

/**
 * Holds a longitude and latitude pair. Values are accessed directly instead of through a getter,
 * this is a deliberate choice for ease of use.
 */
public class Coords {
  public double lng;
  public double lat;

  public Coords(double lng, double lat) {
    this.lng = lng;
    this.lat = lat;
  }

  @Override
  public String toString() {
    return "(" + lng + ", " + lat + ")";
  }
}
