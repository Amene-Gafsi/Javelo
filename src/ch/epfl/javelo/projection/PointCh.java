package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

/**
 * This record class represents a point in the Swiss coordinate system.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record PointCh(double e, double n) {
	/**
	 * Compact Constructor
	 *
	 * @param e the east coordinate in the CH1903+ system.
	 * @param n the north coordinate in the CH1903+ system.
	 * @throws IllegalArgumentException if the coordinates do not belong to Switzerland.
	 */
	public PointCh {
		Preconditions.checkArgument(SwissBounds.containsEN(e, n));
	}

	/**
	 * Gives the square of the distance between "this" and "that".
	 * This method uses Math.pow.
	 *
	 * @param that an instance of this class.
	 * @return the square of the distance between this and the argument that.
	 */
	public double squaredDistanceTo(PointCh that) {
		double distanceE = this.e - that.e;
		double distanceN = this.n - that.n;
		return Math2.squaredNorm(distanceE, distanceN);
	}

	/**
	 * Gives the distance between "this" and "that".
	 * This method uses Math.sqrt.
	 *
	 * @param that an object of this class.
	 * @return the distance between this and that.
	 */
	public double distanceTo(PointCh that) {
		return Math.sqrt(squaredDistanceTo(that));
	}


	/**
	 * Gives the longitude in radians.
	 *
	 * @return the longitude in radians in the WGS84 system.
	 */
	public double lon() {
		return Ch1903.lon(e, n);
	}

	/**
	 * Gives the latitude in radians.
	 *
	 * @return the latitude in radians in the WGS84 system.
	 */
	public double lat() {
		return Ch1903.lat(e, n);
	}
}
