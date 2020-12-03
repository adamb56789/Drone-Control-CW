package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import uk.ac.ed.inf.aqmaps.flightplanning.FlightPlanner;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;
import uk.ac.ed.inf.aqmaps.noflyzone.Obstacles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@SuppressWarnings("SameParameterValue")
public class FlightPlannerTest {
  // If testing takes too long, decrease these values.
  public static final int DAYS_TO_TEST = 1;
  // Tries 3 tricky non-random points by default, try this many more random points
  public static final int RANDOM_STARTING_POINTS_TO_TRY = 0;
  // 3 tricky starting locations
  public static final Coords INF_FORUM_ALCOVE = new Coords(-3.1869108, 55.9449634);
  public static final Coords APPLETON_ALCOVE = new Coords(-3.1864079, 55.9443635);
  public static final Coords LIBRARY_CORNER = new Coords(-3.189626, 55.942625);
  public static final Coords PRESCRIBED_START = new Coords(-3.188396, 55.944425);

  // Obstacles for testing for collisions
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

        if (move.getSensorW3W() != null) {
          var distanceToSensor = move.getSensorW3W().getCoordinates().distance(move.getAfter());
          assertTrue(
              "The move should end within 0.0002 of a sensor, was " + distanceToSensor,
              distanceToSensor < 0.0002);
          sensorCount++;
        }
      }
      assertEquals("The flight plan should contain all of the sensors", 33, sensorCount);
    }
    System.out.printf("Average length: %.3f moves%n", flightPathLengths / flightPlans.size());
    System.out.println("Runs completed: " + flightPlans.size());
  }

  private List<List<Move>> getFlightPlans() {
    // For each of the dates, get the flight plans for a number of starting locations
    return getSpecifiedDates(DAYS_TO_TEST).stream()
        .map(date -> runFlightPlansOnDate(date, RANDOM_STARTING_POINTS_TO_TRY))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private List<List<Move>> runFlightPlansOnDate(int[] date, int startingPointsToTry) {
    var outputFlightPlans = new ArrayList<List<Move>>();
    var random = new Random();
    var startingLocations = new ArrayList<Coords>();
    startingLocations.add(INF_FORUM_ALCOVE);
    startingLocations.add(APPLETON_ALCOVE);
    startingLocations.add(LIBRARY_CORNER);
    startingLocations.add(PRESCRIBED_START);

    var input =
        new ServerInputController(
            ServerInputControllerTest.getFakeServer(), date[0], date[1], date[2], 80);
    var obstacles = new Obstacles(input.getNoFlyZones());
    for (int i = 0; i < startingPointsToTry; i++) {
      double randomLng =
          Obstacles.TOP_LEFT.x
              + (Obstacles.BOTTOM_RIGHT.x - Obstacles.TOP_LEFT.x) * random.nextDouble();
      double randomLat =
          Obstacles.BOTTOM_RIGHT.y
              + (Obstacles.TOP_LEFT.y - Obstacles.BOTTOM_RIGHT.y) * random.nextDouble();

      var startingLocation = new Coords(randomLng, randomLat);

      if (obstacles.pointCollides(startingLocation)) {
        continue;
      }
      startingLocations.add(startingLocation);
    }

    for (var startLocation : startingLocations) {
      var flightPlanner = new FlightPlanner(obstacles, input.getSensorW3Ws(), 0, 0.1);
      System.out.println("date = " + Arrays.toString(date) + ", startingPoint = " + startLocation);
      outputFlightPlans.add(flightPlanner.createBestFlightPlan(startLocation));
    }
    return outputFlightPlans;
  }

  /**
   * Gets up to 12 of the dates 01/01/2020, 02/02/2020, ..., 12/12/2020
   *
   * @param days the number of days to get
   * @return a list of arrays containing all dates in 2020 and 2021
   */
  private List<int[]> getSpecifiedDates(int days) {
    var dates = new ArrayList<int[]>();
    for (int i = 1; i <= days && i <= 12; i++) {
      dates.add(new int[] {i, i, 2020});
    }
    return dates;
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
          dates.add(new int[] {day, month, year});
          if (--days == 0) {
            return dates;
          }
        }
      }
    }
    return dates;
  }
}
