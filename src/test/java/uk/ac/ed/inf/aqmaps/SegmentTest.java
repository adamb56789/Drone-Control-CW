package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Segment;

import static org.junit.Assert.*;

public class SegmentTest {
    @Test
    public void segmentLengthCorrect() {
        var line = TestPaths.MIDDLE_OF_NOWHERE;
        var segment = new Segment(line.start, line.end);

        assertEquals(line.shortestPathLength, segment.length(), 0.000000001);
    }
}
