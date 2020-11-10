package uk.ac.ed.inf.aqmaps;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ed.inf.aqmaps.io.ServerController;

import static org.junit.Assert.assertEquals;

public class ObstacleGraphTest {
  private ObstacleGraph obstacleGraph;

  @Before
  public void setup() {
    var testServer = ServerControllerTest.getServer();
    var input = new ServerController(testServer, 1, 1, 2020, 80);
    obstacleGraph = new ObstacleGraph(new Obstacles(input.getNoFlyZones()));
  }

  @Test
  public void middleOfNowherePathLengthCorrect() {
    var path = TestPaths.MIDDLE_OF_NOWHERE;
    assertEquals(
        "The length of the shortest path is computed correctly",
        path.shortestPathLength,
        obstacleGraph.getShortestPathLength(path.start, path.end), 0.0000000001);
  }

  @Test
  public void nearBuildingsPathLengthCorrect() {
    var path = TestPaths.NEAR_BUILDINGS;
    assertEquals(
            "The length of the shortest path is computed correctly",
            path.shortestPathLength,
            obstacleGraph.getShortestPathLength(path.start, path.end), 0.0000000001);
  }

  @Test
  public void collidesWith1BuildingPathLengthCorrect() {
    var path = TestPaths.COLLIDES_BUILDING;
    assertEquals(
            "The length of the shortest path is computed correctly",
            path.shortestPathLength,
            obstacleGraph.getShortestPathLength(path.start, path.end), 0.0000000001);
  }

  @Test
  public void trickyPathThroughBuildingPathLengthCorrect() {
    var path = TestPaths.TRICKY_PATH_THROUGH_BUILDINGS;
    assertEquals(
            "The length of the shortest path is computed correctly",
            path.shortestPathLength,
            obstacleGraph.getShortestPathLength(path.start, path.end), 0.0000000001);
  }

  @Test
  public void collidesWith3BuildingsPathLengthCorrect() {
    var path = TestPaths.COLLIDES_3_BUILDINGS;
    assertEquals(
            "The length of the shortest path is computed correctly",
            path.shortestPathLength,
            obstacleGraph.getShortestPathLength(path.start, path.end), 0.0000000001);
  }

  @Test
  public void shortestRouteLeavesConfinementPathLengthCorrect() {
    var path = TestPaths.SHORTEST_ROUTE_LEAVES_CONFINEMENT;
    assertEquals(
            "The length of the shortest path is computed correctly",
            path.shortestPathLength,
            obstacleGraph.getShortestPathLength(path.start, path.end), 0.0000000001);
  }
}
