package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.io.InputController;
import uk.ac.ed.inf.aqmaps.io.OutputController;
import uk.ac.ed.inf.aqmaps.pathfinding.FlightPlanner;
import uk.ac.ed.inf.aqmaps.pathfinding.ObstacleEvader;
import uk.ac.ed.inf.aqmaps.pathfinding.Obstacles;

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
    var flightPlanner =
        new FlightPlanner(obstacles, obstacleEvader, sensorW3Ws, settings.getRandomSeed());

    var results = new Results(sensorW3Ws);
    results.recordFlightpath(flightPlanner.createFlightPlan(settings.getStartCoords()));

    System.out.println(results.getMapGeoJSON());
  }
}
