package uk.ac.ed.inf.aqmaps;

/** Holds What3words coordinate and word information. */
public class W3W {
  private final Coords coordinates;
  private final String words;

  public Coords getCoordinates() {
    return coordinates;
  }

  public String getWords() {
    return words;
  }

  public W3W(Coords coordinates, String words) {
    this.coordinates = coordinates;
    this.words = words;
  }

  @Override
  public String toString() {
    return words + ' ' + coordinates;
  }
}
