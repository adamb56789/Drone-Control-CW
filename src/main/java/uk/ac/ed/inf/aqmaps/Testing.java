package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.Server;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;
import uk.ac.ed.inf.aqmaps.pathfinding.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Testing {
  private static final Random random = new Random(0);
  private static final int STARTING_POINT_COUNT = 1;
  public static List<List<Move>> results = new ArrayList<>();
  public static List<Double> times = new ArrayList<>();

  public static Server getFakeServer() {
    class TestServer implements Server {

      @Override
      public String requestData(String url) {
        String data = null;

        // Get the data from the local filesystem.
        String path = "WebServer" + url.substring(19);
        try {
          data = Files.readString(Path.of(path));
        } catch (IOException e) {
          e.printStackTrace();
        }
        return data;
      }
    }
    return new TestServer();
  }

  public static void test() {
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
        }
      }
    }
    double before = System.currentTimeMillis();
    var thread = logProgress();
    thread.start();
    runOnce();
    //    dates.parallelStream().forEach(Testing::run);
    //noinspection deprecation
    thread.stop();
    System.out.println(results.stream().map(Collection::size).collect(Collectors.toList()));
    System.out.println(times);
    System.out.println(
        results.parallelStream().map(Collection::size).reduce(0, Integer::sum)
            * 1.0
            / results.size());
    System.out.println("Total time taken " + (System.currentTimeMillis() - before));
  }

  private static void run(int[] date) {
    for (int i = 0; i < STARTING_POINT_COUNT; i++) {
      double randomLng =
          ConfinementArea.TOP_LEFT.x
              + (ConfinementArea.BOTTOM_RIGHT.x - ConfinementArea.TOP_LEFT.x) * random.nextDouble();
      double randomLat =
          ConfinementArea.BOTTOM_RIGHT.y
              + (ConfinementArea.TOP_LEFT.y - ConfinementArea.BOTTOM_RIGHT.y) * random.nextDouble();

      var input = new ServerInputController(getFakeServer(), date[0], date[1], date[2], 80);
      var obstacles = new Obstacles(input.getNoFlyZones());
      var startingLocation = new Coords(randomLng, randomLat);
      if (obstacles.pointCollision(startingLocation)) {
        continue;
      }
      var obstacleEvader = new ObstacleEvader(obstacles);
      var sensorGraph = new SensorGraph(input.getSensorLocations(), obstacleEvader, 0);
      //      var tour = sensorGraph.getTour(new Coords(-3.186918944120407, 55.944958385847485));
      var tour = sensorGraph.getTour(startingLocation);
      var droneNavigation =
          new FlightPlanner(obstacles, obstacleEvader, input.getSensorLocations());
      int finalI = i;
      Thread thread =
          new Thread(
              () -> {
                int count = 1;
                while (true) {
                  try {
                    TimeUnit.SECONDS.sleep(1);
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                  System.out.println(
                      (count++) + " " + finalI + " " + Arrays.toString(date) + " " + tour);
                }
              });

      thread.start();
      double start = System.nanoTime();
      var flightPlan = droneNavigation.createFlightPlan(tour);
      times.add((System.nanoTime() - start) / 1000000);
      //noinspection deprecation
      thread.stop();

      var res = new Results(input.getSensorLocations());
      res.recordFlightpath(flightPlan);

      results.add(flightPlan);
    }
  }

  private static void runOnce() {
    var input = new ServerInputController(getFakeServer(), 21, 5, 2020, 80);
    var obstacles = new Obstacles(input.getNoFlyZones());
    var startingLocation = new Coords(-3.187241501223053, 55.94376202189103);
    var obstacleEvader = new ObstacleEvader(obstacles);
    var sensorGraph = new SensorGraph(input.getSensorLocations(), obstacleEvader, 0);
    //      var tour = sensorGraph.getTour(new Coords(-3.186918944120407, 55.944958385847485));
    var tour = sensorGraph.getTour(startingLocation);
    var droneNavigation = new FlightPlanner(obstacles, obstacleEvader, input.getSensorLocations());
    var flightPlan = droneNavigation.createFlightPlan(tour);

    var res = new Results(input.getSensorLocations());
    res.recordFlightpath(flightPlan);

    System.out.println(flightPlan.size());
    System.out.println(res.getMapGeoJSON());
    System.out.println(flightPlan);
  }

  private static Thread logProgress() {
    return new Thread(
        () -> {
          while (true) {
            System.out.println(times.size());
            try {
              TimeUnit.MILLISECONDS.sleep(5000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        });
  }
}
