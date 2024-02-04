package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * This record class represents the parameters of the background map presented in the graphic interface.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record MapViewParameters(int zoomLevel, double xTopLeftCoord, double yTopLeftCoord) {
	/**
	 * Transforms the top-left corner coordinates of the displayed map section in Point2D representation.
	 *
	 * @return the Point2D representation of the top-left corner coordinates.
	 */
	public Point2D topLeft() {
		return new Point2D(xTopLeftCoord, yTopLeftCoord);
	}

	/**
	 * Clones the actual object with new top-left corner coordinates.
	 *
	 * @param xTopLeftCoord the x-coordinate of the top-left corner of the displayed map section.
	 * @param yTopLeftCoord the y-coordinate of the top-left corner of the displayed map section.
	 * @return the cloned object.
	 */
	public MapViewParameters withMinXY(double xTopLeftCoord, double yTopLeftCoord) {
		return new MapViewParameters(this.zoomLevel, xTopLeftCoord, yTopLeftCoord);
	}

	/**
	 * Transforms the given point expressed according to the top-left corner to the corresponding PointWebMercator.
	 *
	 * @param x the x-coordinate expressed according to the top-left corner of the displayed map section (positive).
	 * @param y the y-coordinate expressed according to the top-left corner of the displayed map section (positive).
	 * @return the PointWebMercator corresponding to the coordinates.
	 */
	public PointWebMercator pointAt(double x, double y) {
		return PointWebMercator.of(zoomLevel, xTopLeftCoord + x, yTopLeftCoord - y);
	}

	/**
	 * Gives the x-position of a point Web Mercator, expressed in relation to the top-left corner
	 * of the map portion displayed on the screen.
	 *
	 * @param pointWeb a point in the Web Mercator representation.
	 * @return the x-position of a point Web Mercator.
	 */
	public double viewX(PointWebMercator pointWeb) {
		return pointWeb.xAtZoomLevel(zoomLevel);
	}

	/**
	 * Gives the y-position of a point Web Mercator, expressed in relation to the top-left corner
	 * of the map portion displayed on the screen.
	 *
	 * @param pointWeb a point in the Web Mercator representation.
	 * @return the y-position of a point Web Mercator.
	 */
	public double viewY(PointWebMercator pointWeb) {
		return pointWeb.yAtZoomLevel(zoomLevel);
	}
}
