package uk.ac.ed.inf.aqmaps;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class CoordsTest {

  private static Stream<Arguments> provideArguments() {
    return Stream.of(
        Arguments.of("angleTo() in quadrant 1", 1 + Math.sqrt(3), 2, 30),
        Arguments.of("angleTo() in quadrant 2", 0, 1 + Math.sqrt(3), 120),
        Arguments.of("angleTo() in quadrant 3", 1 - Math.sqrt(3), 0, -150),
        Arguments.of("angleTo() in quadrant 4", 2, 1 - Math.sqrt(3), -60),
        Arguments.of("angleTo() vertical", 1, 3, 90),
        Arguments.of("angleTo() west", -1, 1, 180));
  }

  @ParameterizedTest
  @MethodSource("provideArguments")
  void angleToCorrect(String description, double x, double y, double angle) {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(x, y);
    assertEquals(description, Math.toRadians(angle), p1.angleTo(p2), 0.00000000001);
  }

  @ParameterizedTest
  @MethodSource("provideArguments")
  void getPositionAfterMoveCorrect(String description, double x, double y, double angle) {
    var p1 = new Coords(1, 1);
    var expectedP = new Coords(x, y);
    var actualP = p1.getPositionAfterMoveRadians(Math.toRadians(angle), 2);

    assertEquals(description, expectedP.x, actualP.x, 0.00000000001);
    assertEquals(description, expectedP.y, actualP.y, 0.00000000001);
  }
}
