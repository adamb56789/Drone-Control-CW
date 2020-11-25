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

  private final Map<Coords, W3W> sensorCoordsW3WMap;

  public DroneNavigation(List<W3W> sensorLocations) {
    sensorCoordsW3WMap = new HashMap<>();
    sensorLocations.forEach(w3w -> sensorCoordsW3WMap.put(w3w.getCoordinates(), w3w));
  }

  public List<Move> createFlightPlan(List<List<Coords>> tour) {
    System.out.println(tour.stream().flatMap(List::stream).collect(Collectors.toList()));

    var moves = new ArrayList<Move>();
    var currentPosition = tour.get(0).get(0);
    for (int i = 0; i < tour.size(); i++) {
      currentPosition =
          moveToNextLocation(currentPosition, tour.get(i), moves, i == tour.size() - 1);
    }
    System.out.println(moves.size());
    return moves;
  }

  private Coords moveToNextLocation(
      Coords currentPosition,
      List<Coords> waypoints,
      ArrayList<Move> moves,
      boolean locationIsEnd) {

    // This is usually a sensor position, on the last step it will be the ending position.
    var targetLocation = waypoints.get(waypoints.size() - 1);
    for (int i = 1; i < waypoints.size(); i++) {
      var targetWaypoint = waypoints.get(i);

      while (true) {
        var moveDirection = radiansToRoundedDegrees(currentPosition.angleTo(targetWaypoint));
        System.out.println(moveDirection);

        var afterPosition = currentPosition.getPositionAfterMoveDegrees(moveDirection, MOVE_LENGTH);

        if (locationIsEnd && afterPosition.distance(targetLocation) < END_POSITION_RANGE) {
          // If our target is the end position then we use a different range, and return, even if we
          // haven't hit all waypoints
          System.out.println("Reached end");
          moves.add(new Move(currentPosition, afterPosition, moveDirection, null));
          return afterPosition;
        } else if (afterPosition.distance(targetLocation) < SENSOR_RANGE) {
          // If we reach the target sensor then we return, even if we haven't hit all waypoints
          System.out.println("Hit sensor");
          moves.add(
              new Move(
                  currentPosition,
                  afterPosition,
                  moveDirection,
                  sensorCoordsW3WMap.get(targetLocation)));
          return afterPosition;
        } else if (afterPosition.distance(targetWaypoint) < SENSOR_RANGE) {
          // If we reach a waypoint then we jump out of the loop and look for the next waypoint
          System.out.println("Hit waypoint");
          moves.add(new Move(currentPosition, afterPosition, moveDirection, null));
          currentPosition = afterPosition;
          break;
        } else {
          // If we haven't reached anything, keep going
          moves.add(new Move(currentPosition, afterPosition, moveDirection, null));
          currentPosition = afterPosition;
        }
      }
    }
    return currentPosition;
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
}
