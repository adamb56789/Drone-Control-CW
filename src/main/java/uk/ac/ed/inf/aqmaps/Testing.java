package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.Server;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;
import uk.ac.ed.inf.aqmaps.pathfinding.DroneNavigation;
import uk.ac.ed.inf.aqmaps.pathfinding.ObstacleEvader;
import uk.ac.ed.inf.aqmaps.pathfinding.Obstacles;
import uk.ac.ed.inf.aqmaps.pathfinding.SensorGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Testing {
  public static List<List<Move>> results = new ArrayList<>();

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
//    run(dates.get(3));
        dates.stream().forEach(Testing::run);
  }

  private static void run(int[] date) {
    System.out.println(Arrays.toString(date));
    var input = new ServerInputController(getFakeServer(), date[0], date[1], date[2], 80);
    var obstacles = new Obstacles(input.getNoFlyZones());
    var obstacleEvader = new ObstacleEvader(obstacles);
    var sensorGraph = new SensorGraph(input.getSensorLocations(), obstacleEvader, 0);
    var tour = sensorGraph.getTour(new Coords(-3.186918944120407, 55.944958385847485));
    var droneNavigation = new DroneNavigation(obstacles, input.getSensorLocations());
    var flightPlan = droneNavigation.createFlightPlan(tour);
    var r = new Results(input.getSensorLocations());
    r.recordFlightpath(flightPlan);

    results.add(flightPlan);
    System.out.println(r.getMapGeoJSON());
  }
}
