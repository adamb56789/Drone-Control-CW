package uk.ac.ed.inf.aqmaps.deserializers;

import uk.ac.ed.inf.aqmaps.W3W;

/** Used for deserialization of W3W, {@link CoordsDeserializer} for why this is necessary. */
@SuppressWarnings("unused") // It says fields are unused but they are still used by GSON
public class W3WDeserializer {
  private CoordsDeserializer coordinates;
  private String words;

  /** @return a W3W object */
  public W3W getW3W() {
    return new W3W(coordinates.getCoords(), words);
  }
}
