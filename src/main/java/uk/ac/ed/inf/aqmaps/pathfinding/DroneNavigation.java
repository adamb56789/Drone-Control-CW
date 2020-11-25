package uk.ac.ed.inf.aqmaps.pathfinding;

import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.*;
import java.util.stream.Collectors;

public class DroneNavigation {
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
      var waypoints = tour.get(i);
      var waypointNavigation = new WaypointNavigation(obstacles, waypoints, i == tour.size() - 1);

      // This list is in reverse order because the algorithm builds it up starting at the end
      var movesToLocation = waypointNavigation.navigateToLocation(currentPosition);

      if (movesToLocation == null) {
        throw new RuntimeException("movesToLocation is null");
      }

      currentPosition = movesToLocation.get(0).getAfter();
      Collections.reverse(movesToLocation);
      moves.addAll(movesToLocation);
    }
    log(moves.size());
    return moves;
  }

  private void log(Object o) {
    System.out.println(o);
  }
}
