package ch.epfl.javelo.projection;

/**
 * This class contains constants and a method related to territory limitations of Switzerland.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class SwissBounds {
	public static final double MIN_E = 2485000;
	public static final double MAX_E = 2834000;
	public static final double MIN_N = 1075000;
	public static final double MAX_N = 1296000;
	public static final double WIDTH = MAX_E - MIN_E;
	public static final double HEIGHT = MAX_N - MIN_N;

	private SwissBounds() {

	}

	/**
	 * Tells if the given coordinates are in the range of Switzerland.
	 *
	 * @param e the east coordinate in the CH1903+ system.
	 * @param n the north coordinate in the CH1903+ system.
	 * @return true if the coordinates are in the range.
	 */
	public static boolean containsEN(double e, double n) {
		return (e <= MAX_E && e >= MIN_E && n <= MAX_N && n >= MIN_N);
	}
}
