package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.List;
import java.util.stream.Collectors;

/** Holds What3words coordinate and word information. */
public class W3W {
  private final Coords coordinates;
  private final String words;

  public W3W(Coords coordinates, String words) {
    this.coordinates = coordinates;
    this.words = words;
  }

  /**
   * Converts a list of W3W to a list of Coords.
   *
   * @param w3ws the W3Ws
   * @return a list of the corresponding Coords
   */
  public static List<Coords> convertToCoords(List<W3W> w3ws) {
    return w3ws.stream().map(W3W::getCoordinates).collect(Collectors.toList());
  }

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
