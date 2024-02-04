package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Q28_4;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * This record class represents the array of all the edges of the graph.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

	private final static int BYTES_PER_EDGE = 10;
	private final static int OFFSET_DEST_NODE_ID = 0;
	private final static int OFFSET_LENGTH = OFFSET_DEST_NODE_ID + 4;
	private final static int OFFSET_ELEVATION = OFFSET_LENGTH + 2;
	private final static int OFFSET_OSM_ATTR_SET = OFFSET_ELEVATION + 2;

	private final static int OFFSET_PROFILE_TYPE = 30;
	private final static int LENGTH_PROFILE_TYPE = 2;
	private final static int OFFSET_FIRST_SAMPLE = 0;
	private final static int LENGTH_FIRST_SAMPLE = 30;

	private final static int PROF_1 = 1;
	private final static int PROF_2 = 2;
	private final static int PROF_3 = 3;

	// length of each value contained in the profile. In the prof 2, there are two values in 16 bits uint and in the
	// prof 3, there are 4 values in 16 bits uint.
	private final static int PROF_2_VAL_LENGTH = 8;
	private final static int PROF_3_VAL_LENGTH = 4;


	/**
	 * Checks if an edge is inverted.
	 *
	 * @param edgeId, the identity of the edge.
	 * @return true if the edge is inverted.
	 */
	public boolean isInverted(int edgeId) {
		return (edgesBuffer.getInt(edgeId * BYTES_PER_EDGE) < 0);
	}

	/**
	 * Gives the identity of the destination node of the given identity edge.
	 *
	 * @param edgeId, the identity of the edge.
	 * @return the identity of the destination node of the given identity edge.
	 */
	public int targetNodeId(int edgeId) {
		int targetNodeId = edgesBuffer.getInt(edgeId * BYTES_PER_EDGE);
		if (!isInverted(edgeId)) {
			return targetNodeId;
		} else {
			return ~targetNodeId;
		}
	}

	/**
	 * Gives the length of the given edge.
	 *
	 * @param edgeId, the identity of the edge.
	 * @return the length of the edge.
	 */
	public double length(int edgeId) {
		short edgeLengthUQ12_4 = edgesBuffer.getShort(edgeId * BYTES_PER_EDGE + OFFSET_LENGTH);
		return Q28_4.asDouble(Short.toUnsignedInt(edgeLengthUQ12_4));
	}

	/**
	 * Gives the elevation gain in meters of the given identity edge.
	 *
	 * @param edgeId, the identity of the edge.
	 * @return the elevation gain of the edge in meters.
	 */
	public double elevationGain(int edgeId) {
		short elevationGainUQ12_4 = edgesBuffer.getShort(edgeId * BYTES_PER_EDGE + OFFSET_ELEVATION);
		return Q28_4.asDouble(Short.toUnsignedInt(elevationGainUQ12_4));
	}


	/**
	 * Checks if an edge has of profile.
	 *
	 * @param edgeId, the identity of the edge.
	 * @return true if the edge has a profile.
	 */
	public boolean hasProfile(int edgeId) {
		int profileType = Bits.extractUnsigned(profileIds.get(edgeId), OFFSET_PROFILE_TYPE, LENGTH_PROFILE_TYPE);
		return !(profileType == 0);
	}

	/**
	 * Gives the array containing the profile samples of the given identity edge.
	 * This array is empty if the edge does not have a profile.
	 *
	 * @param edgeId, the identity of the edge.
	 * @return the array of all the edge's profile samples.
	 */
	public float[] profileSamples(int edgeId) {
		if (!hasProfile(edgeId)) return new float[0];

		int profileType = Bits.extractUnsigned(profileIds.get(edgeId), OFFSET_PROFILE_TYPE, LENGTH_PROFILE_TYPE);
		int firstSampleId = Bits.extractUnsigned(profileIds.get(edgeId), OFFSET_FIRST_SAMPLE, LENGTH_FIRST_SAMPLE);
		int lengthQ28_4 = Short.toUnsignedInt(edgesBuffer.getShort(edgeId * BYTES_PER_EDGE + OFFSET_LENGTH));
		// Equation which gives the total number of samples
		int sampleNbr = 1 + Math2.ceilDiv(lengthQ28_4, Q28_4.ofInt(2));

		float[] profileSamples = new float[sampleNbr];
		profileSamples[0] = uq12_4ToFloat(elevations.get(firstSampleId));
		int crtSample = 1;
		int crtSampleId = firstSampleId + 1; // take out + 1
		while (crtSample < profileSamples.length) {
			float[] decompressed = decompress(profileSamples[crtSample - 1], profileType, crtSampleId);
			int stopN = Math.min(profileSamples.length - crtSample, decompressed.length);
			for (int i = 0; i < stopN; i++) {
				profileSamples[crtSample++] = decompressed[i];
			}
			crtSampleId++;
		}

		if (isInverted(edgeId)) { // invert the array
			for (int i = 0; i < profileSamples.length / 2; i++) {
				float temp = profileSamples[profileSamples.length - 1 - i];
				profileSamples[profileSamples.length - 1 - i] = profileSamples[i];
				profileSamples[i] = temp;
			}
		}

		return profileSamples;
	}

	/**
	 * Gets the value inside the elevations buffer and decompresses it according to the profile type.
	 *
	 * @param profileType the type of the profile for this sample.
	 * @param sampleId    the index of the sample inside the evaluations buffer.
	 * @param previous    value of the first sample in float.
	 * @return the value(s) of the sample in float.
	 */
	private float[] decompress(float previous, int profileType, int sampleId) {
		short all = elevations.get(sampleId);
		float[] res;
		switch (profileType) {
			case PROF_1 -> res = new float[]{uq12_4ToFloat(all)};
			case PROF_2 -> {
				res = new float[2];
				// explicit let x * PROF_2_VAL_LENGTH for easy understanding. Means # * length. 0 * length means the first and 1 * length means the second.
				res[0] = previous + Q28_4.asFloat(Bits.extractSigned(all, 1 * PROF_2_VAL_LENGTH, PROF_2_VAL_LENGTH));
				res[1] = res[0] + Q28_4.asFloat(Bits.extractSigned(all, 0 * PROF_2_VAL_LENGTH, PROF_2_VAL_LENGTH));
			}
			case PROF_3 -> {
				res = new float[4];
				// explicit let x * PROF_3_VAL_LENGTH for easy understanding. Means # * length. 0 * length means the first and 1 * length means the second.
				res[0] = previous + Q28_4.asFloat(Bits.extractSigned(all, 3 * PROF_3_VAL_LENGTH, PROF_3_VAL_LENGTH));
				res[1] = res[0] + Q28_4.asFloat(Bits.extractSigned(all, 2 * PROF_3_VAL_LENGTH, PROF_3_VAL_LENGTH));
				res[2] = res[1] + Q28_4.asFloat(Bits.extractSigned(all, 1 * PROF_3_VAL_LENGTH, PROF_3_VAL_LENGTH));
				res[3] = res[2] + Q28_4.asFloat(Bits.extractSigned(all, 0 * PROF_3_VAL_LENGTH, PROF_3_VAL_LENGTH));
			}
			default -> res = new float[0];
		}
		return res;
	}

	/**
	 * Transforms a short in Q12_4 to a float (unsigned).
	 *
	 * @param uq12_4 the short to transform.
	 * @return the equivalent in float.
	 */
	private float uq12_4ToFloat(short uq12_4) {
		return Q28_4.asFloat(Short.toUnsignedInt(uq12_4));
	}

	/**
	 * Gives the identity of the set of attributes attached to the given identity edge.
	 *
	 * @param edgeId, the identity of the edge.
	 * @return the identity of the attributes linked to the given edgeId.
	 */
	public int attributesIndex(int edgeId) {
		short edgeAttrUQ12_4 = edgesBuffer.getShort(edgeId * BYTES_PER_EDGE + OFFSET_OSM_ATTR_SET);
		return Short.toUnsignedInt(edgeAttrUQ12_4);
	}
}
