package uk.ac.ed.inf.aqmaps;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/** Implements the Remote interface using a connection to a simple web server. */
public class ServerController implements Remote {
  private final HashMap<W3W, Sensor> sensorMap = new HashMap<>();
  private final Server server;
  private FeatureCollection noFlyZones;
  private String serverUrl;

  /**
   * Create a new WebServer instance for a given day and port.
   *
   * @param day the day to get sensor information for
   * @param month the month
   * @param year the year
   * @param port the port of the web server to connect to on localhost
   */
  public ServerController(int day, int month, int year, int port) {
    this(new WebServer(), day, month, year, port);
  }

  /** This exists for passing in mock server objects for testing. */
  public ServerController(Server server, int day, int month, int year, int port) {
    this.server = server;
    loadData(day, month, year, port);
  }

  private void loadData(int day, int month, int year, int port) {
    serverUrl = "http://localhost:" + port;

    // Load no-fly zones
    String url = serverUrl + "/buildings/no-fly-zones.geojson";
    String nfzJson = server.requestData(url);
    noFlyZones = FeatureCollection.fromJson(nfzJson);

    // Load today's sensors
    url =
        serverUrl
            + "/maps/"
            + year
            + '/'
            + String.format("%02d", month)
            + '/'
            + String.format("%02d", day)
            + "/air-quality-data.json";
    String sensorJson = server.requestData(url);

    Type listType = new TypeToken<ArrayList<SensorDeserializer>>() {}.getType();
    ArrayList<SensorDeserializer> sensorDeserializers = new Gson().fromJson(sensorJson, listType);

    // Get coordinates of the sensors and convert to list of Sensors including W3W objects
    var sensors =
        sensorDeserializers.stream()
            .map(this::convertToSensor)
            .collect(Collectors.toCollection(ArrayList::new));

    // Insert sensors and their W3W into a HashMap for later access by readSensor()
    for (var sensor : sensors) {
      sensorMap.put(sensor.getLocation(), sensor);
    }
  }

  /** Converts a SensorDeserializer to a Sensor by getting the coordinates of its W3W location. */
  private Sensor convertToSensor(SensorDeserializer sensorDeserializer) {
    // Get the appropriate W3W object from the server
    String url =
        serverUrl
            + "/words/"
            + sensorDeserializer.getLocation().replace('.', '/')
            + "/details.json";
    String w3wJson = server.requestData(url);
    var w3w = new Gson().fromJson(w3wJson, W3W.class);

    // Convert SensorDeserializer to Sensor
    return new Sensor(w3w, sensorDeserializer.getBattery(), sensorDeserializer.getReading());
  }

  @Override
  public Sensor readSensor(W3W location) {
    return sensorMap.get(location);
  }

  @Override
  public List<W3W> getSensorLocations() {
    return new ArrayList<>(sensorMap.keySet());
  }

  @Override
  public FeatureCollection getNoFlyZones() {
    return noFlyZones;
  }
}
