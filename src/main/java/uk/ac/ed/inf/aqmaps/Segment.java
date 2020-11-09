package uk.ac.ed.inf.aqmaps;

/**
 * A line segment from one coordinate point to another.
 */
public class Segment {
    private final Coords start;
    private final Coords end;

    public Segment(Coords start, Coords end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Calculates the length of this line segment using Euclidean distance
     * @return the length of the segment
     */
    public double length() {
        return 0; //TODO
    }

    /**
     * Calculates the Euclidean distance between two points a and b
     * @param a the start point
     * @param b the end point
     * @return the distance between the two points
     */
    public static double distance(Coords a, Coords b) {
        return 0; //TODO
    }
}
