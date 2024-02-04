package ch.epfl.javelo;


/**
 * This class defines static methods for mathematical calculations.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class Math2 {
	private Math2() {
	}

	/**
	 * Calculates the integer part of (x/y).
	 *
	 * @param x int value.
	 * @param y int value.
	 * @return the integer part of x divided by y.
	 * @throws IllegalArgumentException if x < 0 or y <= 0.
	 */
	public static int ceilDiv(int x, int y) {
		Preconditions.checkArgument(x >= 0 && y > 0);
		return (x + y - 1) / y;
	}

	/**
	 * Calculates the coordinate y of the straight line passing through the points (0, y0) and (1, y1).
	 * This method uses Math.fma.
	 *
	 * @param y0 double value.
	 * @param y1 double value.
	 * @param x  double value.
	 * @return the y coordinate of the straight line.
	 */
	public static double interpolate(double y0, double y1, double x) {
		double slope = y1 - y0;
		return Math.fma(slope, x, y0);

	}


	/**
	 * Gives the closest value of v belonging to the interval [min ; max].
	 *
	 * @param min int value.
	 * @param v   int value.
	 * @param max int value.
	 * @return the new value of v that belongs to the interval [min ; max].
	 * @throws IllegalArgumentException if min > max.
	 */
	public static int clamp(int min, int v, int max) {
		Preconditions.checkArgument(max >= min);
		if (v > max) {
			return max;
		}
		if (v < min) {
			return min;
		}
		return v;         // if v is in the range then we return v
	}

	/**
	 * Gives the closest value of v belonging to the interval [min ; max].
	 *
	 * @param min double value.
	 * @param v   double value.
	 * @param max double value.
	 * @return the new value of v that belongs to the interval [min ; max].
	 * @throws IllegalArgumentException if min > max.
	 */
	public static double clamp(double min, double v, double max) {
		Preconditions.checkArgument(max >= min);
		if (v >= max) {
			return max;
		}
		if (v <= min) {
			return min;
		}
		return v;
	}

	/**
	 * Calculates the inverse hyperbolic sine of x.
	 * This method uses Math.log and Math.sqrt.
	 *
	 * @param x double value.
	 * @return the hyperbolic sine of the inverse of x.
	 */
	public static double asinh(double x) {
		return Math.log(x + Math.sqrt(1 + x * x));
	}

	/**
	 * Calculates the dot product of two vectors u and v.
	 *
	 * @param uX the x-axis coordinate of u.
	 * @param uY the y-axis coordinate of u.
	 * @param vX the x-axis coordinate of v.
	 * @param vY the y-axis coordinate of v.
	 * @return the dot product of u and v.
	 */
	public static double dotProduct(double uX, double uY, double vX, double vY) {
		return uX * vX + uY * vY;
	}

	/**
	 * Calculates the square of the norm of a given vector.
	 *
	 * @param uX the x-axis coordinate of u.
	 * @param uY the y-axis coordinate of u.
	 * @return the square of the norm of u.
	 */
	public static double squaredNorm(double uX, double uY) {
		return dotProduct(uX, uY, uX, uY);
	}

	/**
	 * Calculates the norm of a given vector.
	 * This method uses Math.sqrt.
	 *
	 * @param uX the x-axis coordinate of u.
	 * @param uY the y-axis coordinate of u.
	 * @return the norm of u.
	 */
	public static double norm(double uX, double uY) {
		return Math.sqrt(squaredNorm(uX, uY));
	}

	/**
	 * Calculates the length of the projection of the vector going from A to P on the vector going from A to B.
	 *
	 * @param aX the x-axis coordinate of A.
	 * @param aY the y-axis coordinate of A.
	 * @param bX the x-axis coordinate of B.
	 * @param bY the y-axis coordinate of B.
	 * @param pX the x-axis coordinate of P.
	 * @param pY the y-axis coordinate of P.
	 * @return the length of the projection of AP on AB.
	 */
	public static double projectionLength(double aX, double aY, double bX, double bY, double pX, double pY) {
		double u1 = aX - pX;
		double u2 = aY - pY;
		double v1 = aX - bX;
		double v2 = aY - bY;

		return dotProduct(u1, u2, v1, v2) / norm(v1, v2);
	}


}