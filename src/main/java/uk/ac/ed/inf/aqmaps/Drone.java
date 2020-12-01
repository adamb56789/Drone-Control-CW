package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.flightplanning.FlightPlanner;
import uk.ac.ed.inf.aqmaps.io.InputController;
import uk.ac.ed.inf.aqmaps.io.OutputController;
import uk.ac.ed.inf.aqmaps.noflyzone.Obstacles;

import java.util.List;

/** Represents the drone. Performs route planning, than follows that plan to collect sensor data. */
public class Drone {
  private final Settings settings;
  private final InputController input;
  private final OutputController output;

  /**
   * @param settings the current Settings
   * @param input the InputController which handles data input
   * @param output the OutputController which handles data output
   */
  public Drone(Settings settings, InputController input, OutputController output) {
    this.settings = settings;
    this.input = input;
    this.output = output;
  }

  /** Start the drone and perform route planning and data collection for the given settings. */
  public void start() {
    // Create a flight plan and record it in the results
    var flightPlan = planRoute();
    System.out.printf("Flight plan is %d moves%n", flightPlan.size());

    var results = new Results(input.getSensorW3Ws());
    results.recordFlightpath(flightPlan);

    // Fly the route and collect sensor data
    flyRoute(flightPlan, results);

    // Output the flight path and GeoJSON map
    output.outputFlightpath(results.getFlightpathString());
    output.outputMapGeoJSON(results.getMapGeoJSON());
  }

  /**
   * Plan the route that the drone will follow.
   *
   * @return a list of Moves specifying the route
   */
  private List<Move> planRoute() {
    // Input and prepare the obstacle and sensor location data
    var obstacles = new Obstacles(input.getNoFlyZones());
    var sensorW3Ws = input.getSensorW3Ws();
    var flightPlanner =
        new FlightPlanner(
            obstacles, sensorW3Ws, settings.getRandomSeed(), settings.getMaxRunTime());

    // Run the flight planning algorithm
    return flightPlanner.createBestFlightPlan(settings.getStartCoords());
  }

  /**
   * Fly the drone along the route, collecting sensor data and adding it to the results.
   *
   * @param flightPlan the flight plan for the drone to follow
   * @param results the Results object to record the sensor data in
   */
  private void flyRoute(List<Move> flightPlan, Results results) {
    for (var move : flightPlan) {
      // Make the move and read the sensor, if there is one
      var sensorData = input.readSensor(move.getSensorW3W());

      // If we visited a sensor, record the data
      if (sensorData != null) {
        results.recordSensorReading(sensorData);
      }
    }
  }
}
