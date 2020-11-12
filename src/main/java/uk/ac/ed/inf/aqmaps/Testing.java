package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.Server;
import uk.ac.ed.inf.aqmaps.io.ServerController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Testing {
  public static List<Double>[] results = new List[6];
  public static int[] winner = new int[6];

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
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < Testing.results.length; i++) {
      results[i] = new ArrayList<>();
    }
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


    dates.parallelStream().forEach(Testing::run);

    for (var result : results) {
      var avg = result.stream().mapToDouble(a -> a).average().orElse(0.0);
      System.out.println(avg);
    }

    System.out.println(Arrays.toString(winner));

    long endTime = System.currentTimeMillis();
    System.out.printf("Took %d ms", endTime - startTime);
  }

  private static void run(int[] date) {
    var input = new ServerController(getFakeServer(), date[0], date[1], date[2], 80);
    var obstacleGraph = new ObstacleGraph(new Obstacles(input.getNoFlyZones()));
    var sensorGraph = new SensorGraph(input.getSensorLocations(), obstacleGraph, 0);
    var path = sensorGraph.getTour(new Coords(-3.1878, 55.9444));
  }
}
