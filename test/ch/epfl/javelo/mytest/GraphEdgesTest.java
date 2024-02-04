package ch.epfl.javelo.mytest;

import ch.epfl.javelo.data.GraphEdges;
import org.junit.jupiter.api.Test;


import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.junit.jupiter.api.Assertions.*;


public class GraphEdgesTest {

	@Test
	void GraphEdgesGeneralTest() {
		
		
		ByteBuffer edgesBuffer = ByteBuffer.allocate(10);
		edgesBuffer.putInt(0, ~12);
		edgesBuffer.putShort(4, (short) 0x10_b);
		edgesBuffer.putShort(6, (short) 0x10_0);
		edgesBuffer.putShort(8, (short) 2022);

		
		IntBuffer profileIds = IntBuffer.wrap(new int[]{
		    (3 << 30) | 1 // type 3 + firstIndex = 1
		  });

		ShortBuffer elevations = ShortBuffer.wrap(new short[]{
		    (short) 0,
		    (short) 0x180C, (short) 0xFEFF,
		    (short) 0xFFFE, (short) 0xF000
		  });

		GraphEdges edges = new GraphEdges(edgesBuffer, profileIds, elevations);
		
		assertTrue(edges.isInverted(0));             //juste
		assertEquals(12, edges.targetNodeId(0));       //juste
		assertEquals(16.6875, edges.length(0));      //juste
		assertEquals(16.0, edges.elevationGain(0));    //juste
		assertEquals(2022, edges.attributesIndex(0));   //juste
		assertTrue(edges.hasProfile(0));                 //juste
		float[] expectedSamples = new float[]{
		  384.0625f, 384.125f, 384.25f, 384.3125f, 384.375f,
		  384.4375f, 384.5f, 384.5625f, 384.6875f, 384.75f
		};
		assertArrayEquals(expectedSamples, edges.profileSamples(0));
		
	}
}
