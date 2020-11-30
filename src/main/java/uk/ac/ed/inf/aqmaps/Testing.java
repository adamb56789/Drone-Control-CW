package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.io.FileOutputController;
import uk.ac.ed.inf.aqmaps.io.ServerInputController;

import java.util.ArrayList;
import java.util.List;

public class Testing {
  public static final Coords PRESCRIBED_START = new Coords(-3.188396, 55.944425);

  public void run() {
    getDates().forEach(this::runFlightPlansOnDate);
  }

  private void runFlightPlansOnDate(int[] date) {
    var settings = new Settings(date[0], date[1], date[2], PRESCRIBED_START, 0, 80);
    var drone =
        new Drone(
            settings, new ServerInputController(settings), new FileOutputController(settings));
    drone.start();
  }

  private List<int[]> getDates() {
    var dates = new ArrayList<int[]>();
    for (int i = 1; i <= 12; i++) {
      dates.add(new int[] {i, i, 2020});
    }
    return dates;
  }
}
