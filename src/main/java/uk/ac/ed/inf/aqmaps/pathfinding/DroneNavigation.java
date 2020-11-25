package uk.ac.ed.inf.aqmaps.pathfinding;

import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DroneNavigation {
  private static final double MOVE_LENGTH = 0.0003;
  private static final double SENSOR_RANGE = 0.0002;
  private static final double END_POSITION_RANGE = 0.0002;
  private final Obstacles obstacles;
  private final Map<Coords, W3W> sensorCoordsW3WMap;

  public DroneNavigation(Obstacles obstacles, List<W3W> sensorLocations) {
    this.obstacles = obstacles;
    sensorCoordsW3WMap = new HashMap<>();
    sensorLocations.forEach(w3w -> sensorCoordsW3WMap.put(w3w.getCoordinates(), w3w));
  }

  public List<Move> createFlightPlan(List<List<Coords>> tour) {
    log(tour.stream().flatMap(List::stream).collect(Collectors.toList()));

    var moves = new ArrayList<Move>();
    var currentPosition = tour.get(0).get(0);
    for (int i = 0; i < tour.size(); i++) {
      //      currentPosition =
      //          moveToNextLocation(currentPosition, tour.get(i), moves, i == tour.size() - 1);
      var waypoints = tour.get(i);
      currentPosition =
          recursiveStep(
              waypoints,
              moves,
              waypoints.get(waypoints.size() - 1),
              i == tour.size() - 1,
              currentPosition,
              radiansToRoundedDegrees(currentPosition.angleTo(waypoints.get(1))),
              1,
              0);
    }
    log(moves.size());
    return moves;
  }

  private Coords moveToNextLocation(
      Coords currentPosition,
      List<Coords> waypoints,
      ArrayList<Move> moves,
      boolean locationIsEnd) {

    // This is usually a sensor position, on the last step it will be the ending position.
    var targetLocation = waypoints.get(waypoints.size() - 1);
    var movesToNextLocation = new ArrayList<Move>();
    for (int i = 1; i < waypoints.size(); i++) {
      var targetWaypoint = waypoints.get(i);
      int whileCount = 0;
      while (true) {
        whileCount++;
        if (whileCount == 100) {
          System.out.println("No path found: stuck in infinite loop");
          return currentPosition;
        }
        var moveDirection = radiansToRoundedDegrees(currentPosition.angleTo(targetWaypoint));
        log(moveDirection);

        var afterPosition = currentPosition.getPositionAfterMoveDegrees(moveDirection, MOVE_LENGTH);

        // If we collide with something we must try a different direction
        if (obstacles.collidesWith(currentPosition, afterPosition)) {
          moveDirection =
              findDirectionWhichAvoidsObstacles(
                  currentPosition,
                  moveDirection,
                  targetWaypoint,
                  waypoints,
                  movesToNextLocation,
                  i);
          afterPosition = currentPosition.getPositionAfterMoveDegrees(moveDirection, MOVE_LENGTH);
        }
        // If our target is the end position then we use a different range, and return, even if we
        // haven't hit all waypoints
        if (locationIsEnd && afterPosition.distance(targetLocation) < END_POSITION_RANGE) {
          log("Reached end");
          movesToNextLocation.add(new Move(currentPosition, afterPosition, moveDirection, null));
          moves.addAll(movesToNextLocation);
          log("Length: " + movesToNextLocation.size());
          return afterPosition;
        }

        // If we reach the target sensor then we return, even if we haven't hit all waypoints
        if (afterPosition.distance(targetLocation) < SENSOR_RANGE) {
          log("Hit sensor " + targetLocation);
          movesToNextLocation.add(
              new Move(
                  currentPosition,
                  afterPosition,
                  moveDirection,
                  sensorCoordsW3WMap.get(targetLocation)));
          moves.addAll(movesToNextLocation);
          log("Length: " + movesToNextLocation.size());
          return afterPosition;
        }

        // If our target is a waypoint for obstacle evasion, instead of being within a certain
        // radius we must ensure that we have gone round the corner and the next waypoint is in
        // sight
        if (i < waypoints.size() - 1) {

          if (!obstacles.collidesWith(afterPosition, waypoints.get(i + 1))) {
            // If we have line of sight with the next waypoint, we have rounded the corner
            log("Rounded corner");
            movesToNextLocation.add(new Move(currentPosition, afterPosition, moveDirection, null));
            currentPosition = afterPosition;
            break;
          } else {
            // If we haven't reached anything, keep going
            movesToNextLocation.add(new Move(currentPosition, afterPosition, moveDirection, null));
            currentPosition = afterPosition;
          }
        } else {
          // If we haven't reached anything, keep going
          movesToNextLocation.add(new Move(currentPosition, afterPosition, moveDirection, null));
          currentPosition = afterPosition;
        }
      }
    }
    moves.addAll(movesToNextLocation);
    return currentPosition;
  }

  private int findDirectionWhichAvoidsObstacles(
      Coords currentPosition,
      int moveDirection,
      Coords targetWaypoint,
      List<Coords> waypoints,
      List<Move> movesToNextLocation,
      int i) {
    // We don't whether a move which is more anticlockwise or more clockwise is needed to
    // avoid the obstacles, and finding out would be difficult, so instead we try both
    // directions and see what works.
    for (int adjustmentAngle = 10; adjustmentAngle <= 180; adjustmentAngle += 10) {
      var antiClockwiseAngle = (moveDirection + adjustmentAngle) % 360;
      var antiClockwisePosition =
          currentPosition.getPositionAfterMoveDegrees(antiClockwiseAngle, MOVE_LENGTH);

      // The new move needs to avoid the obstacle, and end in line of sight of the next target
      if (!obstacles.collidesWith(currentPosition, antiClockwisePosition)
          && !obstacles.collidesWith(antiClockwisePosition, targetWaypoint)) {
        log("Adjusted anti-clockwise to " + antiClockwiseAngle);
        return antiClockwiseAngle;
      }

      var clockwiseAngle = (moveDirection - adjustmentAngle) % 360;
      var clockwisePosition =
          currentPosition.getPositionAfterMoveDegrees(clockwiseAngle, MOVE_LENGTH);
      if (!obstacles.collidesWith(currentPosition, clockwisePosition)
          && !obstacles.collidesWith(antiClockwisePosition, targetWaypoint)) {
        log("Adjusted clockwise to " + clockwiseAngle);
        return clockwiseAngle;
      }
    }
    return -1;
  }

  private Coords recursiveStep(
      List<Coords> waypoints,
      List<Move> movesToNextLocation,
      Coords targetLocation,
      boolean locationIsEnd,
      Coords currentPosition,
      int moveDirection,
      int i,
      int moveCounter) {
    log(i + " " + moveDirection);
    if (moveCounter > 20) {
      System.out.println("INFINITE LOOP");
      return null;
    }

    var afterPosition = currentPosition.getPositionAfterMoveDegrees(moveDirection, MOVE_LENGTH);

    // If we collide with something we must try a different direction
    if (obstacles.collidesWith(currentPosition, afterPosition)) {
      System.out.println("Collision");
      return null;
    } else {

      // If our target is a waypoint for obstacle evasion, instead of being within a certain
      // radius we must ensure that we have gone round the corner and the next waypoint is in
      // sight
      if (i < waypoints.size() - 1) {
        // If we have line of sight with the next waypoint, we have rounded the corner
        if (!obstacles.collidesWith(afterPosition, waypoints.get(i + 1))) {
          log("Rounded corner");
          movesToNextLocation.add(new Move(currentPosition, afterPosition, moveDirection, null));
          var nextMoveDirection =
              radiansToRoundedDegrees(afterPosition.angleTo(waypoints.get(i + 1)));
          return recursiveStep(
              waypoints,
              movesToNextLocation,
              targetLocation,
              locationIsEnd,
              afterPosition,
              nextMoveDirection,
              ++i,
              ++moveCounter);
        }
      }

      // If our target is the end position then we use a different range, and return, even if we
      // haven't hit all waypoints
      if (locationIsEnd && afterPosition.distance(targetLocation) < END_POSITION_RANGE) {
        log("Reached end");
        movesToNextLocation.add(new Move(currentPosition, afterPosition, moveDirection, null));
        log("Length: " + movesToNextLocation.size());
        return afterPosition;
      } else if (afterPosition.distance(targetLocation) < SENSOR_RANGE) {
        // If we reach the target sensor then we return, even if we haven't hit all waypoints
        log("Hit sensor " + targetLocation);
        movesToNextLocation.add(
            new Move(
                currentPosition,
                afterPosition,
                moveDirection,
                sensorCoordsW3WMap.get(targetLocation)));
        log("Length: " + movesToNextLocation.size());
        return afterPosition;
      } else {
        // If we haven't reached anything, keep going
        movesToNextLocation.add(new Move(currentPosition, afterPosition, moveDirection, null));
      }

      var nextMoveDirection = radiansToRoundedDegrees(afterPosition.angleTo(waypoints.get(i)));
      var returnValue =
          recursiveStep(
              waypoints,
              movesToNextLocation,
              targetLocation,
              locationIsEnd,
              afterPosition,
              nextMoveDirection,
              i,
              ++moveCounter);
      if (returnValue != null) {
        return returnValue;
      }

      for (int adjustmentAngle = 10; adjustmentAngle <= 180; adjustmentAngle += 10) {
        var antiClockwiseDirection = (moveDirection + adjustmentAngle) % 360;
        returnValue =
            recursiveStep(
                waypoints,
                movesToNextLocation,
                targetLocation,
                locationIsEnd,
                afterPosition,
                antiClockwiseDirection,
                i,
                ++moveCounter);
        if (returnValue != null) {
          return returnValue;
        }

        var clockwiseDirection = (moveDirection - adjustmentAngle) % 360;
        returnValue =
            recursiveStep(
                waypoints,
                movesToNextLocation,
                targetLocation,
                locationIsEnd,
                afterPosition,
                clockwiseDirection,
                i,
                ++moveCounter);
        if (returnValue != null) {
          return returnValue;
        }
      }
      return returnValue;
    }
  }

  /**
   * Convert an angle in radians (that ranges from -pi to pi) to the angle in degrees to the nearest
   * 10, ranging from 0 to 350
   */
  private int radiansToRoundedDegrees(double angle) {
    angle = Math.toDegrees(angle);
    int degrees = (int) (Math.round(angle / 10.0) * 10);

    // If the angle is negative, rotate it around one revolution so that it no longer is
    if (degrees < 0) {
      degrees += 360;
    }
    return degrees;
  }

  private void log(Object o) {
    System.out.println(o);
  }
}
