package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

/**
 * This class provides static methods to convert WGS 84 and Web Mercator coordinates.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class WebMercator {

	private WebMercator() {
	}

	/**
	 * Transforms the longitude into the x coordinate of the Web Mercator system.
	 *
	 * @param lon the longitude.
	 * @return the x coordinate of the projection of a point at longitude "lon" given in radians.
	 */
	public static double x(double lon) {
		return (1.0 / (2.0 * Math.PI)) * (lon + Math.PI);
	}

	/**
	 * Transforms the latitude into the y coordinate of the Web Mercator system.
	 *
	 * @param lat the latitude.
	 * @return the y coordinate of the projection of a point at latitude "lat" given in radians.
	 */
	public static double y(double lat) {
		return (1.0 / (2.0 * Math.PI)) * (Math.PI - Math2.asinh(Math.tan(lat)));
	}

	/**
	 * Transforms the x coordinate into the longitude.
	 *
	 * @param x the x coordinate of the Web Mercator system.
	 * @return the longitude in radians of a point whose projection is at the given x coordinate.
	 */
	public static double lon(double x) {
		return (2.0 * Math.PI * x) - Math.PI;
	}

	/**
	 * Transforms the y coordinate into the latitude.
	 *
	 * @param y the y coordinate of the Web Mercator system.
	 * @return the latitude in radians of a point whose projection is at the given y coordinate.
	 */
	public static double lat(double y) {
		return Math.atan(Math.sinh(Math.PI - 2.0 * Math.PI * y));
	}
}
