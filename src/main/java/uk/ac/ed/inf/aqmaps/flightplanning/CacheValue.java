package uk.ac.ed.inf.aqmaps.flightplanning;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

public class CacheValue {
    private final int length;
    private final Coords endPosition;

    public CacheValue(int length, Coords endPosition) {
        this.length = length;
        this.endPosition = endPosition;
    }

    public int getLength() {
        return length;
    }

    public Coords getEndPosition() {
        return endPosition;
    }
}
