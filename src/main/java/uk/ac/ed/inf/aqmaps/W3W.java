package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

/** Holds what3words coordinate and word information. */
public class W3W {
  private final Coords coordinates;
  private final String words;

  /**
   * @param coordinates the coordinates of the centre of the W3W square
   * @param words the 3 words
   */
  public W3W(Coords coordinates, String words) {
    this.coordinates = coordinates;
    this.words = words;
  }

  /** @return the coordinates of the centre of the W3W square */
  public Coords getCoordinates() {
    return coordinates;
  }

  /** @return words the 3 words */
  public String getWords() {
    return words;
  }
}
