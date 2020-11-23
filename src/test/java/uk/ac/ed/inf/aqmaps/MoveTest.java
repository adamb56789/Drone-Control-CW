package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import static org.junit.Assert.assertEquals;

public class MoveTest {

  @Test
  public void toStringCorrectWhenSensorExists() {
    var start = new Coords(-1.23456789, 9.87654321);
    var end = new Coords(-3.14159265359, 2.71828182845);
    var w3W = new W3W(new Coords(0, 0), "seiso.yubi.yabai");
    var testmove = new Move(start, end, 110, w3W);

    assertEquals(
        "-1.23456789,9.87654321,110,-3.14159265359,2.71828182845,seiso.yubi.yabai",
        testmove.toString());
  }

  @Test
  public void toStringCorrectWhenSensorNotExists() {
    var start = new Coords(-1.23456789, 9.87654321);
    var end = new Coords(-3.14159265359, 2.71828182845);
    var testmove = new Move(start, end, 110, null);

    assertEquals(
            "-1.23456789,9.87654321,110,-3.14159265359,2.71828182845,null",
            testmove.toString());
  }
}
