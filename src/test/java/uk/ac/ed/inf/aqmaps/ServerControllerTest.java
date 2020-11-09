package uk.ac.ed.inf.aqmaps;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ServerControllerTest {
  private Server getServer() {
    class TestServer implements Server {

      @Override
      public String requestData(String url) {
        String data = null;
        // Check that the server prefix is correct
        String serverUrl = url.substring(0, 19);
        assertEquals("http://localhost:80", serverUrl);

        // Get the data from the local filesystem.
        String path = "WebServer" + url.substring(19);
        try {
          data =  Files.readString(Path.of(path));
        } catch (IOException e) {
          e.printStackTrace();
        }

        // File must exist
        assertNotNull(data);
        return data;
      }
    }
    return new TestServer();
  }

  @Test
  public void noFlyZoneLoadedCorrectly() {
    var remote = new ServerController(getServer(), 1, 1, 2020, 80);
    var noFlyZones = remote.getNoFlyZones();

    assert noFlyZones.features() != null;
    assertEquals("There are 4 buildings", 4, noFlyZones.features().size());
  }

  @Test
  public void sensorLocationsCorrect() {
    var remote = new ServerController(getServer(), 1, 1, 2020, 80);
    var sensorLocations = remote.getSensorLocations();

    assertEquals("There are 33 sensors", 33, sensorLocations.size());
    assertTrue("The words exist", sensorLocations.get(0).getWords().length() > 0);

    double lng = sensorLocations.get(0).getCoordinates().lng;
    double lat = sensorLocations.get(0).getCoordinates().lat;
    assertTrue("The longitude is sensible", -4 <= lng && lng <= -3);
    assertTrue("The latitude is sensible", 55 <= lat && lat <= 56);
  }

  @Test
  public void sensorReadingCorrect() {
    var remote = new ServerController(getServer(), 1, 1, 2020, 80);
    var sensor = remote.readSensor(remote.getSensorLocations().get(0));

    assertTrue("The battery is displaying a sensible number", 0 <= sensor.getBattery() && sensor.getBattery() <= 100);
    assertTrue("The sensor reading exists", sensor.getReading().length() > 0);
  }
}
