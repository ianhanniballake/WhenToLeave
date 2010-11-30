package edu.usc.csci588team02.maps;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

/**
 * Represents a Road or route on a map.<br />
 * <br />
 * Modified from <a href=
 * "http://code.google.com/p/j2memaprouteprovider/source/browse/trunk/J2MEMapRouteAndroidEx/src/org/ci/geo/route/Road.java"
 * >the J2MEMapRoute Example for Android</a> by Max Gontar
 */
public class Road
{
	/**
	 * Description of the Road
	 */
	public String mDescription;
	/**
	 * Name or title of the Road
	 */
	public String mName;
	/**
	 * Lat/Long pairs representing the points in the Road
	 */
	public ArrayList<GeoPoint> mRoute = new ArrayList<GeoPoint>();
}
