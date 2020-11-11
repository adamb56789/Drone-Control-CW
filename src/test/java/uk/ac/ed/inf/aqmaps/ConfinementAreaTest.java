package uk.ac.ed.inf.aqmaps;

import org.junit.Test;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfinementAreaTest {
  @Test
  public void pointInsideConfinementIsInside() {
    var point = new Coords(-3.18612, 55.94329);
    assertTrue(ConfinementArea.isInConfinement(point));
  }

  @Test
  public void pointOutsideConfinementIsNotInside() {
    var point = new Coords(-3.18769, 55.94084);
    assertFalse(ConfinementArea.isInConfinement(point));
  }

  @Test
  public void pointOnBoundaryIsNotInside() {
    var point = new Coords(-3.192473, 55.946233);
    assertFalse(ConfinementArea.isInConfinement(point));
  }
}
