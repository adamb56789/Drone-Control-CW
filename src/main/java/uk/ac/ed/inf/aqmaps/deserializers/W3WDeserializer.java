package uk.ac.ed.inf.aqmaps.deserializers;

import uk.ac.ed.inf.aqmaps.W3W;

/**
 * Used for deserialization of W3W, {@link CoordsDeserializer} for why this is necessary.
 */
public class W3WDeserializer {
  private final CoordsDeserializer coordinates;
  private final String words;

  public W3WDeserializer(CoordsDeserializer coordinates, String words) {
    this.coordinates = coordinates;
    this.words = words;
  }

  /** @return a W3W object */
  public W3W getW3W() {
    return new W3W(coordinates.getCoords(), words);
  }
}
