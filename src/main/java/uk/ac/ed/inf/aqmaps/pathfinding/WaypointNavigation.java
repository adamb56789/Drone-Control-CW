package uk.ac.ed.inf.aqmaps.pathfinding;

import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Angle;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class which handles the navigation of the drone along a series of waypoints. This is the core
 * of the drone control algorithm that plans the movement of the drone itself including all rules
 * about move lengths and directions.
 */
public class WaypointNavigation {
  public static final double MOVE_LENGTH = 0.0003;
  public static final double SENSOR_RANGE = 0.0002;
  public static final double END_POSITION_RANGE = 0.0003;
  private final ArrayList<Integer> offsets = getOffsets();
  private final Obstacles obstacles;
  private List<Coords> waypoints;
  private Coords targetLocation;
  private W3W targetSensorW3W;
  private int countIterations = 0;

  /** @param obstacles the obstacles for collision checking */
  public WaypointNavigation(Obstacles obstacles) {
    this.obstacles = obstacles;
  }

  /**
   * Find a sequence of moves that navigates the drone from the current location along the waypoints
   * to the target.
   *
   * @param startingPosition the starting position of the drone
   * @param waypoints a list of Coords waypoints for the drone to follow on its way to the target.
   * @param targetSensorW3W the W3W of the target sensor, or null if the target is not a sensor.
   * @return a list of Moves that navigate the drone from the starting position to in range of the
   *     target
   */
  public List<Move> navigateToLocation(
      Coords startingPosition, List<Coords> waypoints, W3W targetSensorW3W) {
    this.waypoints = waypoints;
    this.targetLocation = waypoints.get(waypoints.size() - 1);
    this.targetSensorW3W = targetSensorW3W;

    // Estimate the length of the path to first waypoint for checking loops (see moveToWaypoint())
    var maxLengthFirstMove = predictMaxMoveLength(startingPosition, waypoints.get(1));
    var moves = navigateAlongWaypoints(startingPosition, 1, maxLengthFirstMove);

    if (moves == null) {
      // This never occurred in testing, but if a flightpath can't be found, return null
      return null;
    }
    // This list is in reverse order because the algorithm builds it up starting at the end
    Collections.reverse(moves);
    return moves;
  }

  /**
   * Predict the maximum number of moves to reach the specified waypoint. The formula is
   * ceiling(distance / MOVE_LENGTH) + 4. Using less than 4 may work, but it is safer to be at least
   * this. 1 worked ok in testing, but may not in different situations
   *
   * @param startPos the starting position of the move
   * @param target the target waypoint of the move
   * @return the estimated maximum number of moves that it will take to reach the target
   */
  private int predictMaxMoveLength(Coords startPos, Coords target) {
    return (int) ((startPos.distance(target) / MOVE_LENGTH) + 1) + 4;
  }

  private List<Move> navigateAlongWaypoints(
      Coords currentPosition, int currentWaypointNumber, int movesTilTimeout) {
    // This flightpath is invalid if we timeout from taking too many moves, which happens if it gets
    // stuck in a loop not making any progress. This doesn't happen very often
    if (movesTilTimeout == 0) {
      return null;
    }

    if (countIterations++ > 1000000) {
      // Fail if the algorithm continues for too long TODO get rid of this
      System.out.println("Counter time out");
      return null;
    }

    // When looking for a move, start by going directly towards the next waypoint. This may fail if
    // we hit an obstacle, so we try again but in a new direction offset from the direct line. We
    // start with small offsets in both directions and work our way out.
    for (int offset : offsets) {
      // Calculate the direction towards the next waypoint (to the nearest 10)
      var direction =
          Angle.roundTo10Degrees(
              Angle.lineDirection(currentPosition, waypoints.get(currentWaypointNumber)));
      direction = Angle.formatAngle(direction + offset); // Apply the offset and ensure angle is [0,350]

      var positionAfterMove = currentPosition.getPositionAfterMoveDegrees(direction, MOVE_LENGTH);

      // If the move collides with an obstacle then try a different offset
      if (obstacles.lineCollision(currentPosition, positionAfterMove)) {
        continue;
      }

      // If our target waypoint is not the last then it is the corner of on obstacle so check if we
      // have line of sight to the next waypoint and have gone round the corner.
      if (currentWaypointNumber < waypoints.size() - 1
          && !obstacles.lineCollision(
              positionAfterMove, waypoints.get(currentWaypointNumber + 1))) {

        // Estimate the length of the journey to the next waypoint for checking loops
        var nextEstimatedLength =
            predictMaxMoveLength(positionAfterMove, waypoints.get(currentWaypointNumber + 1));
        // Recursive call to move to the next waypoints
        var movesList =
            navigateAlongWaypoints(positionAfterMove, ++currentWaypointNumber, nextEstimatedLength);

        if (movesList != null) {
          // Create a Move object for the calculated move, add it to the list which now contains all
          // moves to the target ahead of where we are now, and return it up the stack
          movesList.add(new Move(currentPosition, positionAfterMove, direction, null));
          return movesList;
        } else {
          // If we get a null that means it got stuck later on, so try a different offset
          continue;
        }
      }

      if (reachedTarget(positionAfterMove)) {
        // Create a list with just this final move, including the sensor we just reached, and return
        // it up the stack
        var movesList = new ArrayList<Move>();
        movesList.add(new Move(currentPosition, positionAfterMove, direction, targetSensorW3W));
        return movesList;
      }

      // If the move has not reached a waypoint or the target, and does not collide, then keep going
      // and recursively search for the next move
      var movesList =
          navigateAlongWaypoints(positionAfterMove, currentWaypointNumber, movesTilTimeout - 1);

      if (movesList != null) {
        // Create a Move object for the calculated move, add it to the list which now contains all
        // moves to the target ahead of where we are now, and return it up the stack
        movesList.add(new Move(currentPosition, positionAfterMove, direction, null));
        return movesList;
      }
      // If we get a null that means it got stuck later on, so try a different offset
    }
    return null;
  }

  private boolean reachedTarget(Coords positionAfterMove) {
    // The range is different if the target the end final end position
    if (targetSensorW3W == null) {
      return positionAfterMove.distance(targetLocation) < END_POSITION_RANGE;
    } else {
      // We use the distance to the location of the sensor here instead of the target location since
      // the target location might have been moved slightly to try to cut the corner (see
      // FlightPlanner)
      return positionAfterMove.distance(targetSensorW3W.getCoordinates()) < SENSOR_RANGE;
    }
  }

  private ArrayList<Integer> getOffsets() {
    var offsets = new ArrayList<Integer>();
    offsets.add(0);
    for (int i = 10; i <= 180; i += 10) {
      offsets.add(i);
      offsets.add(-i);
    }
    return offsets;
  }

}
