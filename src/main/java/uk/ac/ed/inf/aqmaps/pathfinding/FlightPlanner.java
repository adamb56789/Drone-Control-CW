package uk.ac.ed.inf.aqmaps.pathfinding;

import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightPlanner {
  private final Obstacles obstacles;
  private final ObstacleEvader obstacleEvader;
  private final Map<Coords, W3W> sensorCoordsW3WMap;

  public FlightPlanner(
      Obstacles obstacles, ObstacleEvader obstacleEvader, List<W3W> sensorLocations) {
    this.obstacles = obstacles;
    this.obstacleEvader = obstacleEvader;
    sensorCoordsW3WMap = new HashMap<>();
    sensorLocations.forEach(w3w -> sensorCoordsW3WMap.put(w3w.getCoordinates(), w3w));
  }

  public List<Move> createFlightPlan(List<List<Coords>> tour) {
    var moves = new ArrayList<Move>();
    var currentPosition = tour.get(0).get(0); // The starting position is the very start of the tour
    for (int i = 0; i < tour.size(); i++) {
      var waypoints = tour.get(i);

      W3W targetSensorOrNull;
      // If we are on the last step the target position will not be a sensor, so send null instead.
      if (i < tour.size() - 1) {
        // The target sensor is the last element of this section of the tour
        targetSensorOrNull = sensorCoordsW3WMap.get(waypoints.get(waypoints.size() - 1));
      } else {
        targetSensorOrNull = null;
      }

      // If we are on any but the last leg of the tour, attempt to optimize the target location to
      // cut the corner. This is very effective, cutting the average move length by 8, with no
      // measurable performance impact.
      // Average move lengths in testing:
      // Without this step: 84.6
      // With this step: 76.6
      if (i < tour.size() - 1) {
        var currentTarget = waypoints.get(waypoints.size() - 1);

        var nextWaypoints = tour.get(i + 1);
        var nextTarget = nextWaypoints.get(nextWaypoints.size() - 1);

        // Calculate the bisector between the direction from the target to the current position and
        // the target to the next target
        double angle1 = currentTarget.angleTo(currentPosition);
        double angle2 = currentTarget.angleTo(nextTarget);
        double bisector = (angle1 + angle2) / 2;

        // Move the target 1 * (sensor range) in that direction to cut the corner
        var newTarget =
            currentTarget.getPositionAfterMoveRadians(bisector, WaypointNavigation.SENSOR_RANGE);

        // Check the the new target is not inside an obstacle
        if (!obstacles.pointInObstacle(newTarget)) {
          waypoints = obstacleEvader.getShortestPathPoints(currentPosition, newTarget);
        }
      }

      var waypointNavigation = new WaypointNavigation(obstacles, waypoints, targetSensorOrNull);

      var movesToLocation = waypointNavigation.navigateToLocation(currentPosition);

      if (movesToLocation == null) {
        // In case there is no valid flightpath, we give up here
        System.out.println("Gave up searching for path");
        return moves;
      }

      currentPosition = movesToLocation.get(movesToLocation.size() - 1).getAfter();
      moves.addAll(movesToLocation);
    }
    return moves;
  }
}
