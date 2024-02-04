package ch.epfl.javelo.mytest;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Math2;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class FunctionsTest {

	/**
	 * The test is based on the good implementation of {@link Math2#interpolate(double, double, double)}
	 */
	@Test
	void sampledTest() {
		// x0 = 0 ; x1 = 1 ; x2 = 2 ; xMax = 3
		var samples = new float[]{1.0f, 0.0f, 2.0f, 3.0f};
		var xMax = 3;
		var function = Functions.sampled(samples, xMax);


		// List of cases:
		// 1. Normal use, first segment
		// 2. Normal use, middle segment
		// 3. Normal use, last segment
		// 4. Edge case: x < 0
		// 5. Edge case: x = 0
		// 6. Edge case: x = xMax
		// 7. Edge case: x > xMax

		// 1.
		var expected = Math2.interpolate(1.0f, 0.0f, 0.5);
		var got = function.applyAsDouble(0.5);
		assertEquals(expected, got);

		// 2.
		expected = Math2.interpolate(0.0f, 2.0f, 0.5);
		got = function.applyAsDouble(1.5);
		assertEquals(expected, got);

		// 3.
		expected = Math2.interpolate(2.0f, 3.0f, 0.5);
		got = function.applyAsDouble(2.5);
		assertEquals(expected, got);

		// 4.
		expected = samples[0];
		got = function.applyAsDouble(-2.5);
		assertEquals(expected, got);

		// 5.
		expected = samples[0];
		got = function.applyAsDouble(0);
		assertEquals(expected, got);

		// 6.
		expected = samples[samples.length-1];
		got = function.applyAsDouble(xMax);
		assertEquals(expected, got);

		// 7.
		expected = samples[samples.length-1];
		got = function.applyAsDouble(xMax + 50);
		assertEquals(expected, got);

	}

	@Test
	void constantTest() {
		// Test with all sort of random number. Must always give the constant.
		int constant = 42;
		var rnd = new Random();
		var function = Functions.constant(constant);
		for (int i = 0; i < 10000; i++) {
			int val = rnd.nextInt();
			assertEquals(constant, function.applyAsDouble(val));
		}
	}


}