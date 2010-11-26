package edu.usc.csci588team02.service;

import java.io.IOException;
import java.util.Date;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import edu.usc.csci588team02.activity.LocationAware;
import edu.usc.csci588team02.activity.Refreshable;
import edu.usc.csci588team02.maps.RouteInformation;
import edu.usc.csci588team02.maps.RouteInformation.TravelType;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.utility.NotificationUtility;

public class NotificationService extends Service implements Refreshable,
		LocationAware
{
	private static final String PREF = "MyPrefs";
	private static final String TAG = "NotificationService";
	private Location currentLocation = null;
	private NotificationUtility mNotificationUtility = null;
	private final AppServiceConnection service = new AppServiceConnection(this,
			this);

	@Override
	public IBinder onBind(final Intent intent)
	{
		return null;
	}

	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy");
		unbindService(service);
	}

	@Override
	public void onLocationChanged(final Location location)
	{
		currentLocation = location;
		refreshData();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId)
	{
		if (mNotificationUtility == null)
		{
			// Setup Notification Utility Manager
			final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mNotificationUtility = new NotificationUtility(this, nm);
		}
		if (!service.isConnected())
		{
			Log.d(TAG, "Connecting to AppService");
			bindService(new Intent(this, AppService.class), service,
					Context.BIND_AUTO_CREATE);
			// refreshData once we are connected
		}
		else
			refreshData();
		return START_STICKY;
	}

	@Override
	public void refreshData()
	{
		Log.d(TAG, "Checking for notification");
		// Don't do anything until we are authenticated.
		if (!service.isAuthenticated())
		{
			stopSelf();
			return;
		}
		try
		{
			final EventEntry nextEvent = service.getNextEventWithLocation();
			// No next event = no notification needed
			if (nextEvent == null)
			{
				stopSelf();
				return;
			}
			//
			if (currentLocation == null)
			{
				stopSelf();
				return;
			}
			final SharedPreferences settings = getSharedPreferences(PREF, 0);
			TravelType travelType = TravelType.DRIVING;
			final String travelTypePref = settings.getString(
					"TransportPreference", "DRIVING");
			if (travelTypePref.equals("BICYCLING"))
				travelType = TravelType.BICYCLING;
			else if (travelTypePref.equals("WALKING"))
				travelType = TravelType.WALKING;
			final String locationString = currentLocation.getLatitude() + ","
					+ currentLocation.getLongitude();
			final int minutesToEvent = RouteInformation.getDuration(
					locationString, nextEvent.where.valueString, travelType);
			final long minutesUntilEvent = (nextEvent.when.startTime.value - new Date()
					.getTime()) / 60000;
			final long leaveInMinutes = minutesUntilEvent - minutesToEvent;
			Log.v(TAG, "Leave in " + leaveInMinutes + " minutes");
			final int notifyTimeInMin = settings.getInt("NotifyTime", 3600) / 60;
			// Send the notification
			mNotificationUtility.createSimpleNotification(nextEvent.title,
					nextEvent, leaveInMinutes, notifyTimeInMin);
		} catch (final IOException e)
		{
		} finally
		{
			// Stop ourselves after we're done
			stopSelf();
		}
	}
}
