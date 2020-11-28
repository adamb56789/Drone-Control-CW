package uk.ac.ed.inf.aqmaps.pathfinding;

import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Handles the creation of a flight plan for the drone. */
public class FlightPlanner {
  private final Obstacles obstacles;
  private final ObstacleEvader obstacleEvader;
  private final Map<Coords, W3W> sensorCoordsW3WMap;

  /**
   * Constructor
   *
   * @param obstacles the Obstacles for collision detection
   * @param obstacleEvader the ObstacleEvader for finding paths between points around obstacles
   * @param sensorW3Ws the W3W locations of the sensors
   */
  public FlightPlanner(Obstacles obstacles, ObstacleEvader obstacleEvader, List<W3W> sensorW3Ws) {
    this.obstacles = obstacles;
    this.obstacleEvader = obstacleEvader;
    sensorCoordsW3WMap = new HashMap<>();
    sensorW3Ws.forEach(w3w -> sensorCoordsW3WMap.put(w3w.getCoordinates(), w3w));
  }

  /**
   * Create a flight plan for the drone along the given tour.
   *
   * @param tour a list of points that make up the journey of the drone.
   * @return a list of Moves that make up the flight plan
   */
  public List<Move> createFlightPlan(List<Coords> tour) {
    var moves = new ArrayList<Move>();
    var currentPosition = tour.get(0); // The starting position is the very start of the tour

    for (int i = 1; i < tour.size(); i++) {
      var currentTarget = tour.get(i);

      // If the target is not a sensor this will be null
      W3W targetSensorOrNull = sensorCoordsW3WMap.get(currentTarget);

      // If we are on any but the last leg of the tour, attempt to optimize the target location to
      // cut the corner.
      if (i < tour.size() - 1) {
        var nextTarget = tour.get(i + 1);

        // Calculate the bisector between the direction from the target to the current position and
        // the target to the next target
        double angle1 = currentTarget.angleTo(currentPosition);
        double angle2 = currentTarget.angleTo(nextTarget);
        double bisector = (angle1 + angle2) / 2;

        // Move the target 0.5 * (sensor range) in that direction to cut the corner. 0.5 was chosen
        // as it performed the best in testing. 1.0 doesn't work because it will often miss the
        // sensor range by a small amount and then go in circles for a bit.
        var newTarget =
            currentTarget.getPositionAfterMoveRadians(
                bisector, WaypointNavigation.SENSOR_RANGE * 0.5);

        // Check the the new target is not inside an obstacle
        if (!obstacles.pointCollision(newTarget)) {
          // Update the target to the optimised one
          currentTarget = newTarget;
        }
      }
      // Compute a list of waypoints from the current position to the target
      var waypoints = obstacleEvader.getPath(currentPosition, currentTarget);

      // Compute a list of Moves from the current position to the target
      var waypointNavigation = new WaypointNavigation(obstacles);
      var movesToTarget =
          waypointNavigation.navigateToLocation(currentPosition, waypoints, targetSensorOrNull);

      if (movesToTarget == null) {
        // In case there is no valid flightpath, we give up here
        System.out.println("Gave up searching for path"); // TODO
        return moves;
      }
      // Update the current position to the end of the sequence of moves
      currentPosition = movesToTarget.get(movesToTarget.size() - 1).getAfter();

      moves.addAll(movesToTarget);
    }
    return moves;
  }
}
