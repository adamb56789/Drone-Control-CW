package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;
import uk.ac.ed.inf.aqmaps.pathfinding.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@SuppressWarnings("SameParameterValue")
public class FlightPlannerTest {
  // If testing takes too long, decrease these values.
  public static final int DAYS_TO_TEST = 20; // Maximum is 731
  public static final int STARTING_POINTS_TO_TRY = 1;

  private final Obstacles obstacles =
      new Obstacles(
          (new ServerInputController(ServerInputControllerTest.getFakeServer(), 1, 1, 2020, 80))
              .getNoFlyZones());

  @Test
  public void flightPlanCorrect() {
    // All tests are done at once since it takes a long time to generate this many flight plans
    // Calculate average move length while we're at it
    double flightPathLengths = 0;
    var flightPlans = getFlightPlans();
    for (var flightPlan : flightPlans) {
      flightPathLengths += flightPlan.size();
      assertTrue(
          "Flight plan should be no more than 150 moves, was " + flightPlan.size(),
          flightPlan.size() <= 150);
      var sensorCount = 0;
      for (var move : flightPlan) {
        assertFalse(
            "Flight plan should not collide with any obstacles",
            obstacles.lineCollision(move.getBefore(), move.getAfter()));

        var inRange =
            0 <= move.getDirection() && move.getDirection() <= 350 && move.getDirection() % 10 == 0;
        assertTrue(
            "All directions should be multiples of 10 degrees and in range [0,350]. Instead was "
                + move.getDirection(),
            inRange);

        if (move.getSensor() != null) {
          var distanceToSensor = move.getSensor().getCoordinates().distance(move.getAfter());
          assertTrue(
              "The move should end within 0.0003 of a sensor, was " + distanceToSensor,
              distanceToSensor < 0.0002);
          sensorCount++;
        }
      }
      assertEquals("The flight plan should contain all of the sensors", 33, sensorCount);
    }
    System.out.printf("Average length: %.3f moves%n", flightPathLengths / flightPlans.size());
  }

  private List<List<Move>> getFlightPlans() {
    // For each of the dates, get the flight plans for a number of starting locations
    return getDates(DAYS_TO_TEST)
        .stream() // This is slow so using a parallelStream makes the test go faster
        .map(date -> runFlightPlansOnDate(date, STARTING_POINTS_TO_TRY))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  /**
   * Gets the first n dates in 2020 and 2021
   *
   * @param days the number of days to get
   * @return a list of arrays containing all dates in 2020 and 2021
   */
  private List<int[]> getDates(int days) {
    var dates = new ArrayList<int[]>();
    for (int year = 2020; year <= 2021; year++) {
      for (int month = 1; month <= 12; month++) {
        for (int day = 1; day <= 31; day++) {
          if (day == 31 && (month == 4 || month == 6 || month == 9 || month == 11)) {
            break;
          } else if (day == 29 && month == 2 && year == 2021) {
            break;
          } else if (day == 30 && month == 2 && year == 2020) {
            break;
          }
          int[] date = new int[3];
          date[0] = day;
          date[1] = month;
          date[2] = year;
          dates.add(date);
          if (--days == 0) {
            return dates;
          }
        }
      }
    }
    return dates;
  }

  private List<List<Move>> runFlightPlansOnDate(int[] date, int startingPointsToTry) {
    var outputFlightPlans = new ArrayList<List<Move>>();
    var random = new Random();
    for (int i = 0; i < startingPointsToTry; i++) {

      // Generate random starting location
      double randomLng =
          ConfinementArea.TOP_LEFT.x
              + (ConfinementArea.BOTTOM_RIGHT.x - ConfinementArea.TOP_LEFT.x) * random.nextDouble();
      double randomLat =
          ConfinementArea.BOTTOM_RIGHT.y
              + (ConfinementArea.TOP_LEFT.y - ConfinementArea.BOTTOM_RIGHT.y) * random.nextDouble();

      var input =
          new ServerInputController(
              ServerInputControllerTest.getFakeServer(), date[0], date[1], date[2], 80);
      var obstacles = new Obstacles(input.getNoFlyZones());
      var startingLocation = new Coords(randomLng, randomLat);

      // Check that the starting location is not inside an obstacle
      if (obstacles.pointCollision(startingLocation)) {
        continue;
      }

      // Create the sensor graph and compute the tour
      var obstacleEvader = new ObstacleEvader(obstacles);
      var tour =
          (new SensorGraph(input.getSensorLocations(), obstacleEvader, 0))
              .getTour(startingLocation);

      // Compute the flight plan
      outputFlightPlans.add(
          (new FlightPlanner(obstacles, obstacleEvader, input.getSensorLocations()))
              .createFlightPlan(tour));
    }
    return outputFlightPlans;
  }
}
