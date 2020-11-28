package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;
import uk.ac.ed.inf.aqmaps.flightplanning.Obstacles;

import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ObstaclesTest {
  private Obstacles obstacles;

  private static Stream<Arguments> lineCollisionArguments() {
    return Stream.of(
        Arguments.of("Middle of nowhere line no collision", TestPaths.MIDDLE_OF_NOWHERE, false),
        Arguments.of("Near buildings line no collision", TestPaths.NEAR_BUILDINGS, false),
        Arguments.of("leaves confinement line collides", TestPaths.LEAVES_CONFINEMENT, true),
        Arguments.of("Collides with 1 building line collides", TestPaths.COLLIDES_1_BUILDING, true),
        Arguments.of(
            "Tricky path through building line collides",
            TestPaths.TRICKY_PATH_THROUGH_BUILDINGS,
            true),
        Arguments.of(
            "Collides with 3 buildings line collides", TestPaths.COLLIDES_3_BUILDINGS, true),
        Arguments.of(
            "Shortest route leaves confinement line collides",
            TestPaths.SHORTEST_ROUTE_LEAVES_CONFINEMENT,
            true));
  }

  @Before
  public void setup() {
    var testServer = ServerInputControllerTest.getFakeServer();
    var input = new ServerInputController(testServer, 1, 1, 2020, 80);
    obstacles = new Obstacles(input.getNoFlyZones());
  }

  @Test
  public void numberOfPointsCorrect() {
    var points = obstacles.getOutlinePoints();
    assertEquals("The obstacle polygons should have a total of 36 vertices", 32, points.size());
  }

  @Test
  public void meetsCornersLineNoCollision() {
    var start = obstacles.getOutlinePoints().get(0);
    var end = obstacles.getOutlinePoints().get(1);
    assertFalse(obstacles.lineCollision(start, end));
  }

  @ParameterizedTest
  @MethodSource("lineCollisionArguments")
  public void formatAngle(String description, TestPath testPath, boolean collision) {
    var testServer = ServerInputControllerTest.getFakeServer();
    var input = new ServerInputController(testServer, 1, 1, 2020, 80);
    obstacles = new Obstacles(input.getNoFlyZones());
    assertEquals(description, collision, obstacles.lineCollision(testPath.start, testPath.end));
  }

  @Test
  public void pointNotInObstacleNoCollision() {
    assertFalse(obstacles.pointCollides(TestPaths.MIDDLE_OF_NOWHERE.start));
  }

  @Test
  public void pointOutsideConfinementCollision() {
    assertTrue(obstacles.pointCollides(TestPaths.LEAVES_CONFINEMENT.end));
  }

  @Test
  public void pointInsideObstacleCollision() {
    assertTrue(obstacles.pointCollides(new Coords(-3.186743, 55.944321)));
  }
}
