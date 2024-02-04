package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

import java.util.Arrays;

/**
 * This class represents an itinerary profile calculator.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class ElevationProfileComputer {
	ElevationProfileComputer() {

	}

	/**
	 * Gives the long profile of the route, ensuring that the spacing between
	 * the profile samples is at most "maxStepLength" meters.
	 *
	 * @param route         the route from which the profiles are extracted.
	 * @param maxStepLength the maximum spacing between the samples in meters.
	 * @return the long profile of the route.
	 * @throws IllegalArgumentException if "maxStepLength" is not strictly positive.
	 */
	public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
		Preconditions.checkArgument(maxStepLength > 0);

		int nbSamples = ((int) Math.ceil(route.length() / maxStepLength)) + 1;
		double lengthOfSample = route.length() / (nbSamples - 1);
		float[] elevationSamples = new float[nbSamples];
		for (int i = 0; i < nbSamples; i++) {
			elevationSamples[i] = (float) route.elevationAt(i * lengthOfSample);
		}

		// find a valid sample to fill in the gaps at the beginning of the list and check if the array contains a non Nan value
		boolean containsNonNaN = false; // this means that all the values are Nan
		for (int i = 0; i < nbSamples; i++) {
			if (!Float.isNaN(elevationSamples[i])) {
				Arrays.fill(elevationSamples, 0, i, elevationSamples[i]);
				containsNonNaN = true;
				break;
			}
		}

		// if there are no valid samples then fill the array with zeros
		if (!containsNonNaN) {
			for (int i = 0; i < nbSamples; i++) {
				elevationSamples[i] = 0f;
			}
		}

		// find a valid sample to fill in the gaps at the end of the list
		if (Float.isNaN(elevationSamples[nbSamples - 1])) {
			for (int i = nbSamples - 1; i >= 0; i--) {
				if (!Float.isNaN(elevationSamples[i])) {
					Arrays.fill(elevationSamples, i + 1, nbSamples, elevationSamples[i]);
					break;
				}
			}
		}

		// fill in the gaps in between the list
		for (int i = 0; i < nbSamples; i++) {
			if (Float.isNaN(elevationSamples[i])) {
				int j = i + 1;
				while (Float.isNaN(elevationSamples[j])) {
					j++;
				}
				for (int k = i; k < j; k++) {
					elevationSamples[k] = (float) Math2.interpolate(elevationSamples[i - 1], elevationSamples[j], ((k - i) + 1f) / ((j - i) + 1f));
				}
			}
		}
		return new ElevationProfile(route.length(), elevationSamples);
	}
}
