package uk.ac.ed.inf.aqmaps;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class CoordsTest {

  private static Stream<Arguments> lineDirectionArguments() {
    return Stream.of(
        Arguments.of("angle in quadrant 1", 1 + Math.sqrt(3), 2, 30),
        Arguments.of("angle in quadrant 2", 0, 1 + Math.sqrt(3), 120),
        Arguments.of("angle in quadrant 3", 1 - Math.sqrt(3), 0, -150),
        Arguments.of("angle in quadrant 4", 2, 1 - Math.sqrt(3), -60),
        Arguments.of("angle vertical", 1, 3, 90),
        Arguments.of("angle west", -1, 1, 180));
  }

  private static Stream<Arguments> getPositionAfterMoveArguments() {
    return Stream.of(
        Arguments.of("angle in quadrant 1", 1 + Math.sqrt(3), 2, 30),
        Arguments.of("angle in quadrant 2", 0, 1 + Math.sqrt(3), 120),
        Arguments.of("angle in quadrant 3", 1 - Math.sqrt(3), 0, -150),
        Arguments.of("angle in quadrant 4", 2, 1 - Math.sqrt(3), -60),
        Arguments.of("angle vertical", 1, 3, 90),
        Arguments.of("angle west", -1, 1, 180));
  }

  private static Stream<Arguments> roundedDirection10DegreesArguments() {
    return Stream.of(
        Arguments.of(0, 1, 0, 90),
        Arguments.of(-1, 0, 10, 190),
        Arguments.of(0, -1, -10, 260),
        Arguments.of(1, 0, 0, 0),
        Arguments.of(0, -1, 90, 0),
        Arguments.of(1, 1.1, 0, 50),
        Arguments.of(1.1, -1, -180, 140));
  }

  private static Stream<Arguments> bisectorDirectionArguments() {
    return Stream.of(
        Arguments.of(1, 0, 1, 0, 0),
        Arguments.of(0, -1, 1, 0, -45),
        Arguments.of(1, 0, 0, 1, 45),
        Arguments.of(0, 1, -1, 0, 135),
        Arguments.of(-1, 0, 0, -1, 225),
        Arguments.of(-1, 1, -1, -1, 180),
        Arguments.of(1, -1, -1, 0, 247.5));
  }

  @ParameterizedTest
  @MethodSource("lineDirectionArguments")
  public void lineDirectionCorrect(String description, double x, double y, double angle) {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(x, y);
    assertEquals(description, Math.toRadians(angle), p1.directionTo(p2), 1e-12);
  }

  @ParameterizedTest
  @MethodSource("roundedDirection10DegreesArguments")
  public void roundedDirection10DegreesCorrect(double a1, double a2, int offset, int correct) {
    var p = new Coords(0, 0);
    var a = new Coords(a1, a2);
    assertEquals(correct, p.roundedDirection10Degrees(a, offset));
  }

  @ParameterizedTest
  @MethodSource("bisectorDirectionArguments")
  public void bisectorDirectionCorrect(double a1, double a2, double b1, double b2, double correct) {
    var x = new Coords(0, 0);
    var a = new Coords(a1, a2);
    var b = new Coords(b1, b2);
    assertEquals(Math.toRadians(correct), x.bisectorDirection(a, b), 1e-12);
  }

  @ParameterizedTest
  @MethodSource("getPositionAfterMoveArguments")
  void getPositionAfterMoveCorrect(String description, double x, double y, double angle) {
    var p1 = new Coords(1, 1);
    var expectedP = new Coords(x, y);
    var actualP = p1.getPositionAfterMoveRadians(Math.toRadians(angle), 2);

    assertEquals(description, expectedP.x, actualP.x, 0.00000000001);
    assertEquals(description, expectedP.y, actualP.y, 0.00000000001);
  }
}
