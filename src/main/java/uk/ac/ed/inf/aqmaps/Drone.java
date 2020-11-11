package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.io.InputController;
import uk.ac.ed.inf.aqmaps.io.OutputController;
import uk.ac.ed.inf.aqmaps.io.ServerController;

import java.util.ArrayList;
import java.util.List;

/** Represents the drone. Performs route planning, than follows that plan to collect sensor data. */
public class Drone {
  private final Settings settings;
  private final InputController input;
  private final OutputController output;

  public static List<Double>[] results = new List[7];

  public Drone(Settings settings, InputController input, OutputController output) {
    this.settings = settings;
    this.input = input;
    this.output = output;
  }

  /**
   * Start the drone and perform route planning and data collection for the given settings.
   */
  public void start() {
    for (int i = 0; i < results.length; i++) {
      results[i] = new ArrayList<>();
    }
    var input = new ServerController(settings);
    var obstacleGraph = new ObstacleGraph(new Obstacles(input.getNoFlyZones()));
    var sensorGraph = new SensorGraph(input.getSensorLocations(), obstacleGraph, settings.getRandomSeed());
    var path = sensorGraph.getTour(settings.getStartCoords());
  }
}
