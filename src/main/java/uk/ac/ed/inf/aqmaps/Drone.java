package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.io.InputController;
import uk.ac.ed.inf.aqmaps.io.OutputController;
import uk.ac.ed.inf.aqmaps.pathfinding.FlightPlanner;
import uk.ac.ed.inf.aqmaps.pathfinding.ObstacleEvader;
import uk.ac.ed.inf.aqmaps.pathfinding.Obstacles;
import uk.ac.ed.inf.aqmaps.pathfinding.SensorGraph;

/** Represents the drone. Performs route planning, than follows that plan to collect sensor data. */
public class Drone {
  private final Settings settings;
  private final InputController input;
  private final OutputController output;

  public Drone(Settings settings, InputController input, OutputController output) {
    this.settings = settings;
    this.input = input;
    this.output = output;
  }

  /** Start the drone and perform route planning and data collection for the given settings. */
  public void start() {
    var obstacles = new Obstacles(input.getNoFlyZones());
    var obstacleEvader = new ObstacleEvader(obstacles);
    var sensorW3Ws = input.getSensorW3Ws();
    var sensorCoords = W3W.convertToCoords(sensorW3Ws);
    var flightPlanner = new FlightPlanner(obstacles, obstacleEvader, sensorW3Ws);

    var sensorGraph = new SensorGraph(obstacleEvader, flightPlanner, settings.getRandomSeed());

    var tour = sensorGraph.createSensorTour(settings.getStartCoords(), sensorCoords);

    var results = new Results(sensorW3Ws);
    //    results.recordFlightpath(droneNavigation.createFlightPlan(tour));

    System.out.println(results.getMapGeoJSON());
  }
}
