package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ed.inf.aqmaps.geometry.Angle;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class AngleTest {
  private static Stream<Arguments> lineDirectionArguments() {
    return Stream.of(
        Arguments.of("angle in quadrant 1", 1 + Math.sqrt(3), 2, 30),
        Arguments.of("angle in quadrant 2", 0, 1 + Math.sqrt(3), 120),
        Arguments.of("angle in quadrant 3", 1 - Math.sqrt(3), 0, -150),
        Arguments.of("angle in quadrant 4", 2, 1 - Math.sqrt(3), -60),
        Arguments.of("angle vertical", 1, 3, 90),
        Arguments.of("angle west", -1, 1, 180));
  }

  private static Stream<Arguments> roundTo10Arguments() {
    return Stream.of(
        Arguments.of(0, 0),
        Arguments.of(5, 10),
        Arguments.of(42, 40),
        Arguments.of(-5, 0),
        Arguments.of(-47, -50),
        Arguments.of(169, 170));
  }

  private static Stream<Arguments> formatAngleArguments() {
    return Stream.of(
        Arguments.of(0, 0),
        Arguments.of(90, 90),
        Arguments.of(300, 300),
        Arguments.of(400, 40),
        Arguments.of(-10, 350),
        Arguments.of(360, 0));
  }

  @ParameterizedTest
  @MethodSource("lineDirectionArguments")
  public void lineDirectionCorrect(String description, double x, double y, double angle) {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(x, y);
    assertEquals(description, Math.toRadians(angle), Angle.lineDirection(p1, p2), 1e-12);
  }

  @Test
  public void bisectorDirectionCorrect() {
    var x = new Coords(0, 0);
    var a = new Coords(0, -1);
    var b = new Coords(1, 0);
    assertEquals(Math.toRadians(-45), Angle.bisectorDirection(x, a, b), 1e-12);
  }

  @Test
  public void bisectorDirectionCorrect2() {
    var x = new Coords(0, 0);
    var a = new Coords(1, 0);
    var b = new Coords(0, 1);
    assertEquals(Math.toRadians(45), Angle.bisectorDirection(x, a, b), 1e-12);
  }

  @Test
  public void bisectorDirectionCorrect3() {
    var x = new Coords(0, 0);
    var a = new Coords(0, 1);
    var b = new Coords(-1, 0);
    assertEquals(Math.toRadians(135), Angle.bisectorDirection(x, a, b), 1e-12);
  }

  @Test
  public void bisectorDirectionCorrect4() {
    var x = new Coords(0, 0);
    var a = new Coords(-1, 0);
    var b = new Coords(0, -1);
    assertEquals(Math.toRadians(225), Angle.bisectorDirection(x, a, b), 1e-12);
  }

  @ParameterizedTest
  @MethodSource("roundTo10Arguments")
  public void roundTo10Correct(int angle, int roundedAngle) {
    assertEquals(roundedAngle, Angle.roundTo10Degrees(Math.toRadians(angle)));
  }

  @ParameterizedTest
  @MethodSource("formatAngleArguments")
  public void formatAngle(int angle, int formattedAngle) {
    assertEquals(formattedAngle, Angle.formatAngle(angle));
  }
}
