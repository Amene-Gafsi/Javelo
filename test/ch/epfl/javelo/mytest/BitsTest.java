package ch.epfl.javelo.mytest;

import ch.epfl.javelo.Bits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitsTest {
	@Test
	void extractUnSigned() {
		int expected = 8;
		int value = 0b10000000000000000000000000000000;
		var actualBits = Bits.extractUnsigned(0b10000000000000000000000000000000,28,4);
		assertEquals(expected, actualBits);
	}
}