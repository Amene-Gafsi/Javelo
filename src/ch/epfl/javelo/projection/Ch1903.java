package ch.epfl.javelo.projection;

/**
 * This class provides static methods to convert WGS 84 and Swiss coordinates.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class Ch1903 {
	private Ch1903() {

	}

	/**
	 * Calculates the east coordinate in the CH1903+ system with the longitude and latitude in the WGS84 system.
	 * This method uses Math.pow.
	 *
	 * @param lon the longitude.
	 * @param lat the latitude.
	 * @return the east coordinate in the CH1903+ system.
	 */
	public static double e(double lon, double lat) {
		double lambda = lambdaZero(lon);
		double phi = phiZero(lat);

		double E = 2600072.37 + 211455.93 * lambda - 10938.51 * lambda * phi - 0.36 * lambda * Math.pow(phi, 2) - 44.54 * Math.pow(lambda, 3);

		return E;
	}

	/**
	 * Calculates the north coordinate in the CH1903+ system with the longitude and latitude in the WGS84 system.
	 * This method uses Math.pow.
	 *
	 * @param lon the longitude.
	 * @param lat the latitude.
	 * @return the north coordinate in the CH1903+ system.
	 */
	public static double n(double lon, double lat) {
		double lambda = lambdaZero(lon);
		double phi = phiZero(lat);

		double N = 1200147.07 + 308807.95 * phi + 3745.25 * Math.pow(lambda, 2) + 76.63 * Math.pow(phi, 2) - 194.56 * Math.pow(lambda, 2) * phi + 119.79 * Math.pow(phi, 3);

		return N;
	}

	/**
	 * Calculates the longitude with the CH1903+ system.
	 * This method uses Math.pow and Math.toRadians.
	 *
	 * @param e the east coordinate.
	 * @param n the north coordinate.
	 * @return the longitude.
	 */
	public static double lon(double e, double n) {
		double x = xCord(e);
		double y = yCord(n);

		double lambda = 2.6779094 + 4.728982 * x + 0.791484 * x * y + 0.1306 * x * Math.pow(y, 2) - 0.0436 * Math.pow(x, 3);

		return Math.toRadians(lambda) * 100.0 / 36.0;
	}

	/**
	 * Calculates the latitude with the CH1903+ system.
	 * This method uses Math.pow and Math.toRadians.
	 *
	 * @param e the east coordinate.
	 * @param n the north coordinate.
	 * @return the latitude.
	 */
	public static double lat(double e, double n) {
		double x = xCord(e);
		double y = yCord(n);

		double phi = 16.9023892 + 3.238272 * y - 0.270978 * Math.pow(x, 2) - 0.002528 * Math.pow(y, 2) - 0.0447 * Math.pow(x, 2) * y - 0.0140 * Math.pow(y, 3);

		return Math.toRadians(phi) * 100.0 / 36.0;
	}

	/**
	 * Calculates lambda zero used in the global calculation of the east and north coordinate in the CH1903+ system.
	 * This method is used in method e and method n below.
	 * This method uses Math.pow.
	 *
	 * @param lon the longitude.
	 * @return lambda zero.
	 */
	private static double lambdaZero(double lon) {
		return Math.pow(10, -4) * (3600 * Math.toDegrees(lon) - 26782.5);
	}

	/**
	 * Calculates phi zero used in the global calculation of the east and north coordinate in the CH1903+ system.
	 * This method is used in method e and method n below.
	 * This method uses Math.pow.
	 *
	 * @param lat the latitude.
	 * @return phi zero.
	 */
	private static double phiZero(double lat) {
		return Math.pow(10, -4) * (3600 * Math.toDegrees(lat) - 169028.66);
	}

	/**
	 * Calculates the x coordinate used in the global calculation of the longitude and latitude in the CH1903+ system.
	 * This method is used in method lon and method lat below.
	 * This method uses Math.pow.
	 *
	 * @param e the east coordinate.
	 * @return x coordinate.
	 */
	private static double xCord(double e) {
		return Math.pow(10, -6) * (e - 2600000);
	}

	/**
	 * Calculates the y coordinate used in the global calculation of the longitude and latitude in the CH1903+ system.
	 * This method is used in method lon and method lat below.
	 * This method uses Math.pow.
	 *
	 * @param n the north coordinate.
	 * @return y coordinate.
	 */
	private static double yCord(double n) {
		return Math.pow(10, -6) * (n - 1200000);
	}


}
