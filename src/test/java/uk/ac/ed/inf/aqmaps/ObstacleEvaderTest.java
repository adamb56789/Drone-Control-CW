package uk.ac.ed.inf.aqmaps;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;
import uk.ac.ed.inf.aqmaps.pathfinding.ObstacleEvader;
import uk.ac.ed.inf.aqmaps.pathfinding.Obstacles;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class ObstacleEvaderTest {
  private static Stream<Arguments> provideArguments() {
    return Stream.of(
        Arguments.of("Middle of nowhere path", TestPaths.MIDDLE_OF_NOWHERE),
        Arguments.of("Near buildings path", TestPaths.NEAR_BUILDINGS),
        Arguments.of("Path collides with 1 building", TestPaths.COLLIDES_1_BUILDING),
        Arguments.of("Tricky path through buildings", TestPaths.TRICKY_PATH_THROUGH_BUILDINGS),
        Arguments.of("Path collides with 3 buildings", TestPaths.COLLIDES_3_BUILDINGS),
        Arguments.of(
            "Shortest path leaves confinement", TestPaths.SHORTEST_ROUTE_LEAVES_CONFINEMENT));
  }

  @ParameterizedTest
  @MethodSource("provideArguments")
  void shortestPathLengthCorrect(String description, TestPath path) {
    var testServer = ServerInputControllerTest.getFakeServer();
    var input = new ServerInputController(testServer, 1, 1, 2020, 80);
    var obstacleGraph = new ObstacleEvader(new Obstacles(input.getNoFlyZones()));

    assertEquals(
        description,
        path.shortestPathLength,
        obstacleGraph.getShortestPathLength(path.start, path.end),
        0.0000000001);
  }
}
