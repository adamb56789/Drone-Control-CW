package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import static org.junit.Assert.assertEquals;

public class CoordsTest {
  @Test
  public void angleQuadrant1Correct() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(1 + Math.sqrt(3), 2);
    assertEquals(Math.toRadians(30), p1.angleTo(p2), 0.00000000001);
  }

  @Test
  public void angleQuadrant2Correct() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(0, 1 + Math.sqrt(3));
    assertEquals(Math.toRadians(120), p1.angleTo(p2), 0.00000000001);
  }

  @Test
  public void angleQuadrant3Correct() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(1 - Math.sqrt(3), 0);
    assertEquals(Math.toRadians(-150), p1.angleTo(p2), 0.00000000001);
  }

  @Test
  public void angleQuadrant4Correct() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(2, 1 - Math.sqrt(3));
    assertEquals(Math.toRadians(-60), p1.angleTo(p2), 0.00000000001);
  }

  @Test
  public void angleVerticalCorrect() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(1, 2);
    assertEquals(Math.toRadians(90), p1.angleTo(p2), 0.00000000001);
  }

  @Test
  public void angleWestCorrect() {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(0, 1);
    assertEquals(Math.toRadians(180), p1.angleTo(p2), 0.00000000001);
  }

  @Test
  public void moveQuadrant1Correct() {
    var p1 = new Coords(1, 1);
    var expectedP = new Coords(1 + Math.sqrt(3), 2);
    var actualP = p1.moveInDirection(Math.toRadians(30), 2);

    assertEquals(expectedP.x, actualP.x, 0.00000000001);
    assertEquals(expectedP.y, actualP.y, 0.00000000001);
  }

  @Test
  public void moveQuadrant2Correct() {
    var p1 = new Coords(1, 1);
    var expectedP = new Coords(0, 1 + Math.sqrt(3));
    var actualP = p1.moveInDirection(Math.toRadians(120), 2);

    assertEquals(expectedP.x, actualP.x, 0.00000000001);
    assertEquals(expectedP.y, actualP.y, 0.00000000001);
  }

  @Test
  public void moveQuadrant3Correct() {
    var p1 = new Coords(1, 1);
    var expectedP = new Coords(1 - Math.sqrt(3), 0);
    var actualP = p1.moveInDirection(Math.toRadians(-150), 2);

    assertEquals(expectedP.x, actualP.x, 0.00000000001);
    assertEquals(expectedP.y, actualP.y, 0.00000000001);
  }

  @Test
  public void moveQuadrant4Correct() {
    var p1 = new Coords(1, 1);
    var expectedP = new Coords(2, 1 - Math.sqrt(3));
    var actualP = p1.moveInDirection(Math.toRadians(-60), 2);

    assertEquals(expectedP.x, actualP.x, 0.00000000001);
    assertEquals(expectedP.y, actualP.y, 0.00000000001);
  }

  @Test
  public void moveVerticalCorrect() {
    var p1 = new Coords(1, 1);
    var expectedP = new Coords(1, 2);
    var actualP = p1.moveInDirection(Math.toRadians(90), 1);

    assertEquals(expectedP.x, actualP.x, 0.00000000001);
    assertEquals(expectedP.y, actualP.y, 0.00000000001);
  }

  @Test
  public void moveWestCorrect() {
    var p1 = new Coords(1, 1);
    var expectedP = new Coords(0, 1);
    var actualP = p1.moveInDirection(Math.toRadians(180), 1);

    assertEquals(expectedP.x, actualP.x, 0.00000000001);
    assertEquals(expectedP.y, actualP.y, 0.00000000001);
  }
}
