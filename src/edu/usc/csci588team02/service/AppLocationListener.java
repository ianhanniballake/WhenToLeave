package edu.usc.csci588team02.service;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import edu.usc.csci588team02.activity.TabbedInterface;

public class AppLocationListener implements LocationListener
{
	private static TabbedInterface mainActivity;

	public AppLocationListener(TabbedInterface activity)
	{
		mainActivity = activity;
	}

	@Override
	public void onLocationChanged(final Location location)
	{
		if (location != null)
		{
			Log.d("LOCATION CHANGED", "Lat:  " + location.getLatitude() + "");
			Log.d("LOCATION CHANGED", "Long: " + location.getLongitude() + "");
			mainActivity.CalculateTimeToLeaveAndNotify(location);
		}
	}

	@Override
	public void onProviderDisabled(final String provider)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(final String provider)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras)
	{
		// TODO Auto-generated method stub
	}
}