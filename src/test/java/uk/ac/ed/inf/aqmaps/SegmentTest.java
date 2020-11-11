package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.geometry.Segment;

import static org.junit.Assert.*;

public class SegmentTest {
  @Test
  public void segmentLengthCorrect() {
    var line = TestPaths.MIDDLE_OF_NOWHERE;
    var segment = new Segment(line.start, line.end);

    assertEquals(line.shortestPathLength, segment.length(), 0.000000001);
  }

  @Test
  public void angleQuadrant1Correct() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(1 + Math.sqrt(3), 2);
    assertEquals(Math.toRadians(30), Segment.angle(p1, p2), 0.00000000001);
  }

  @Test
  public void angleQuadrant2Correct() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(0, 1 + Math.sqrt(3));
    assertEquals(Math.toRadians(120), Segment.angle(p1, p2), 0.00000000001);
  }

  @Test
  public void angleQuadrant3Correct() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(1 - Math.sqrt(3), 0);
    assertEquals(Math.toRadians(-150), Segment.angle(p1, p2), 0.00000000001);
  }

  @Test
  public void angleQuadrant4Correct() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(2, 1 - Math.sqrt(3));
    assertEquals(Math.toRadians(-60), Segment.angle(p1, p2), 0.00000000001);
  }

  @Test
  public void angleVerticalCorrect() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(1, 2);
    assertEquals(Math.toRadians(90), Segment.angle(p1, p2), 0.00000000001);
  }

  @Test
  public void angleWestCorrect() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(0, 1);
    assertEquals(Math.toRadians(180), Segment.angle(p1, p2), 0.00000000001);
  }
}
