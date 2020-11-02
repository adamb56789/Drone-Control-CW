package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.FeatureCollection;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Handles interaction with all remote information sources and devices. This includes the web
 * server, containing the sensor locations, no-fly zones, and W3W locations. It also includes the
 * drones reading information from nearby sensors. In this implementation, the sensor readings are
 * also from the web server, however a separation is being deliberately maintained in the getters so
 * that this class could be replaced with one which reads data from real sensors.
 */
public class Remote {
  private static final String SERVER = "http://localhost";

  /**
   * @param day the day to get sensor information for
   * @param month the month
   * @param year the year
   * @param port the port of the web server to connect to on localhost
   */
  public Remote(int day, int month, int year, int port) {
    final String serverUrl = "http://localhost:" + port;
    var client = HttpClient.newHttpClient();

    // Load no-fly zones
    try {
      String url = serverUrl + "/buildings/no-fly-zones.geojson";
      var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println(response.body());
    } catch (IOException | InterruptedException e) {
      System.out.println("Fatal error: Unable to connect to " + SERVER + " at port " + port + ".");
      System.exit(1);
    }
  }
}
