package uk.ac.ed.inf.aqmaps;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ed.inf.aqmaps.geometry.Angle;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class AngleTest {
  private static Stream<Arguments> provideArguments() {
    return Stream.of(
        Arguments.of("angle in quadrant 1", 1 + Math.sqrt(3), 2, 30),
        Arguments.of("angle in quadrant 2", 0, 1 + Math.sqrt(3), 120),
        Arguments.of("angle in quadrant 3", 1 - Math.sqrt(3), 0, -150),
        Arguments.of("angle in quadrant 4", 2, 1 - Math.sqrt(3), -60),
        Arguments.of("angle vertical", 1, 3, 90),
        Arguments.of("angle west", -1, 1, 180));
  }

  @ParameterizedTest
  @MethodSource("provideArguments")
  void lineDirectionCorrect(String description, double x, double y, double angle) {
    var p1 = new Coords(1, 1);
    var p2 = new Coords(x, y);
    assertEquals(description, Math.toRadians(angle), Angle.lineDirection(p1, p2), 0.00000000001);
  }
}
