package ch.epfl.javelo;

/**
 * This class defines a method for checking function preconditions.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class Preconditions {
	private Preconditions() {
	}

	/**
	 * Checks if the boolean argument is true, otherwise an exception is thrown.
	 *
	 * @param shouldBeTrue boolean expression to evaluate.
	 * @throws IllegalArgumentException if the parameter shouldBeTrue is false.
	 */
	public static void checkArgument(boolean shouldBeTrue) throws IllegalArgumentException {
		if (!shouldBeTrue) throw new IllegalArgumentException();
	}
}
