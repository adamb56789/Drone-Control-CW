 package uk.ac.ed.inf.aqmaps;

 import com.google.gson.Gson;
 import com.mapbox.geojson.Feature;
 import com.mapbox.geojson.FeatureCollection;
 import com.mapbox.geojson.Point;
 import uk.ac.ed.inf.aqmaps.deserializers.W3WDeserializer;
 import uk.ac.ed.inf.aqmaps.geometry.Coords;
 import uk.ac.ed.inf.aqmaps.io.Server;
 import uk.ac.ed.inf.aqmaps.io.ServerInputController;
 import uk.ac.ed.inf.aqmaps.io.ServerInputController;

 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.*;
 import java.util.stream.Collectors;
 import java.util.stream.Stream;

 public class Testing {
  public static List<Double>[] results = new List[6];
  public static int[] winner = new int[6];
  public static List<Long> times = new ArrayList<>();
  public static List<Long> loadingTimes = new ArrayList<>();
  public static List<Long> dijkstraTimes = new ArrayList<>();
  public static List<Integer> counters = new ArrayList<>();
  public static int counter;
  public static List<Double> numbers = new ArrayList<>();

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
    //    try (Stream<Path> walk = Files.walk(Paths.get("WebServer/words"))) {
    //
    //      List<String> result = walk.filter(Files::isRegularFile)
    //              .map(Path::toString).collect(Collectors.toList());
    //
    //      result.forEach(System.out::println);
    //      var input = new ServerController(getFakeServer(), 1, 1, 2020, 80);
    //
    //      var f = input.getNoFlyZones();
    //      var sensorLocations = new ArrayList<W3W>();
    //      result.stream().forEach(path -> {
    //        try {
    //          var w = new Gson().fromJson(Files.readString(Path.of(path)),
    // W3WDeserializer.class).getW3W();
    //          sensorLocations.add(w);
    //          //f.features().add(Feature.fromGeometry(Point.fromLngLat(w.getCoordinates().x,
    // w.getCoordinates().y)));
    //        } catch (IOException e) {
    //          e.printStackTrace();
    //        }
    //      });
    //      System.out.println(f.toJson());
    //      var obstacles = new Obstacles(input.getNoFlyZones());
    //      var obstacleGraph = new ObstacleGraph(obstacles);
    //      var sensorGraph = new SensorGraph(sensorLocations, obstacleGraph, 0);
    //      var path = sensorGraph.getTour(new Coords(-3.1878, 55.9444));
    //      var superList = new ArrayList<List<Coords>>();
    //      for (int i = 0; i < path.size(); i++) {
    //        superList.add(
    //                obstacleGraph.getShortestPathPoints(path.get(i), path.get((i + 1) %
    // path.size())));
    //      }
    //      System.out.println(path);
    //
    //
// System.out.println(superList.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    //
    //    } catch (IOException e) {
    //      e.printStackTrace();
    //    }
    //
    //    System.exit(1);
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

    //    for (var result : results) {
    //      var avg = result.stream().mapToDouble(a -> a).average().orElse(0.0);
    //      System.out.println(avg);
    //    }
    System.out.println(numbers.stream().mapToDouble(a -> a).average().orElse(0));
    System.out.println(Arrays.toString(winner));

    long endTime = System.currentTimeMillis();
    System.out.printf("Counter: %d%n", counter);
    System.out.print("Counters frequency: ");
    for (int i = 0; i < 33; i++) {
      System.out.printf("(%d, %d) ", i, Collections.frequency(counters, i));
    }
    System.out.println();
    System.out.printf("Loading: %d ms%n", loadingTimes.stream().mapToLong(a -> a).sum() /
 1000000);
    System.out.printf(
        "Dijkstra: %d ms%n", dijkstraTimes.stream().mapToLong(a -> a).sum() / 1000000);
    System.out.printf("Everything: %d ms%n", times.stream().mapToLong(a -> a).sum() / 1000000);
    System.out.printf("Real time: %d ms%n", endTime - startTime);
  }

  private static void run(int[] date) {
    long startTime = System.nanoTime();
    var input = new ServerInputController(getFakeServer(), date[0], date[1], date[2], 80);
    long endTime = System.nanoTime();
    loadingTimes.add(endTime - startTime);
    var obstacles = new Obstacles(input.getNoFlyZones());
    var obstacleGraph = new ObstacleGraph(obstacles);
    var sensorGraph = new SensorGraph(input.getSensorLocations(), obstacleGraph, 0);
    var path = sensorGraph.getTour(new Coords(-3.186918944120407, 55.944958385847485));
    var superList = new ArrayList<List<Coords>>();
    int n = 0;
    for (int i = 0; i < path.size(); i++) {
      if (obstacles.collidesWith(path.get(i), path.get((i + 1) % path.size()))) {
        n++;
      }
    }
    counters.add(n);
    if (n == 5) {

      for (int i = 0; i < path.size(); i++) {
        superList.add(
            obstacleGraph.getShortestPathPoints(path.get(i), path.get((i + 1) % path.size())));
      }
      //    System.out.println(path);
//      System.out.println(
//          superList.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }
    times.add(System.nanoTime() - startTime);
  }
 }
