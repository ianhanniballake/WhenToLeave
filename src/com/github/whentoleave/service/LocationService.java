package com.github.whentoleave.service;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.github.whentoleave.model.EventEntry;
import com.github.whentoleave.utility.NotificationUtility;

/**
 * Application service, managing all Google account access and authentication,
 * as well as notifications
 */
public class LocationService extends Service implements LocationListener,
		Handler.Callback
{
	/**
	 * AlarmManager used to create repeated notification checks
	 */
	private static AlarmManager alarmManager;
	/**
	 * Message returned when the user's location updated
	 */
	public static final int MSG_LOCATION_UPDATE = 1;
	/**
	 * Message to register a component interested in location updates
	 */
	public static final int MSG_REGISTER_LOCATION_LISTENER = 2;
	/**
	 * Message to disable/sleep the GPS
	 */
	public static final int MSG_SLEEP_GPS = 3;
	/**
	 * Message to unregister a component no longer interested in location
	 * updates
	 */
	public static final int MSG_UNREGISTER_LOCATION_LISTENER = 4;
	/**
	 * Action used to distinguish notification alarm service starts from regular
	 * service starts
	 */
	private static final String NOTIFICATION_ACTION = "WHENTOLEAVE_NOTIFICATION_ACTION";
	/**
	 * PendingIntent triggered by the alarm manager
	 */
	private static PendingIntent pendingIntent;
	/**
	 * Preferences name to load settings from
	 */
	private static final String PREF = "MyPrefs";
	/**
	 * A 'significant' time period between location updates. Currently two
	 * minutes in milliseconds
	 */
	private static final int SIGNIFICANT_TIME_PERIOD = 1000 * 60 * 2;
	/**
	 * Logging tag
	 */
	private static final String TAG = "AppService";
	/**
	 * Current location of the device
	 */
	private Location currentLocation = null;
	/**
	 * Whether the Google HttpTransport is authenticated or not
	 */
	private final boolean isAuthenticated = false;
	/**
	 * List of Messengers to notify of location changes
	 */
	private final ArrayList<Messenger> locationListenerList = new ArrayList<Messenger>();
	/**
	 * LocationManager to start and stop receiving location updates from
	 */
	private LocationManager locationManager;
	/**
	 * Messenger associated with this service
	 */
	private final Messenger messenger = new Messenger(new Handler(this));
	/**
	 * NotificationUtility used to send out notifications
	 */
	private NotificationUtility mNotificationUtility = null;

	/**
	 * Check for notifications, sending them out if required
	 */
	private void checkNotifications()
	{
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		if (!settings.getBoolean("EnableNotifications", true))
			return;
		Log.d(TAG, "Checking for notification");
		// Don't do anything until we are authenticated.
		if (!isAuthenticated)
			return;
		try
		{
			final EventEntry nextEvent = getNextEventWithLocation();
			// No next event = no notification needed
			if (nextEvent == null)
				return;
			// No current location = no when to leave
			if (currentLocation == null)
				return;
			final String travelType = settings.getString("TransportPreference",
					"driving");
			final long leaveInMinutes = nextEvent.getWhenToLeaveInMinutes(
					currentLocation, travelType);
			Log.v(TAG, "Leave in " + leaveInMinutes + " minutes");
			final int notifyTimeInMin = settings.getInt("NotifyTime", 3600) / 60;
			Log.v(TAG, "Notification Pref:" + notifyTimeInMin);
			if (leaveInMinutes <= notifyTimeInMin)
				mNotificationUtility.createSimpleNotification(nextEvent.title,
						nextEvent, leaveInMinutes, notifyTimeInMin);
		} catch (final IOException e)
		{
			Log.e(TAG, "Error checking for notifications", e);
		}
	}

	/**
	 * Enables only Network Provider location updates
	 */
	private void enableNetworkProviderLocationListening()
	{
		Log.d(TAG, "enableNetworkProviderLocationListening");
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		int interval = settings.getInt("RefreshInterval", 600);
		interval = interval * 1000;
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, interval, 0, this);
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what)
		{
			case MSG_REGISTER_LOCATION_LISTENER:
				registerLocationListener(msg.replyTo);
				return true;
			case MSG_SLEEP_GPS:
				Log.d(TAG, "Stopped listening for GPS Updates");
				locationManager.removeUpdates(this);
				// Keep Network Provider updates running if there are location
				// listeners remaining
				if (!locationListenerList.isEmpty()
						&& locationProviderEnabled(LocationManager.NETWORK_PROVIDER))
					enableNetworkProviderLocationListening();
				return true;
			case MSG_UNREGISTER_LOCATION_LISTENER:
				unregisterLocationListener(msg.replyTo);
				return true;
			default:
				return false;
		}
	}

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix. Modified from <a href=
	 * "http://developer.android.com/guide/topics/location/obtaining-user-location.html"
	 * >the Android Dev Guide on Obtaining User Location</a>
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 * @return whether the new location is better than the current best location
	 */
	private boolean isBetterLocation(final Location location,
			final Location currentBestLocation)
	{
		// A null location is never better
		if (location == null)
			return false;
		// A new location is always better than no location
		if (currentBestLocation == null)
			return true;
		// Check whether the new location fix is newer or older
		final long timeDelta = location.getTime()
				- currentBestLocation.getTime();
		final boolean isSignificantlyNewer = timeDelta > SIGNIFICANT_TIME_PERIOD;
		final boolean isSignificantlyOlder = timeDelta < -SIGNIFICANT_TIME_PERIOD;
		final boolean isNewer = timeDelta > 0;
		// If it's been more than two minutes since the current location, use
		// the new location because the user has likely moved
		if (isSignificantlyNewer)
			return true;
		// If the new location is more than two minutes older, it must be worse
		else if (isSignificantlyOlder)
			return false;
		// Check whether the new location fix is more or less accurate
		final int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		final boolean isLessAccurate = accuracyDelta > 0;
		final boolean isMoreAccurate = accuracyDelta < 0;
		final boolean isSignificantlyLessAccurate = accuracyDelta > 200;
		// Check if the old and new location are from the same provider
		final boolean isFromSameProvider = isSameProvider(
				location.getProvider(), currentBestLocation.getProvider());
		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate)
			return true;
		else if (isNewer && !isLessAccurate)
			return true;
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
			return true;
		return false;
	}

	/**
	 * Null safe check for whether two providers are the same
	 * 
	 * @param provider1
	 *            first provider
	 * @param provider2
	 *            second provider
	 * @return if the two providers are the same or both null
	 */
	private boolean isSameProvider(final String provider1,
			final String provider2)
	{
		if (provider1 == null)
			return provider2 == null;
		return provider1.equals(provider2);
	}

	/**
	 * Ensures that a given location provider exists and is enabled
	 * 
	 * @param provider
	 *            location provider to check
	 * @return if the location provider exists and is enabled
	 */
	private boolean locationProviderEnabled(final String provider)
	{
		return locationManager.getProvider(provider) != null
				&& locationManager.isProviderEnabled(provider);
	}

	/**
	 * Called when an Activity or Service binds to this Service
	 */
	@Override
	public IBinder onBind(final Intent intent)
	{
		Log.d(TAG, "onBind");
		return messenger.getBinder();
	}

	/**
	 * Called when this service is first started. Sets up Google API queries as
	 * well as registering this service for GPS location updates. Note that the
	 * service is NOT authenticated until setAuthToken is called.
	 */
	@Override
	public void onCreate()
	{
		Log.d(TAG, "onCreate");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Setup Notification Utility Manager
		final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationUtility = new NotificationUtility(this, nm);
		// Set up notification alarm
		final Context context = getBaseContext();
		alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		final Intent alarmIntent = new Intent(context, LocationService.class);
		alarmIntent.setAction(NOTIFICATION_ACTION);
		pendingIntent = PendingIntent.getService(context, 0, alarmIntent, 0);
		// Set up the alarm to trigger every minute
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				System.currentTimeMillis() + 30000, 60000, pendingIntent);
	}

	/**
	 * Called when this service is ended. Cleans up location updates and alarms
	 */
	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy");
		locationManager.removeUpdates(this);
		if (alarmManager != null)
			alarmManager.cancel(pendingIntent);
	}

	@Override
	public void onLocationChanged(final Location location)
	{
		if (isBetterLocation(location, currentLocation))
		{
			Log.d(TAG,
					"LOCATION CHANGED: + (Lat/Long): ("
							+ location.getLatitude() + ", "
							+ location.getLongitude() + ")");
			currentLocation = location;
			checkNotifications();
			final ArrayList<Messenger> listenersToRemove = new ArrayList<Messenger>();
			for (final Messenger listener : locationListenerList)
				try
				{
					listener.send(Message.obtain(null, MSG_LOCATION_UPDATE,
							location));
				} catch (final RemoteException e)
				{
					// Remove dead listeners from the list
					listenersToRemove.add(listener);
				}
			locationListenerList.removeAll(listenersToRemove);
		}
	}

	@Override
	public void onProviderDisabled(final String provider)
	{
		// Nothing to do
	}

	@Override
	public void onProviderEnabled(final String provider)
	{
		// Nothing to do
	}

	/**
	 * Called when this service is started. Handles both initial set up and,
	 * when passed a NOTIFICATION_ACTION Intent, notification checking as well
	 * as updating any Refreshable listeners. Note that the service is NOT
	 * authenticated until setAuthToken is called.
	 */
	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId)
	{
		Log.d(TAG, "onStart");
		if (intent != null && NOTIFICATION_ACTION.equals(intent.getAction()))
			checkNotifications();
		return START_STICKY;
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras)
	{
		// Nothing to do
	}

	/**
	 * Registers a new component as wanting to receive location updates. Kicks
	 * off an initial GPS Provider location request to ensure a good initial
	 * location.
	 * 
	 * @param replyTo
	 *            component to register
	 */
	private void registerLocationListener(final Messenger replyTo)
	{
		locationListenerList.add(replyTo);
		final int locationListenerSize = locationListenerList.size();
		Log.d(TAG, "Registering new Location Listener: " + locationListenerSize
				+ " now listening");
		// If this is the very first location listener, make sure we enable
		// network provider listening as well
		if (locationListenerSize == 1
				&& locationProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			enableNetworkProviderLocationListening();
			// Get an initial location
			final Location lastNetworkLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			onLocationChanged(lastNetworkLocation);
		}
		// Setup GPS callbacks for the next minute to ensure we have the best
		// location possible
		if (locationProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);
			// Get an initial GPS location as well
			final Location lastGPSLocation = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			onLocationChanged(lastGPSLocation);
			new Handler(this).sendEmptyMessageDelayed(MSG_SLEEP_GPS, 60000);
		}
	}

	/**
	 * Unregisters a component from receiving location updates
	 * 
	 * @param replyTo
	 *            messenger to unregister
	 */
	private void unregisterLocationListener(final Messenger replyTo)
	{
		locationListenerList.remove(replyTo);
		Log.d(TAG,
				"Unregistering Location Listener: "
						+ locationListenerList.size() + " remaining.");
		// Stop getting location if no one is listening for it
		if (locationListenerList.isEmpty())
			locationManager.removeUpdates(this);
	}
}
