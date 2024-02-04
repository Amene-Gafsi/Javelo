package ch.epfl.javelo.data;

import ch.epfl.javelo.Preconditions;

import java.util.StringJoiner;

import static ch.epfl.javelo.data.Attribute.ALL;
import static ch.epfl.javelo.data.Attribute.COUNT;

/**
 * This record class represents a set of attributes (stored in a long) of OpenStreetMap.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record AttributeSet(long bits) {
	/**
	 * Compact constructor
	 *
	 * @throws IllegalArgumentException if the value passed to the constructor contains a 1 bit
	 *                                  which does not correspond to any valid attribute.
	 */
	public AttributeSet {
		Preconditions.checkArgument(bits >> Attribute.COUNT == 0);
	}

	/**
	 * Returns a set containing only the attributes given in argument.
	 *
	 * @param attributes the attributes to consider and store in a long. We store the attributes by putting
	 *                   a 1 at the position of the attribute. If we have a 0, the attribute at that
	 *                   position is not contained.
	 * @return a set containing only the attributes given in argument.
	 */
	public static AttributeSet of(Attribute... attributes) {
		long selectedAttributes = 0;
		for (Attribute a : attributes) {
			selectedAttributes |= 1L << a.ordinal(); // we preserve the positions of the attributes throughout the foreach
		}
		return new AttributeSet(selectedAttributes);
	}

	/**
	 * Tells if "this" contains the given attribute.
	 *
	 * @param attribute the attribute.
	 * @return true if and only if "this" contains the given attribute.
	 */
	public boolean contains(Attribute attribute) {
		return ((this.bits & (1L << attribute.ordinal())) != 0);
	}

	/**
	 * Tells if the intersection of "this" with the argument "that" is not empty.
	 *
	 * @param that an AttributeSet.
	 * @return true if and only if the intersection of "this" with the argument "that" is not empty.
	 */
	public boolean intersects(AttributeSet that) {
		return ((this.bits & that.bits) != 0);
	}

	@Override
	public String toString() {
		StringJoiner actualAttributes = new StringJoiner(",", "{", "}");
		for (Attribute at: ALL) {
			if (contains(at)) actualAttributes.add(at.toString());
		}
		return actualAttributes.toString();
	}
}
