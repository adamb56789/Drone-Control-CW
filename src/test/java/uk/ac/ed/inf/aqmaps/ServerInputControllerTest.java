package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.io.InputController;
import uk.ac.ed.inf.aqmaps.io.Server;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ServerInputControllerTest {
  private InputController input;

  public static Server getFakeServer() {
    class TestServer implements Server {

      @Override
      public String requestData(String url) {
        String data = null;
        // Check that the server prefix is correct
        String serverUrl = url.substring(0, 19);
        assertEquals("http://localhost:80", serverUrl);

        // Get the data from the local filesystem resources
        String path = "/WebServer" + url.substring(19);
        var file = new File(ServerInputControllerTest.class.getResource(path).getFile());
        try {
          data = Files.readString(file.toPath());
        } catch (IOException e) {
          System.out.println("Path to test data incorrect or other input error");
          e.printStackTrace();
        }

        // File must exist
        assertNotNull(data);
        return data;
      }
    }
    return new TestServer();
  }

  @Before
  public void setup() {
    input = new ServerInputController(getFakeServer(), 1, 1, 2020, 80);
  }

  @Test
  public void noFlyZoneLoadedCorrectly() {
    var noFlyZones = input.getNoFlyZones();

    assertEquals("There should be 4 buildings", 4, noFlyZones.size());
  }

  @Test
  public void sensorLocationsCorrect() {
    var sensorLocations = input.getSensorW3Ws();

    assertEquals("There should be 33 sensors to visit in one day", 33, sensorLocations.size());
    assertTrue("The words should exist", sensorLocations.get(0).getWords().length() > 0);

    double lng = sensorLocations.get(0).getCoordinates().x;
    double lat = sensorLocations.get(0).getCoordinates().y;
    assertTrue("The longitude should be sensible", -4 <= lng && lng <= -3);
    assertTrue("The latitude should be sensible", 55 <= lat && lat <= 56);
  }

  @Test
  public void sensorReadingCorrect() {
    var sensor = input.readSensor(input.getSensorW3Ws().get(0));

    assertTrue(
        "The battery should be displaying a sensible number",
        0 <= sensor.getBattery() && sensor.getBattery() <= 100);
    assertTrue("The sensor reading should exist", sensor.getReading().length() > 0);
  }
}
