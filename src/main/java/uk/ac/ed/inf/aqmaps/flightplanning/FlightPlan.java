package uk.ac.ed.inf.aqmaps.flightplanning;

import uk.ac.ed.inf.aqmaps.Move;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A wrapper class for a flight plan as a list of moves, and the random seed that was used to
 * generate it. This is needed so that results can be sorted by seed before finding the minimum,
 * allowing for consistent operation even when concurrency is used.
 */
public class FlightPlan {
  private final int seed;
  private final List<Move> moves;

  /**
   * @param seed the random seed that his flight plan used
   * @param moves the list of move which makes up the flight plan
   */
  public FlightPlan(int seed, List<Move> moves) {
    this.seed = seed;
    this.moves = moves;
  }

  /**
   * Get the list of moves in the flight plan, limited to a maximum of 150 moves. In testing with
   * the current possible input values, this never came close actually limiting the number of moves.
   *
   * @return a list of moves of length <= 150
   */
  public List<Move> getMovesWithLimit() {
    return moves.stream().limit(150).collect(Collectors.toList());
  }

  public int getSeed() {
    return seed;
  }

  public List<Move> getMoves() {
    return moves;
  }
}
