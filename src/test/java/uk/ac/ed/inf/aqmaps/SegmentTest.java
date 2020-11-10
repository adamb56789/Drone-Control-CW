package uk.ac.ed.inf.aqmaps;

import org.junit.Test;

import static org.junit.Assert.*;

public class SegmentTest {
    @Test
    public void segmentLengthCorrect() {
        var start = new Coords(-3.18612, 55.94329);
        var end = new Coords(-3.19612, 55.99329);
        var segment = new Segment(start, end);

        // sqrt((-3.18612--3.19612)^2+(55.94329-55.99329)^2) = 0.05099019513
        assertEquals(0.05099019513, segment.length(), 0.0000000001);
    }
}
