package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

/**
 * This record class represents the point of an itinerary closest to a given reference point.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record RoutePoint(PointCh point, double position, double distanceToReference) {

	/**
	 * Represents a non-existent point.
	 */
	public static final RoutePoint NONE = new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY);

	/**
	 * Gives a point identical to the receiver (this) but whose position is offset by the given difference,
	 * which can be positive or negative. This method is intended to be used to transform a point whose position
	 * is expressed with respect to the segment that contains it into an equivalent point but whose position is
	 * expressed with respect to the complete route.
	 *
	 * @param positionDifference the difference between two frames of reference.
	 * @return point identical to the receiver (this) but whose position is shifted by the "positionDifference".
	 */
	public RoutePoint withPositionShiftedBy(double positionDifference) {
		return new RoutePoint(this.point, this.position + positionDifference, this.distanceToReference);
	}

	/**
	 * Returns "this" if its distance to the reference is less than or equal to "that", and that otherwise.
	 * Returns the closest RoutePoint (this or that) to the reference point.
	 *
	 * @param that the RoutePoint against which the distance to the reference is compared.
	 * @return "this" if its distance to the reference is less than or equal to "that", and that otherwise.
	 */
	public RoutePoint min(RoutePoint that) {
		if (this.distanceToReference <= that.distanceToReference) return this;
		return that;
	}

	/**
	 * Returns "this" if its distance to the reference is less than or equal to "that",
	 * and a new instance of RoutePoint whose attributes are the arguments passed to min otherwise.
	 *
	 * @param thatPoint               the point on the itinerary.
	 * @param thatPosition            the position of the point along the route, in meters.
	 * @param thatDistanceToReference the difference between two frames of reference.
	 * @return "this" if its distance to the reference is less than or equal to "that",
	 * and a new instance of RoutePoint whose attributes are the arguments passed to min otherwise.
	 */
	public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
		if (this.distanceToReference <= thatDistanceToReference) return this;
		return new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
	}

}
