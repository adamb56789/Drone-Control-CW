package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.FeatureCollection;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.io.OutputController;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DroneTest {

    @Test
    public void droneCreatesAndOutputsFiles() {
        var input = new ServerInputController(ServerInputControllerTest.getFakeServer(), 1, 1, 2020, 80);
        var settings = new Settings(new String[]{"1", "1", "2020", "55.944425", "-3.188396", "0", "80", "0", "0.2"});

        // Create a fake output controller which tests the strings instead of outputting them
        var output = new OutputController() {
            @Override
            public void outputFlightpath(String flightpathText) {
                // No need to do extensive testing since this string is tested in ResultsTest
                assertTrue(flightpathText.length() > 0);
                assertTrue(flightpathText.split("\n").length <= 151);
            }

            @Override
            public void outputMapGeoJSON(String json) {
                var features = FeatureCollection.fromJson(json);
                // Again, no need for complex checks as it is covered in ResultsTest
                assert features.features() != null;
                assertEquals(34, features.features().size());
            }
        };

        var drone = new Drone(settings, input, output);
        drone.start();
    }
}
