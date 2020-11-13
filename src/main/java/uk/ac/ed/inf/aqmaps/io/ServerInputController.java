package uk.ac.ed.inf.aqmaps.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;
import uk.ac.ed.inf.aqmaps.Sensor;
import uk.ac.ed.inf.aqmaps.Settings;
import uk.ac.ed.inf.aqmaps.W3W;
import uk.ac.ed.inf.aqmaps.deserializers.SensorDeserializer;
import uk.ac.ed.inf.aqmaps.deserializers.W3WDeserializer;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/** Implements the Remote interface using a connection to a simple web server. */
public class ServerInputController implements InputController {
  private final HashMap<W3W, Sensor> sensorMap = new HashMap<>();
  private final Server server;
  private List<Polygon> noFlyZones;
  private String serverUrl;

  /**
   * Create a new WebServer instance for a given day and port.
   *
   * @param settings the Settings object containing the current settings
   */
  public ServerInputController(Settings settings) {
    this(
        new WebServer(),
        settings.getDay(),
        settings.getMonth(),
        settings.getYear(),
        settings.getPort());
  }

  /** This exists so mock server objects can be passed in for testing. */
  public ServerInputController(Server server, int day, int month, int year, int port) {
    this.server = server;
    loadData(day, month, year, port);
  }

  private void loadData(int day, int month, int year, int port) {
    serverUrl = "http://localhost:" + port;

    // Load no-fly zones
    String url = serverUrl + "/buildings/no-fly-zones.geojson";
    String nfzJson = server.requestData(url);
    //noinspection ConstantConditions ignore warning about features() being nullable
    noFlyZones =
        FeatureCollection.fromJson(nfzJson).features().stream()
            .map(Polygon::buildFromFeature)
            .collect(Collectors.toList());

    // Load today's sensors
    url =
        serverUrl
            + "/maps/"
            + String.format("%04d", year)
            + '/'
            + String.format("%02d", month)
            + '/'
            + String.format("%02d", day)
            + "/air-quality-data.json";
    String sensorJson = server.requestData(url);

    // Deserialize the air quality data into SensorDeserializer objects. A separate class is used
    // for deserialization since the JSON contains the location as a word string only, but we want
    // to internally represent the sensor with a W3W object which also contains the coordinates.
    Type listType = new TypeToken<ArrayList<SensorDeserializer>>() {}.getType();
    ArrayList<SensorDeserializer> sensorDeserializers = new Gson().fromJson(sensorJson, listType);

    // Convert the list of SensorDeserializers to a list of Sensors
    var sensors =
        sensorDeserializers.stream()
            .map(this::convertToSensor)
            .collect(Collectors.toCollection(ArrayList::new));

    // Put locations and their corresponding sensor into a Map for later access by readSensor()
    for (var sensor : sensors) {
      sensorMap.put(sensor.getLocation(), sensor);
    }
  }

  /** Converts a SensorDeserializer to a Sensor by getting the coordinates of its W3W location. */
  private Sensor convertToSensor(SensorDeserializer sensorDeserializer) {
    // Get the appropriate W3W object from the server
    // Example url: http://localhost:80/words/dent/shins/cycle/details.json
    String url =
        serverUrl
            + "/words/"
            + sensorDeserializer.getLocation().replace('.', '/')
            + "/details.json";
    String w3wJson = server.requestData(url);
    var w3w = (new Gson().fromJson(w3wJson, W3WDeserializer.class)).getW3W();

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
  public List<Polygon> getNoFlyZones() {
    return noFlyZones;
  }
}
