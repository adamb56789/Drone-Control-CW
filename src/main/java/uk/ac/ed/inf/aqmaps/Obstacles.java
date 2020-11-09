package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.FeatureCollection;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/** Holds information about the obstacles or no-fly zones that the drone must avoid. */
public class Obstacles {
  private final FeatureCollection mapbox;
  private final List<Point2D> points;
  private final List<Line2D> lineSegments;
  private final List<Polygon> polygons;

  public Obstacles(FeatureCollection mapbox) {
    this.mapbox = mapbox;
    this.points = new ArrayList<>();
    this.lineSegments = new ArrayList<>();
    this.polygons = new ArrayList<>();
  }

  public FeatureCollection getMapbox() {
    return mapbox;
  }

  public List<Point2D> getPoints() {
    return points;
  }

  public List<Line2D> getLineSegments() {
    return lineSegments;
  }

  public List<Polygon> getPolygons() {
    return polygons;
  }
}
