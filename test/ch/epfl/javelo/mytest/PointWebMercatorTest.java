package ch.epfl.javelo.mytest;

import ch.epfl.javelo.projection.PointWebMercator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointWebMercatorTest {
	@Test
	void ofTest() {
		var zoomLevel = (int) 2 ;
		var x = (double) 45;
		var y = (double) 15 ;
		var xAfterZoom = Math.scalb(x, -(zoomLevel + 8));
		var yAfterZoom = Math.scalb(y, -(zoomLevel + 8));
		var got = PointWebMercator.of(zoomLevel, x, y);
		var expected = new PointWebMercator(xAfterZoom, yAfterZoom);
		assertEquals( expected , got );

	}

	@Test
	void xAtZoomLevelTest() {
		var x = (double) 0 ;
		var zoomLevel = 2;
		var expected = x * Math.pow(2, zoomLevel + 8);
		var function = new PointWebMercator(0 , 0.1);
		var got = function.xAtZoomLevel(0);
		assertEquals(expected ,got);

	}

	@Test
	void yAtZoomLevelTest() {
		var y = (double) 0.5 ;
		var zoomLevel = 0;
		var expected = y * Math.pow(2, zoomLevel + 8);
		var function = new PointWebMercator(0.5 , y);
		var got = function.yAtZoomLevel(zoomLevel);
		assertEquals(expected ,got);

	}

}