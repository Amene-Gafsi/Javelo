package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * This record class represents a point in the Web Mercator coordinate system.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record PointWebMercator(double x, double y) {

	/**
	 *  A map side (square) is of 256 pixels. This constant defines the binary exponent to obtain this value (2^8=256).
	 *  It can then be used in various calculations related to zoom levels.
	 */
	private final static int MAP_SIDE_BINARY_EXPONENT = 8;

	/**
	 * Compact constructor.
	 *
	 * @throws IllegalArgumentException if x or y are not in the range [0,1].
	 */
	public PointWebMercator {
		Preconditions.checkArgument(x >= 0 && x <= 1 && y >= 0 && y <= 1);
	}

	/**
	 * Gives the Web Mercator point whose coordinates are x and y at the "zoomLevel".
	 * This method uses Math.scalb.
	 *
	 * @param zoomLevel the zoom level.
	 * @param x         the x coordinate in the Web Mercator system.
	 * @param y         the y coordinate in the Web Mercator system.
	 * @return the point whose coordinates are x and y at the "zoomLevel".
	 */
	public static PointWebMercator of(int zoomLevel, double x, double y) {
		double xZoom = Math.scalb(x, -(zoomLevel + MAP_SIDE_BINARY_EXPONENT));
		double yZoom = Math.scalb(y, -(zoomLevel + MAP_SIDE_BINARY_EXPONENT));
		return new PointWebMercator(xZoom, yZoom);
	}

	/**
	 * Converts a point in the Swiss coordinate system into the same point in the Web Mercator system.
	 *
	 * @param pointCh point that is represented in the Swiss coordinate system :
	 *                the swiss coordinates are "e" (east coordinate) and "n" (north coordinate).
	 * @return the Web Mercator point corresponding to the point in the Swiss coordinate system.
	 */
	public static PointWebMercator ofPointCh(PointCh pointCh) {
		double lon = pointCh.lon();
		double lat = pointCh.lat();
		double x = WebMercator.x(lon);
		double y = WebMercator.y(lat);
		return new PointWebMercator(x, y);
	}

	/**
	 * Gives the x coordinate of the Web Mercator system at the given zoom level.
	 * This method uses Math.scalb.
	 *
	 * @param zoomLevel the zoom level.
	 * @return the x coordinate at the given zoom level.
	 */
	public double xAtZoomLevel(int zoomLevel) {
		return Math.scalb(this.x, zoomLevel + MAP_SIDE_BINARY_EXPONENT);
	}

	/**
	 * Gives the y coordinate of the Web Mercator system at the given zoom level.
	 * This method uses Math.scalb.
	 *
	 * @param zoomLevel the zoom level.
	 * @return the y coordinate at the given zoom level.
	 */
	public double yAtZoomLevel(int zoomLevel) {
		return Math.scalb(this.y, zoomLevel + MAP_SIDE_BINARY_EXPONENT);
	}

	/**
	 * Gives the longitude of the Web Mercator point in radians.
	 *
	 * @return the longitude of the point in radians.
	 */
	public double lon() {
		return WebMercator.lon(this.x);
	}

	/**
	 * Gives the latitude of the Web Mercator point in radians.
	 *
	 * @return the latitude of the point in radians.
	 */
	public double lat() {
		return WebMercator.lat(this.y);
	}

	/**
	 * Gives the Swiss coordinate point at the same position as "this".
	 *
	 * @return the Swiss coordinate point at the same position as "this"
	 * or null if this point is not within the range of Switzerland defined by SwissBounds.
	 */
	public PointCh toPointCh() {
		double lon = this.lon();
		double lat = this.lat();
		double e = Ch1903.e(lon, lat);
		double n = Ch1903.n(lon, lat);

		if (!SwissBounds.containsEN(e, n)) return null;

		return new PointCh(e, n);
	}
}
