package uk.ac.ed.inf.aqmaps.io;

import uk.ac.ed.inf.aqmaps.Move;
import uk.ac.ed.inf.aqmaps.Sensor;

import java.util.List;

public class FileOutputController implements OutputController {
  @Override
  public void flightpath(List<Move> moves) {}

  @Override
  public void readings(List<Move> moves, List<Sensor> sensors) {}
}
