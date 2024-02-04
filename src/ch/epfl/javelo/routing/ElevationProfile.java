package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleUnaryOperator;

/**
 * This class represents the profile of the itinerary.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class ElevationProfile {

	private final double length;
	private final float[] elevationSamples;
	private double maxElevation = 0.0;
	private double minElevation = 0.0;
	private double totalAscent = 0.0;
	private double totalDescent = 0.0;
	private DoubleUnaryOperator elevationAtFnc;

	/**
	 * Constructor.
	 * In this constructor we directly calculate the maxElevation, the minElevation, the totalAscent and the totalDescent.
	 *
	 * @param length           the length of the profile.
	 * @param elevationSamples the elevation samples.
	 * @throws IllegalArgumentException preconditions not checked: length must be > 0 and there must be at
	 * least 2 elements in the elevations samples array.
	 */
	public ElevationProfile(double length, float[] elevationSamples) {
		this.length = length;
		this.elevationSamples = elevationSamples.clone();

		Preconditions.checkArgument(length > 0 && elevationSamples.length >= 2);

		DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();


		for (int i = 0; i < elevationSamples.length - 1; ++i) {

			statistics.accept(elevationSamples[i]);

			if (elevationSamples[i + 1] >= elevationSamples[i]) {
				totalAscent += elevationSamples[i + 1] - elevationSamples[i];
			} else {
				totalDescent += elevationSamples[i] - elevationSamples[i + 1];
			}
		}

		statistics.accept(elevationSamples[elevationSamples.length - 1]);

		this.maxElevation = (float) statistics.getMax();
		this.minElevation = (float) statistics.getMin();
	}

	/**
	 * Gives the length of the profile, in meters.
	 *
	 * @return length of the profile, in meters.
	 */
	public double length() {
		return length;
	}

	/**
	 * Gives the minimum altitude of the profile, in meters.
	 *
	 * @return the minimum altitude of the profile, in meters.
	 */
	public double minElevation() {
		return minElevation;
	}

	/**
	 * Gives the maximum altitude of the profile, in meters.
	 *
	 * @return the maximum altitude of the profile, in meters.
	 */
	public double maxElevation() {
		return maxElevation;
	}

	/**
	 * Gives the total positive elevation of the profile, in meters.
	 *
	 * @return the total positive elevation of the profile, in meters.
	 */
	public double totalAscent() {
		return totalAscent;
	}

	/**
	 * Gives the total negative elevation of the profile, in meters.
	 *
	 * @return the total negative elevation of the profile, in meters.
	 */
	public double totalDescent() {
		return totalDescent;
	}

	/**
	 * Gives the altitude of the profile at the given position, which is not necessarily between 0 and the length of the profile.
	 *
	 * @param position the given position in meters.
	 * @return the altitude of the profile at the given position.
	 * The first sample is returned when the position is negative, the last when it is greater than the length.
	 */
	public double elevationAt(double position) {
		if (elevationAtFnc == null) {
			elevationAtFnc = Functions.sampled(elevationSamples, length);
		}
		return elevationAtFnc.applyAsDouble(position);
	}
}
