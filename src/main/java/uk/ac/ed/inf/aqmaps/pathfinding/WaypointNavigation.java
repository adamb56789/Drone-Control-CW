package uk.ac.ed.inf.aqmaps.pathfinding;

import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaypointNavigation {
  private static final double MOVE_LENGTH = 0.0003;
  private static final double SENSOR_RANGE = 0.0002;
  private static final double END_POSITION_RANGE = 0.0002;
  private final Obstacles obstacles;
  private final List<Coords> waypoints;
  private final Coords targetLocation;
  private final boolean targetIsEnd;
  private final W3W targetSensorW3W;

  public WaypointNavigation(Obstacles obstacles, List<Coords> waypoints, W3W targetSensorW3W) {
    this.obstacles = obstacles;
    this.waypoints = waypoints;
    this.targetLocation = waypoints.get(waypoints.size() - 1);
    this.targetIsEnd = targetSensorW3W == null;
    this.targetSensorW3W = targetSensorW3W;
  }

  /**
   * Find a sequence of moves that navigates the drone from the current location along the
   * waypoints.
   *
   * @param currentPosition the current position of the drone
   * @return a list of Moves, or null if there is no valid flightpath
   */
  public List<Move> navigateToLocation(Coords currentPosition) {
    var moves = tryMove(currentPosition, 1, predictMaxMoveLength(0));
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
   * this.
   *
   * @param i the waypoint number.
   * @return the estimated maximum number of moves that it will take to go from waypoint i to the
   *     next
   */
  private int predictMaxMoveLength(int i) {
    return (int) ((waypoints.get(i).distance(waypoints.get(i + 1)) / MOVE_LENGTH) + 1) + 4;
  }

  private List<Move> tryMove(
      Coords currentPosition, int currentWaypointNumber, int movesTilTimeout) {
    // This flightpath is invalid if we timeout from taking too many moves
    // This happens if it gets stuck in a loop not making any progress
    // This happens rarely
    if (movesTilTimeout == 0) {
      return null;
    }

    var offsets =
        new int[] {
          0, 10, -10, 20, -20, 30, -30, 40, -40, 50, -50, 60, -60, 70, -70, 80, -80, 90, -90, 100,
          -100, 110, -110, 120, -120, 130, -130, 140, -140, 150, -150, 160, -160, 170, -170, 180
        };
    for (int offset : offsets) {
      // Calculate the direction towards the next waypoint (to the nearest 10)
      var direction =
          radiansToRoundedDegrees(currentPosition.angleTo(waypoints.get(currentWaypointNumber)));
      direction = formatAngle(direction + offset); // Apply the offset

      // Calculate the position at the end of the move
      var afterPosition = currentPosition.getPositionAfterMoveDegrees(direction, MOVE_LENGTH);

      // If the move collides with an obstacle then try a different offset
      if (obstacles.collidesWith(currentPosition, afterPosition)) {
        continue;
      }

      // If our target is a waypoint for obstacle evasion, instead of being within a certain
      // radius we must ensure that we have gone round the corner and the next waypoint is in
      // sight
      if (currentWaypointNumber < waypoints.size() - 1
          && !obstacles.collidesWith(afterPosition, waypoints.get(currentWaypointNumber + 1))) {

        // Don't bother recursing again if it will timeout and return null here instead (efficiency)
        if (movesTilTimeout == 1) {
          return null;
        }

        var nextEstimatedLength = predictMaxMoveLength(currentWaypointNumber);

        var returnValue = tryMove(afterPosition, ++currentWaypointNumber, nextEstimatedLength);

        if (returnValue == null) {
          // If we get a null that means it got stuck later on, so try a different offset
          continue;
        } else {
          returnValue.add(new Move(currentPosition, afterPosition, direction, null));
          return returnValue;
        }
      }

      // Check if the move puts it in range of the target.
      // If our target is the end position then we use a different range.
      if (targetIsEnd && afterPosition.distance(targetLocation) < END_POSITION_RANGE
          || afterPosition.distance(targetLocation) < SENSOR_RANGE) {
        // Create a list with just this final move, including the sensor we just reached, and return
        // it up the stack
        var returnList = new ArrayList<Move>();
        returnList.add(new Move(currentPosition, afterPosition, direction, targetSensorW3W));
        return returnList;
      }

      // Don't bother recursing again if it will timeout and return null here instead (efficiency)
      if (movesTilTimeout == 1) {
        return null;
      }

      // If the move has not reached a waypoint or the target, and does not collide, then keep going
      // and search for the next move
      var returnValue = tryMove(afterPosition, currentWaypointNumber, movesTilTimeout - 1);

      if (returnValue == null) {
        // If we get a null that means it got stuck later on, so try a different offset
        // (continue is technically unnecessary but keeping it for readability)
        // noinspection UnnecessaryContinue
        continue;
      } else {
        returnValue.add(new Move(currentPosition, afterPosition, direction, null));
        return returnValue;
      }
    }
    return null;
  }

  /**
   * Convert an angle in radians (that ranges from -pi to pi) to the angle in degrees to the nearest
   * 10, ranging from 0 to 350
   */
  private int radiansToRoundedDegrees(double angle) {
    angle = Math.toDegrees(angle);
    int degrees = (int) (Math.round(angle / 10.0) * 10);

    return formatAngle(degrees);
  }

  private int formatAngle(int degrees) {
    // If the angle is negative, rotate it around one revolution so that it no longer is
    if (degrees < 0) {
      degrees += 360;
    } else if (degrees > 350) {
      degrees -= 360;
    }
    return degrees;
  }
}
