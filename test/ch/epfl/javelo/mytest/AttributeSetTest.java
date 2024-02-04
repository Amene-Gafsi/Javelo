package ch.epfl.javelo.mytest;

import ch.epfl.javelo.data.Attribute;
import ch.epfl.javelo.data.AttributeSet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AttributeSetTest {
	@Test
	void of() {
		// List of cases :
		// 1. Normal
		// 2. All attributes are in the list

		// 1.
		var attributeBits = (1L << Attribute.HIGHWAY_SERVICE.ordinal()) | (1L << Attribute.LCN_YES.ordinal());
		var expected = new AttributeSet(attributeBits);
		assertEquals(expected, AttributeSet.of(Attribute.HIGHWAY_SERVICE, Attribute.LCN_YES));

		// 2.
		attributeBits = ~0L >>> (64 - Attribute.ALL.size());
		expected = new AttributeSet(attributeBits);
//		assertEquals(expected, AttributeSet.of(Attribute.ALL));

	}
	@Test
	void contains() {
		// List of cases :
		// 1. Normal case.
		// 2. Last attribute.
		// 3. If attribute is not contained

		// 1.
		var attribute = Attribute.HIGHWAY_FOOTWAY;
		var attributeSet = new AttributeSet(85); // if 81 it only the attribute mentioned above.
		var expected = attributeSet.bits() == 1L << attribute.ordinal();
		assertEquals(expected, attributeSet.contains(attribute));

		// 2.
		attribute = Attribute.LCN_YES;
		attributeSet = new AttributeSet(1L << attribute.ordinal());
		expected = attributeSet.bits() == 1L << attribute.ordinal();
		assertEquals(expected, attributeSet.contains(attribute));

		// 3.
		attribute = Attribute.BICYCLE_DESIGNATED;
		attributeSet = new AttributeSet(0);
		expected = false;
		assertEquals(expected, attributeSet.contains(attribute));
	}

}