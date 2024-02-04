package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GpxGeneratorTest {
	private static final int ORIGIN_N = 1_200_000;
	private static final int ORIGIN_E = 2_600_000;
	private static final double EDGE_LENGTH = 100.25;

	// Sides of triangle used for "sawtooth" edges (shape: /\/\/\…)
	private static final double TOOTH_EW = 1023;
	private static final double TOOTH_NS = 64;
	private static final double TOOTH_LENGTH = 1025;
	private static final double TOOTH_ELEVATION_GAIN = 100d;
	private static final double TOOTH_SLOPE = TOOTH_ELEVATION_GAIN / TOOTH_LENGTH;

//	public static void main(String[] args) throws IOException {
//		var route = new SingleRoute(verticalEdges(10));
//		float[] rightTable = new float[] {2, 3, 4, 15};
//		ElevationProfile e = new ElevationProfile(10, rightTable);
//		GpxGenerator.writeGpx("weshlazone", route, e);

//	}

	private static List<Edge> verticalEdges(int edgesCount) {
		var edges = new ArrayList<Edge>(edgesCount);
		for (int i = 0; i < edgesCount; i += 1) {
			var p1 = new PointCh(ORIGIN_E, ORIGIN_N + i * EDGE_LENGTH);
			var p2 = new PointCh(ORIGIN_E, ORIGIN_N + (i + 1) * EDGE_LENGTH);
			edges.add(new Edge(i, i + 1, p1, p2, EDGE_LENGTH, x -> Double.NaN));
		}
		return Collections.unmodifiableList(edges);

//	@Test
//	void createGpxTest() throws IOException {



//		List<Edge> edges = new ArrayList<>();
//		List<PointCh> expectedPoints = new ArrayList<>();
//
//		edges.add(new Edge(1, 2, new PointCh(2485010, 1075010), new PointCh(2485020, 1075020), 14.14213562373095, Functions.constant(Double.NaN)));
//		edges.add(new Edge(2, 4, new PointCh(2485020, 1075020), new PointCh(2485030, 1075030), 14.14213562373095, Functions.constant(-8)));
//		edges.add(new Edge(4, 6, new PointCh(2485030, 1075030), new PointCh(2485040, 1075040), 14.14213562373095, Functions.constant(0)));
//
//		expectedPoints.add(new PointCh(2485010, 1075010));
//		expectedPoints.add(new PointCh(2485020, 1075020));
//		expectedPoints.add(new PointCh(2485030, 1075030));
//		expectedPoints.add(new PointCh(2485040, 1075040));
//
//		var route = new SingleRoute(edges);
//
//		float[] elevations = {3, 4, 5, 6, 8 , -1, -3 , -4};
//		var length = 3 * 14.14213562373095;
//		var elv = new ElevationProfile(length, elevations);
//		GpxGenerator.writeGpx("TestGPX", route, elv);

	}

}