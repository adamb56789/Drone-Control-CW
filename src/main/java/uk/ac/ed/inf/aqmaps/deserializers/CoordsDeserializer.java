package uk.ac.ed.inf.aqmaps.deserializers;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

/**
 * Used for deserialization of Coords, this is needed since the field names in Coords inherit from
 * Point2D so @SerializedName can't be used to rename lng and lat to x and y
 */
public class CoordsDeserializer {
  private final double lng;
  private final double lat;

  public CoordsDeserializer(double lng, double lat) {
    this.lng = lng;
    this.lat = lat;
  }

  /** @return a Coords object */
  public Coords getCoords() {
    return new Coords(lng, lat);
  }
}
