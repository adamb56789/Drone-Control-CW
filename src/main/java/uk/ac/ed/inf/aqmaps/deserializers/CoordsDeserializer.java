package uk.ac.ed.inf.aqmaps.deserializers;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

/**
 * Used for deserialization of Coords, this is needed since the field names in Coords inherit from
 * Point2D so @SerializedName can't be used to rename lng and lat to x and y
 */
@SuppressWarnings("unused") // It says fields are unused but they are still used by GSON
public class CoordsDeserializer {
  private double lng;
  private double lat;

  /** @return a Coords object */
  public Coords getCoords() {
    return new Coords(lng, lat);
  }
}
