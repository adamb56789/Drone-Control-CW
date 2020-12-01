package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ed.inf.aqmaps.flightplanning.ConfinementArea;
import uk.ac.ed.inf.aqmaps.flightplanning.Obstacles;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ObstaclesTest {

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

  @Test
  public void obstacleEdgesNoCollision() {
    var testServer = ServerInputControllerTest.getFakeServer();
    var input = new ServerInputController(testServer, 1, 1, 2020, 80);
    var noFlyZones = input.getNoFlyZones();
    var obstacles = new Obstacles(noFlyZones);
    // The outline polygons are what Obstacles uses for its graph representation
    var outlinePointsList =
        noFlyZones.stream().map(Polygon::generateOutlinePoints).collect(Collectors.toList());

    // We test that the edges of the obstacles do not collide with the obstacle to check that the
    // paths around obstacles do not collide with the obstacle

    for (var outlinePoints : outlinePointsList) {
      for (int i = 0; i < outlinePoints.size() - 1; i++) {
        // If the point is not in confinement then it will always collide, so ignore it
        var p1 = outlinePoints.get(i);
        var p2 = outlinePoints.get(i + 1);
        if (ConfinementArea.isInConfinement(p1) && ConfinementArea.isInConfinement(p2)) {
          assertFalse(obstacles.lineCollision(p1, p2));
        }
      }
    }
  }

  @ParameterizedTest
  @MethodSource("lineCollisionArguments")
  public void lineCollisionCorrect(String description, TestPath testPath, boolean collision) {
    var testServer = ServerInputControllerTest.getFakeServer();
    var input = new ServerInputController(testServer, 1, 1, 2020, 80);
    var obstacles = new Obstacles(input.getNoFlyZones());
    assertEquals(description, collision, obstacles.lineCollision(testPath.start, testPath.end));
  }
}
