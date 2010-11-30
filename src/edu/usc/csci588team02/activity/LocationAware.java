package edu.usc.csci588team02.activity;

import android.location.Location;

/**
 * Interface denoting that the implementing type can be registered for location
 * changes
 */
public interface LocationAware
{
	/**
	 * Does any processing required to ensure that the information displayed
	 * accurately reflects this new location
	 * 
	 * @param location
	 *            the new location
	 */
	public void onLocationChanged(final Location location);
}
