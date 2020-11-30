package uk.ac.ed.inf.aqmaps.flightplanning;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.Objects;

public class CacheKey {
  public static final int PRECISION = 14;
  private final double startLng;
  private final double startLat;
  private final Coords currentTarget;
  /**
   * Could be null.
   */
  private final Coords nextTarget;

  public CacheKey(Coords startPosition, Coords currentTarget, Coords nextTarget) {
    startLng = round(startPosition.x);
    startLat = round(startPosition.y);
    this.currentTarget = currentTarget;
    this.nextTarget = nextTarget;
  }

  public static double round(double value) {
    return Math.round(value * Math.pow(10, PRECISION)) / Math.pow(10, PRECISION);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CacheKey cacheKey = (CacheKey) o;
    return Double.compare(cacheKey.startLng, startLng) == 0 &&
            Double.compare(cacheKey.startLat, startLat) == 0 &&
            Objects.equals(currentTarget, cacheKey.currentTarget) &&
            Objects.equals(nextTarget, cacheKey.nextTarget);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startLng, startLat, currentTarget, nextTarget);
  }
}
