package ch.epfl.javelo.mytest;


import ch.epfl.javelo.routing.ElevationProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ElevationProfileTest {

	@Test
	void elevationsprofileTest() {
    float[] elevations = {3, 4, 5, 6, 8 , -1, -3 , -4};
    var length = (double) 5;
    var elvTest = new ElevationProfile(length, elevations);
    var expected1 = 5;
    var expected2 = 12;
    var got =  elvTest.totalAscent();
	assertEquals(expected1, got);
	assertEquals(expected2, elvTest.totalDescent());
	assertEquals(5, elvTest.length());
	assertEquals(8, elvTest.maxElevation());
	assertEquals(-4, elvTest.minElevation());
	assertEquals(-4, elvTest.elevationAt(5));
	assertThrows(IllegalArgumentException.class, () -> {
		new ElevationProfile(-1, elevations);
		});
	assertThrows(IllegalArgumentException.class, () -> {
		new ElevationProfile(5, new float[0]);
		});
}
}
