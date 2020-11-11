package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

public class TestPath {
    public final Coords start;
    public final Coords end;
    public final double shortestPathLength;

    public TestPath(double x1, double y1, double x2, double y2, double shortestPathLength) {
        this.start = new Coords(x1, y1);
        this.end = new Coords(x2, y2);
        this.shortestPathLength = shortestPathLength;
    }
}
