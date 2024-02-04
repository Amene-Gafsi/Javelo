package ch.epfl.javelo;

/**
 * This class defines static methods for extracting a sequence of bits from an integer.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class Bits {
	private Bits() {

	}

	/**
	 * Extracts the "length" bit range starting at the "start" index bit.
	 * The interpretation of the integer is signed.
	 *
	 * @param value  the integer from whom we want to extract the bit range.
	 * @param start  the starting point of the extraction.
	 * @param length the length of the extraction (length = how many bits we want to extract).
	 * @return the extraction of the bit range.
	 * @throws IllegalArgumentException if the range that we want to extract is out of bound for an integer.
	 */
	public static int extractSigned(int value, int start, int length) {
		Preconditions.checkArgument(length >= 0 && length <= Integer.SIZE && start >= 0 && start < Integer.SIZE && (start + length) <= Integer.SIZE);

		int leftShit = Integer.SIZE - start - length;
		int rightShift = Integer.SIZE - length;
		int leftShifted = value << leftShit;
		return leftShifted >> rightShift;
	}

	/**
	 * Extracts the "length" bit range starting at the "start" index bit.
	 * The interpretation of the integer is unsigned.
	 *
	 * @param value  the integer from whom we want to extract the bit range.
	 * @param start  the starting point of the extraction.
	 * @param length the length of the extraction (length = how many bits we want to extract).
	 * @return the extraction of the bit range.
	 * @throws IllegalArgumentException if the range that we want to extract is out of bound for an integer.
	 */
	public static int extractUnsigned(int value, int start, int length) {
		Preconditions.checkArgument(length >= 0 && length < Integer.SIZE && start >= 0 && start < Integer.SIZE && (start + length) <= Integer.SIZE);

		int leftShit = Integer.SIZE - start - length;
		int rightShift = Integer.SIZE - length;
		int leftShifted = value << leftShit;
		return leftShifted >>> rightShift;
	}
}
