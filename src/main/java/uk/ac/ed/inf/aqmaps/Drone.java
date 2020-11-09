package uk.ac.ed.inf.aqmaps;

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

  /**
   * Start the drone and perform route planning and data collection for the given settings.
   */
  public void start() {
    //TODO
  }
}
