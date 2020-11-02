package uk.ac.ed.inf.aqmaps;

/** Holds What3words coordinate and word information. */
public class W3W {
  private Coords coordinates;
  private String words;

  public Coords getCoordinates() {
    return coordinates;
  }

  public String getWords() {
    return words;
  }

  @Override
  public String toString() {
    return words + ' ' + coordinates;
  }
}
