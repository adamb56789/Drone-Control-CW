package uk.ac.ed.inf.aqmaps.io;

import uk.ac.ed.inf.aqmaps.Settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Outputs to the current directory of the filesystem */
public class FileOutputController implements OutputController {
  private final Settings settings;

  public FileOutputController(Settings settings) {
    this.settings = settings;
  }

  @Override
  public void outputFlightpath(String flightpathText) {
    String path =
        String.format(
            "flightpath-%02d-%02d-%04d.txt",
            settings.getDay(), settings.getMonth(), settings.getYear());
    try {
      Files.writeString(Path.of(path), flightpathText);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void outputMapGeoJSON(String json) {
    String path =
        String.format(
            "readings-%02d-%02d-%04d.geojson",
            settings.getDay(), settings.getMonth(), settings.getYear());
    try {
      Files.writeString(Path.of(path), json);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
