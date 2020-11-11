package uk.ac.ed.inf.aqmaps;

/** Holds various lines for testing pathfinding. The shortest paths were found manually. */
public class TestPaths {

  /** Shortest path is a straight line */
  public static final TestPath MIDDLE_OF_NOWHERE =
      new TestPath(-3.190433, 55.945481, -3.189062, 55.94351, 0.002400933568425);

  /** Shortest path is a straight line, but is within the bounding boxes of buildings */
  public static final TestPath NEAR_BUILDINGS =
      new TestPath(-3.186153, 55.944796, -3.187708, 55.944453, 0.001592379979778);

  /** Path leaves confinement so there is no valid path */
  public static final TestPath LEAVES_CONFINEMENT =
      new TestPath(-3.187011, 55.942771, -3.187011, 55.942446, 0);

  /** Path around a single rectangular building of 3 segments */
  public static final TestPath COLLIDES_BUILDING =
      new TestPath(-3.186126, 55.943397, -3.186925, 55.943252, 0.000825924945475);

  /** Tricky path around buildings involving small cutouts of buildings, of 4 segments */
  public static final TestPath TRICKY_PATH_THROUGH_BUILDINGS =
      new TestPath(-3.186391, 55.944383, -3.18692, 55.944946, 0.001039992986219);

  /** A path which must evade 3 buildings, 4 segments */
  public static final TestPath COLLIDES_3_BUILDINGS =
      new TestPath(-3.186324, 55.942891, -3.187494, 55.945703, 0.003380847400841);

  /** A path where the shortest route would leave the confinement area, 3 segments */
  public static final TestPath SHORTEST_ROUTE_LEAVES_CONFINEMENT =
      new TestPath(-3.189626, 55.942625, -3.188306, 55.942624, 0.001837400150209);
}
