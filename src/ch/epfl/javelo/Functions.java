package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

/**
 * This class defines methods to represent mathematical functions from the real numbers to the real numbers.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class Functions {
	private Functions() {

	}

	/**
	 * Returns a constant function, whose value is always y.
	 *
	 * @param y the constant.
	 * @return a constant function, whose value is always y.
	 */
	public static DoubleUnaryOperator constant(double y) {
		return (double x) -> y;
	}


	/**
	 * Gives a function obtained by linear interpolation between samples, regularly spaced and covering the range from 0 to xMax.
	 *
	 * @param samples list of samples to containing the y values for interpolations.
	 * @param xMax    The upper bound of x values.
	 * @return a function obtained by linear interpolation between samples, regularly spaced
	 * and covering the range from 0 to xMax.
	 * @throws IllegalArgumentException if the samples array contains less than two elements,
	 *                                  or if xMax is less than or equal to 0.
	 */
	public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
		Preconditions.checkArgument(samples.length > 1 && xMax > 0);

		// clone the original array, like this, the DoubleUnaryOperator is immutable.
		float[] samplesCopy = samples.clone();

		return (double x) -> {
			// if outside [0;xMax]
			if (x <= 0) return samplesCopy[0];
			if (x >= xMax) return samplesCopy[samplesCopy.length - 1];

			// as samples are for x = [0;xMax] spaced equally, we can calculate the delta x between each sample by:
			double deltaX = xMax / (samplesCopy.length - 1);

			double mult = x / deltaX;

			// if the multiplication is round, directly on a sample, no need for interpolation.
			if ((mult == Math.floor(mult)) && !Double.isInfinite(mult)) return samplesCopy[(int) mult];
			int segmentNo = (int) mult;

			if (segmentNo == samplesCopy.length - 1) return samplesCopy[samplesCopy.length - 1];
			return Math2.interpolate(samplesCopy[segmentNo], samplesCopy[segmentNo + 1], (x - segmentNo * deltaX) / deltaX);
		};

	}

}







