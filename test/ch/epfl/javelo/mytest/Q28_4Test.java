package ch.epfl.javelo.mytest;

import ch.epfl.javelo.Q28_4;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Q28_4Test {
	@Test
	void OfInt() {
		// List of cases :
		// 1. Edge case : negative number, must preserve the sign.
		// 2. Out of bound (2^28-1).
		// 3. Out of bound (-2^28).
		// 4. Normal case : positive number.

		// 1.
		var expected = (-128 << 4) | (1 << 31);
		Assertions.assertEquals(expected, Q28_4.ofInt(-128));

		// 2.
		assertThrows(IllegalArgumentException.class, () -> {
			Q28_4.ofInt(1 << 28);
		});

		// 3.
		assertThrows(IllegalArgumentException.class, () -> {
			Q28_4.ofInt((-(1 << 28)) - 1);
		});

		// 4.
		expected = (1 << 27) << 4;
		assertEquals(expected, Q28_4.ofInt(1 << 27));

	}

	@Test
	void AsDouble() {
		// List of cases :
		// 1. Normal case.
		// 2. Normal case.
		// 3. Negative number.

		// 1.
		var expected = 0.5;
		assertEquals(expected, Q28_4.asDouble(0b1000));

		//2.
		expected = 1.5;
		assertEquals(expected, Q28_4.asDouble(0b11000));

		// 3.
		expected = -1.5;
		int q28_4_negative2sComplement = -1;
		q28_4_negative2sComplement = q28_4_negative2sComplement<<4;
		q28_4_negative2sComplement |= 1<<31;
		q28_4_negative2sComplement |= 1<<3; // add -0.5 in 2's complement
		assertEquals(expected, Q28_4.asDouble(q28_4_negative2sComplement));

	}
}


