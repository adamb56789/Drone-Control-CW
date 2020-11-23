package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ResultsTest {

  @Test
  public void flightpathStringCorrect() {
    var w3W = new W3W(new Coords(0, 0), "seiso.yubi.yabai");
    var move1 =
        new Move(TestPaths.MIDDLE_OF_NOWHERE.start, TestPaths.MIDDLE_OF_NOWHERE.end, 110, w3W);
    var move2 = new Move(TestPaths.NEAR_BUILDINGS.start, TestPaths.NEAR_BUILDINGS.end, 0, null);

    var results = new Results();
    results.addFlightpath(List.of(move1, move2));

    assertEquals(
        "1,-3.190433,55.945481,110,-3.189062,55.94351,seiso.yubi.yabai\n"
            + "2,-3.186153,55.944796,0,-3.187708,55.944453,null\n",
        results.getFlightpathString());
  }
}
