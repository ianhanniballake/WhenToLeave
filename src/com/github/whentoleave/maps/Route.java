package com.github.whentoleave.maps;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

/**
 * Represents a Route on a map.<br />
 * <br />
 * Modified from <a href=
 * "http://code.google.com/p/j2memaprouteprovider/source/browse/trunk/J2MEMapRouteAndroidEx/src/org/ci/geo/route/Road.java"
 * >the J2MEMapRoute Example for Android</a> by Max Gontar
 */
public class Route
{
	/**
	 * Description of the Route
	 */
	public String mDescription;
	/**
	 * Name or title of the Route
	 */
	public String mName;
	/**
	 * Lat/Long pairs representing the points in the Route
	 */
	public ArrayList<GeoPoint> mRoute = new ArrayList<GeoPoint>();
}
