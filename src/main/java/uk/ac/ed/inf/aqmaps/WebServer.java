package uk.ac.ed.inf.aqmaps;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Implements the Remote interface using a connection to a simple web server. */
public class WebServer implements Remote {
  private final HttpClient client;
  private String serverUrl = "http://localhost";
  private FeatureCollection noFlyZones;
  private HashMap<W3W, Sensor> sensorMap = new HashMap<>();

  /**
   * Create a new WebServer instance for a given day and port.
   *
   * @param day the day to get sensor information for
   * @param month the month
   * @param year the year
   * @param port the port of the web server to connect to on localhost
   */
  public WebServer(int day, int month, int year, int port) {
    serverUrl = serverUrl + ":" + port;
    client = HttpClient.newHttpClient();

    // Load no-fly zones

    String url = serverUrl + "/buildings/no-fly-zones.geojson";
    String nfzJson = requestFromServer(url);
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
    String sensorJson = requestFromServer(url);

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

  private String requestFromServer(String url) {
    String returnValue = "";
    try {
      var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        // 200 means OK
        returnValue = response.body();
      } else {
        // any other status code means the data has not been acquired successfully
        System.out.println("Fatal error: " + url + " returned status " + response.statusCode());
        System.exit(1);
      }

    } catch (IOException | InterruptedException e) {
      // This is normally a java.net.ConnectException if there is no server running on the port
      System.out.println("Fatal error: Unable to connect to " + url);
      System.exit(1);
    }
    return returnValue; // This should never be reached, but the compiler isn't figuring that out.
  }

  /** Converts a SensorDeserializer to a Sensor by getting the coordinates of its W3W location. */
  private Sensor convertToSensor(SensorDeserializer sensorDeserializer) {
    // Get the appropriate W3W object from the server
    String url = serverUrl + "/words/" + sensorDeserializer.getLocation().replace('.', '/') + "/details.json";
    String w3wJson = requestFromServer(url);
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
