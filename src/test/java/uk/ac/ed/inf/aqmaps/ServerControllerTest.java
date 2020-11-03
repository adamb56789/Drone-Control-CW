package uk.ac.ed.inf.aqmaps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServerControllerTest {
  private String getTestBuilding() {
    return "{\n"
        + "  \"type\": \"FeatureCollection\",\n"
        + "  \"features\": [\n"
        + "    {\n"
        + "      \"type\": \"Feature\",\n"
        + "      \"properties\": {\n"
        + "        \"name\": \"Test Building\",\n"
        + "        \"fill\": \"#ff0000\"\n"
        + "      },\n"
        + "      \"geometry\": {\n"
        + "        \"type\": \"Polygon\",\n"
        + "        \"coordinates\": [\n"
        + "          [\n"
        + "            [\n"
        + "              -3.1897,\n"
        + "              55.9428\n"
        + "            ],\n"
        + "            [\n"
        + "              -3.1894,\n"
        + "              55.9423\n"
        + "            ],\n"
        + "            [\n"
        + "              -3.1882,\n"
        + "              55.9425\n"
        + "            ],\n"
        + "            [\n"
        + "              -3.1885,\n"
        + "              55.9429\n"
        + "            ],\n"
        + "            [\n"
        + "              -3.1897,\n"
        + "              55.9428\n"
        + "            ]\n"
        + "          ]\n"
        + "        ]\n"
        + "      }\n"
        + "    }\n"
        + "  ]\n"
        + "}\n";
  }

  private String getTestMap() {
    return "[\n"
        + "  {\n"
        + "    \"location\": \"dent.shins.cycle\",\n"
        + "    \"battery\": 40.831715,\n"
        + "    \"reading\": \"null\"\n"
        + "  }\n"
        + "]";
  }

  private String getTestW3W() {
    return "{\n"
        + "  \"country\": \"GB\",\n"
        + "  \"square\": {\n"
        + "    \"southwest\": {\n"
        + "      \"lng\": -3.185597,\n"
        + "      \"lat\": 55.942863\n"
        + "    },\n"
        + "    \"northeast\": {\n"
        + "      \"lng\": -3.185549,\n"
        + "      \"lat\": 55.94289\n"
        + "    }\n"
        + "  },\n"
        + "  \"nearestPlace\": \"Edinburgh\",\n"
        + "  \"coordinates\": {\n"
        + "    \"lng\": -3.185573,\n"
        + "    \"lat\": 55.942877\n"
        + "  },\n"
        + "  \"words\": \"dent.shins.cycle\",\n"
        + "  \"language\": \"en\",\n"
        + "  \"map\": \"https://w3w.co/dent.shins.cycle\"\n"
        + "}\n";
  }

  private Server getServer() {
    class TestServer implements Server {

      @Override
      public String requestData(String url) {
        System.out.println(url);
        switch (url) {
          case "http://localhost:80/buildings/no-fly-zones.geojson":
            return getTestBuilding();
          case "http://localhost:80/maps/2000/01/01/air-quality-data.json":
            return getTestMap();
          case "http://localhost:80/words/dent/shins/cycle/details.json":
            return getTestW3W();
          default:
            throw new RuntimeException("Resource not found");
        }
      }
    }
    return new TestServer();
  }

  @Test
  public void noFlyZoneLoadedCorrectly() {
    var remote = new ServerController(getServer(), 1, 1, 2000, 80);
    var noFlyZones = remote.getNoFlyZones();

    assert noFlyZones.features() != null;
    assertEquals(
        "Test Building", noFlyZones.features().get(0).getStringProperty("name"));
    assertEquals(1, noFlyZones.features().size());
  }

  @Test
  public void sensorLocationsCorrect() {
    var remote = new ServerController(getServer(), 1, 1, 2000, 80);
    var sensorLocations = remote.getSensorLocations();

    assertEquals(1, sensorLocations.size());
    assertEquals("dent.shins.cycle", sensorLocations.get(0).getWords());
    assertEquals(-3.185573, sensorLocations.get(0).getCoordinates().lng, 0.0000000001);
    assertEquals(55.942877, sensorLocations.get(0).getCoordinates().lat, 0.0000000001);
  }

  @Test
  public void sensorReadingCorrect() {
    var remote = new ServerController(getServer(), 1, 1, 2000, 80);
    var sensor = remote.readSensor(remote.getSensorLocations().get(0));

    assertEquals(40.831715, sensor.getBattery(), 0.0000000001);
    assertEquals("null", sensor.getReading());
  }
}
