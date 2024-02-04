package ch.epfl.javelo;

/**
 * This class defines static methods for convert numbers between the Q28.4 representation and other representations.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class Q28_4 {

	private final static int INTEGER_PART_LENGTH = 28;
	private final static int DOUBLE_PART_LENGTH = 4;

	private Q28_4() {}

	/**
	 * Converts an integer into a Q28.4 representation.
	 * The Q28.4 representation allows us to represent rational numbers by keeping the 4 minimum
	 * weighted bits for decimal representation. Basically, this method shifts the input by 4 to the left.
	 *
	 * @param i the int that we want to convert in Q28.4.
	 * @return the Q28.4 representation of the integer given.
	 * @throws IllegalArgumentException if i < -2^28 or i > 2^28-1.
	 */
	public static int ofInt(int i) {
		if (i < 0) i |= 1 << (INTEGER_PART_LENGTH-1);  // we put the internal representation of the most significant bit (msb) to 1

		return i << DOUBLE_PART_LENGTH;
	}

	/**
	 * Converts the Q28.4 integer into a double.
	 *
	 * @param q28_4 the integer in Q28.4 representation to convert into a double.
	 * @return the value Q28.4 in double.
	 */
	public static double asDouble(int q28_4) {
		return Math.scalb((double) q28_4, -DOUBLE_PART_LENGTH);
	}

	/**
	 * Converts the Q28.4 integer into a float.
	 * Based on the asDouble implementation.
	 *
	 * @param q28_4 the integer in Q28.4 representation to convert into a float.
	 * @return the value Q28.4 in float.
	 */
	public static float asFloat(int q28_4) {
		// Cast to float mandatory.
		return (float) asDouble(q28_4);
	}
}
