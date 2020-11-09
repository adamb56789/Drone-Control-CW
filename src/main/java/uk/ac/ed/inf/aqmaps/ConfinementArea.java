package uk.ac.ed.inf.aqmaps;

/**
 * Holds information about the confinement area and checking whether a point is inside it.
 */
public class ConfinementArea {
    /** A Point representing the northwest corner of the confinement area. */
    private static final Coords TOP_LEFT = new Coords(-3.192473, 55.946233);
    /** A Point representing the southeast corner of the confinement area. */
    private static final Coords BOTTOM_RIGHT = new Coords(-3.184319, 55.942617);

    public static boolean isInConfinement(Coords point) {
        return true; //TODO
    }
}
