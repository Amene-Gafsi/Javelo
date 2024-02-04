package ch.epfl.javelo.mytest;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.routing.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElevationProfileComputerTest {
	// List of cases
	// 1. the elevationSamples array contains only Nan values except the last value
	// 2. the elevationSamples array contains only Nan values except the first value
	// 3. the elevationSamples array contains only Nan values.
	// 4. the elevationSamples array contains a Nan value in between valid values and two at the last position.
	// 5. Just to be sure
	// Exception test

	@Test
	void ElevationProfileTest() {
		// 1.
		var route = new RouteImplementation();
//		float[] elevationSamples = new float[]{1f, 1f, 1f};
//		var expected1 = new ElevationProfile(6.0, elevationSamples);
//		var got1 = ElevationProfileComputer.elevationProfile(route, 5.0);
//		assertEquals(expected1.length(), got1.length());
//		assertEquals(expected1.elevationAt(0), got1.elevationAt(0));
//		assertEquals(expected1.elevationAt(2), got1.elevationAt(2));
//		assertEquals(expected1.elevationAt(4), got1.elevationAt(4));

		// 2.
//		float[] elevationSamples2 = new float[]{1f, 1f, 1f};
//		var expected2 = new ElevationProfile(6.0, elevationSamples2);
//		var got2 = ElevationProfileComputer.elevationProfile(route, 5.0);
//		assertEquals(expected2.length(), got2.length());
//		assertEquals(expected2.elevationAt(0), got2.elevationAt(0));
//		assertEquals(expected2.elevationAt(2), got2.elevationAt(2));
//		assertEquals(expected2.elevationAt(4), got2.elevationAt(4));

		// 3.
//		float[] elevationSamples3 = new float[]{0f, 0f, 0f};
//		var expected3 = new ElevationProfile(6.0, elevationSamples3);
//		var got3 = ElevationProfileComputer.elevationProfile(route, 5);
//		assertEquals(expected3.length(), got3.length());
//		assertEquals(expected3.elevationAt(0), got3.elevationAt(0));
//		assertEquals(expected3.elevationAt(2), got3.elevationAt(2));
//		assertEquals(expected3.elevationAt(4), got3.elevationAt(4));


		// 4.
		float[] elevationSamples4 = new float[]{
				1f, (float) Math2.interpolate(1f, 2f, 1f/3f), (float) Math2.interpolate(1f, 2f, 2f/3f), 2f, 2f,
		};
		var expected2 = new ElevationProfile(10.0, elevationSamples4);
		var got2 = ElevationProfileComputer.elevationProfile(route, 2.5);
		assertEquals(expected2.length(), got2.length());
		assertEquals(expected2.elevationAt(0), got2.elevationAt(0));
		assertEquals(expected2.elevationAt(2), got2.elevationAt(2));
		assertEquals(expected2.elevationAt(4), got2.elevationAt(4));
		assertEquals(expected2.elevationAt(6), got2.elevationAt(6));
		assertEquals(expected2.elevationAt(8), got2.elevationAt(8));

		// 5.
//		float[] elevationSamples5 = new float[]{3f, (float) Math2.interpolate(3f, 2f, 1.0/2.0), 2f};
//		var expected5 = new ElevationProfile(6.0, elevationSamples5);
//		var got5 = ElevationProfileComputer.elevationProfile(route, 5.0);
//		assertEquals(expected5.length(), got5.length());
//		assertEquals(expected5.elevationAt(0), got5.elevationAt(0));
//		assertEquals(expected5.elevationAt(2), got5.elevationAt(2));
//		assertEquals(expected5.elevationAt(4), got5.elevationAt(4));

		// Exception test
		assertThrows(IllegalArgumentException.class, () -> {
			ElevationProfileComputer.elevationProfile(route, 0);
		});
	}

}

class RouteImplementation implements Route {

	@Override
	public int indexOfSegmentAt(double position) {
		return 0;
	}

	@Override
	public double length() {
		// 1. l = 6
		// 4. l = 10
		return 10;
	}

	@Override
	public List<Edge> edges() {
		return null;
	}

	@Override
	public List<PointCh> points() {
		return null;
	}

	@Override
	public PointCh pointAt(double position) {
		return null;
	}

	@Override
	public double elevationAt(double position) {
		// 5.
		double elevation = 0.0;
		if (position == 0.0) {
			elevation = 1.0;
		} else if (position == 2.0) {
			elevation = Float.NaN;
		} else if (position == 4.0) {
			elevation = Float.NaN;
		}
		 else if (position == 6.0){
			elevation = 2.0;
		} else {
			elevation = Float.NaN;
		}
		return elevation;
//		1.
//		float[] elevationSamples = new float[]{Float.NaN, Float.NaN, 1f};
//		ElevationProfile elevationProfile = new ElevationProfile(4.0, elevationSamples);
//		return Functions.sampled(elevationSamples, elevationProfile.length()).applyAsDouble(position);
	}


	@Override
	public int nodeClosestTo(double position) {
		return 0;
	}

	@Override
	public RoutePoint pointClosestTo(PointCh point) {
		return null;
	}
}
