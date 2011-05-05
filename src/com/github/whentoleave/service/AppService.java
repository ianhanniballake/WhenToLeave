package com.github.whentoleave.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.github.whentoleave.R;
import com.github.whentoleave.model.CalendarEntry;
import com.github.whentoleave.model.CalendarFeed;
import com.github.whentoleave.model.CalendarUrl;
import com.github.whentoleave.model.EventEntry;
import com.github.whentoleave.model.EventEntryComparator;
import com.github.whentoleave.model.EventFeed;
import com.github.whentoleave.model.Namespace;
import com.github.whentoleave.utility.NotificationUtility;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;

/**
 * Application service, managing all Google account access and authentication,
 * as well as notifications
 */
public class AppService extends Service implements LocationListener,
		Handler.Callback
{
	/**
	 * AlarmManager used to create repeated notification checks
	 */
	private static AlarmManager alarmManager;
	/**
	 * Message returned if an error occured while processing
	 */
	public static final int MSG_ERROR = 1;
	/**
	 * Message returned with a list of the users' calendars
	 */
	public static final int MSG_GET_CALENDARS = 2;
	/**
	 * Message returned with a specific requested event
	 */
	public static final int MSG_GET_EVENT = 3;
	/**
	 * Message returned with a time ordered set of events
	 */
	public static final int MSG_GET_EVENTS = 4;
	/**
	 * Message returned with the next event with a location
	 */
	public static final int MSG_GET_NEXT_EVENT_WITH_LOCATION = 5;
	/**
	 * Message to invalidate the Auth Token
	 */
	public static final int MSG_INVALIDATE_AUTH_TOKEN = 6;
	/**
	 * Message returned when the user's location updated
	 */
	public static final int MSG_LOCATION_UPDATE = 7;
	/**
	 * Message to denote that the AppService is ready to receive requests for
	 * data
	 */
	public static final int MSG_REFRESH_DATA = 8;
	/**
	 * Message to register a component interested in location updates
	 */
	public static final int MSG_REGISTER_LOCATION_LISTENER = 9;
	/**
	 * Message to register a component interested in periodic data refresh
	 * notices
	 */
	public static final int MSG_REGISTER_REFRESHABLE = 10;
	/**
	 * Message to set the Auth Token
	 */
	public static final int MSG_SET_AUTH_TOKEN = 11;
	/**
	 * Message to disable/sleep the GPS
	 */
	public static final int MSG_SLEEP_GPS = 12;
	/**
	 * Message to unregister a component no longer interested in location
	 * updates
	 */
	public static final int MSG_UNREGISTER_LOCATION_LISTENER = 13;
	/**
	 * Message to unregister a component no longer interested in periodic data
	 * refresh notices
	 */
	public static final int MSG_UNREGISTER_REFRESHABLE = 14;
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
	private boolean isAuthenticated = false;
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
	 * List of Messengers to notify on alarm timer ticks
	 */
	private final ArrayList<Messenger> refreshOnTimerListenerList = new ArrayList<Messenger>();
	/**
	 * HttpTransport used for Google API queries
	 */
	private HttpTransport transport;

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

	/**
	 * Gets a list of all of the authenticated user's calendars. Assumes that
	 * the service is already authenticated
	 * 
	 * @return the list of all calendars the user has access to
	 * @throws IOException
	 *             on IO error
	 */
	private List<CalendarEntry> getCalendars() throws IOException
	{
		final ArrayList<CalendarEntry> calendars = new ArrayList<CalendarEntry>();
		final CalendarUrl calFeedUrl = CalendarUrl.forAllCalendarsFeed();
		// page through results
		while (true)
		{
			final CalendarFeed feed = CalendarFeed.executeGet(transport,
					calFeedUrl);
			if (feed.calendars != null)
				calendars.addAll(feed.calendars);
			final String nextLink = feed.getNextLink();
			if (nextLink == null)
				break;
		}
		return calendars;
	}

	/**
	 * Gets all events in a given Date range. Assumes that the service is
	 * already authenticated
	 * 
	 * @param start
	 *            start date
	 * @param end
	 *            end date
	 * @return all events from all calendars in the Date range, ordered by start
	 *         time
	 * @throws IOException
	 *             on IO error
	 */
	private Set<EventEntry> getEvents(final Date start, final Date end)
			throws IOException
	{
		final TreeSet<EventEntry> events = new TreeSet<EventEntry>(
				new EventEntryComparator());
		final List<CalendarEntry> calendars = getCalendars();
		for (final CalendarEntry calendar : calendars)
		{
			final GoogleUrl eventFeedUrl = new GoogleUrl(
					calendar.getEventFeedLink() + "?start-min="
							+ new DateTime(start) + "&start-max="
							+ new DateTime(end) + "&orderby=starttime"
							+ "&singleevents=true");
			final EventFeed eventFeed = EventFeed.executeGet(transport,
					eventFeedUrl);
			events.addAll(eventFeed.getEntries());
		}
		return events;
	}

	/**
	 * Finds the next event across all calendars (chronologically) that has a
	 * location. Searches in an exponentially larger date range until it finds
	 * an event (first 1 day, then 2, then 4, etc). Assumes that the service is
	 * already authenticated
	 * 
	 * @return the next event that has a location, null if no events with a
	 *         location are found
	 * @throws IOException
	 *             on IO error
	 */
	private EventEntry getNextEventWithLocation() throws IOException
	{
		Calendar queryFrom = Calendar.getInstance();
		final Calendar queryTo = Calendar.getInstance();
		queryTo.add(Calendar.DATE, 1);
		int daysToAdd = 2;
		final long curTime = System.currentTimeMillis();
		while (daysToAdd < 2048)
		{
			final Set<EventEntry> events = getEvents(queryFrom.getTime(),
					queryTo.getTime());
			for (final EventEntry event : events)
				if (event.where != null && event.where.valueString != null
						&& event.when.startTime.value > curTime)
					return event;
			queryFrom = queryTo;
			queryTo.add(Calendar.DATE, daysToAdd);
			daysToAdd *= 2;
		}
		return null;
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		final Messenger replyToMessenger = msg.replyTo;
		switch (msg.what)
		{
			case MSG_GET_CALENDARS:
				replyWithCalendars(replyToMessenger);
				return true;
			case MSG_GET_EVENT:
				final String eventUrl = (String) msg.obj;
				replyWithEvent(eventUrl, replyToMessenger);
				return true;
			case MSG_GET_EVENTS:
				final Date[] dateRange = (Date[]) msg.obj;
				replyWithEvents(dateRange[0], dateRange[1], replyToMessenger);
				return true;
			case MSG_GET_NEXT_EVENT_WITH_LOCATION:
				replyWithNextEventWithLocation(replyToMessenger);
				return true;
			case MSG_INVALIDATE_AUTH_TOKEN:
				invalidateAuthToken();
				return true;
			case MSG_REFRESH_DATA:
				try
				{
					replyToMessenger.send(Message
							.obtain(null, MSG_REFRESH_DATA));
				} catch (final RemoteException e)
				{
					// Do nothing
				}
				return true;
			case MSG_REGISTER_LOCATION_LISTENER:
				registerLocationListener(msg.replyTo);
				return true;
			case MSG_SET_AUTH_TOKEN:
				final String authToken = (String) msg.obj;
				setAuthToken(authToken);
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
			case MSG_UNREGISTER_REFRESHABLE:
				refreshOnTimerListenerList.remove(msg.replyTo);
				return true;
			default:
				return false;
		}
	}

	/**
	 * Effectively logs the user out, invalidating their authentication token.
	 * Note that all queries done between now and future authentication will
	 * fail
	 */
	private void invalidateAuthToken()
	{
		((GoogleHeaders) transport.defaultHeaders).remove("Authorization");
		isAuthenticated = false;
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
		// Run 'adb shell setprop log.tag.HttpTransport DEBUG'
		// to turn on debugging
		Logger.getLogger("com.google.api.client").setLevel(Level.CONFIG);
		// Per documentation on google-api-java-client, use the appropriate
		// transport for the current version of Android
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO)
			transport = new ApacheHttpTransport();
		else
			transport = new NetHttpTransport();
		GoogleUtils.useMethodOverride(transport);
		final GoogleHeaders headers = new GoogleHeaders();
		headers.setApplicationName(R.string.app_name + "-" + R.string.version);
		headers.gdataVersion = "2";
		transport.defaultHeaders = headers;
		final AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Namespace.DICTIONARY;
		transport.addParser(parser);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Setup Notification Utility Manager
		final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationUtility = new NotificationUtility(this, nm);
		// Set up notification alarm
		final Context context = getBaseContext();
		alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		final Intent alarmIntent = new Intent(context, AppService.class);
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
		{
			checkNotifications();
			final ArrayList<Messenger> listenersToRemove = new ArrayList<Messenger>();
			for (final Messenger refreshable : refreshOnTimerListenerList)
				try
				{
					refreshable.send(Message.obtain(null, MSG_REFRESH_DATA));
				} catch (final RemoteException e)
				{
					// Remove dead listeners
					listenersToRemove.add(refreshable);
				}
			refreshOnTimerListenerList.removeAll(listenersToRemove);
		}
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
	 * Returns a MSG_GET_CALENDARS message with a list of calendars. Assumes
	 * that the service is already authenticated
	 * 
	 * @param replyToMessenger
	 *            messenger to send reply to
	 */
	private void replyWithCalendars(final Messenger replyToMessenger)
	{
		try
		{
			final List<CalendarEntry> calendars = getCalendars();
			replyToMessenger.send(Message.obtain(null, MSG_GET_CALENDARS,
					calendars));
		} catch (final RemoteException e)
		{
			Log.w(TAG, "replyWithCalendars", e);
		} catch (final IOException e)
		{
			Log.e(TAG, "replyWithCalendars", e);
			try
			{
				replyToMessenger.send(Message.obtain(null, MSG_ERROR,
						e.getMessage()));
			} catch (final RemoteException e1)
			{
				Log.w(TAG, "replyWithCalendars in IOException", e);
			}
		}
	}

	/**
	 * Gets a particular EventEntry given its URL. Assumes that the service is
	 * already authenticated
	 * 
	 * @param eventUrl
	 *            the URL of the EventEntry to return
	 * @param replyToMessenger
	 *            messenger to send reply to
	 */
	private void replyWithEvent(final String eventUrl,
			final Messenger replyToMessenger)
	{
		try
		{
			final EventEntry event = EventEntry.executeGet(transport,
					new GoogleUrl(eventUrl));
			replyToMessenger.send(Message.obtain(null, MSG_GET_EVENT, event));
		} catch (final RemoteException e)
		{
			Log.w(TAG, "replyWithEvent", e);
		} catch (final IOException e)
		{
			Log.e(TAG, "replyWithEvent", e);
			try
			{
				replyToMessenger.send(Message.obtain(null, MSG_ERROR,
						e.getMessage()));
			} catch (final RemoteException e1)
			{
				Log.w(TAG, "replyWithEvent in IOException", e);
			}
		}
	}

	/**
	 * Gets all events in the specified time period. Assumes that the service is
	 * already authenticated
	 * 
	 * @param start
	 *            earliest time for events to return
	 * @param end
	 *            latest time for events to return
	 * @param replyToMessenger
	 *            messenger to send reply to
	 */
	private void replyWithEvents(final Date start, final Date end,
			final Messenger replyToMessenger)
	{
		try
		{
			final Set<EventEntry> events = getEvents(start, end);
			replyToMessenger.send(Message.obtain(null, MSG_GET_EVENTS, events));
		} catch (final RemoteException e)
		{
			Log.w(TAG, "replyWithEvents", e);
		} catch (final IOException e)
		{
			Log.e(TAG, "replyWithEvents", e);
			try
			{
				replyToMessenger.send(Message.obtain(null, MSG_ERROR,
						e.getMessage()));
			} catch (final RemoteException e1)
			{
				Log.w(TAG, "replyWithEvents in IOException", e);
			}
		}
	}

	/**
	 * Gets the next event that has a location set. Assumes that the service is
	 * already authenticated
	 * 
	 * @param replyToMessenger
	 *            messenger to send reply to
	 */
	private void replyWithNextEventWithLocation(final Messenger replyToMessenger)
	{
		try
		{
			final EventEntry event = getNextEventWithLocation();
			replyToMessenger.send(Message.obtain(null,
					MSG_GET_NEXT_EVENT_WITH_LOCATION, event));
		} catch (final RemoteException e)
		{
			Log.w(TAG, "replyWithNextEventWithLocation", e);
		} catch (final IOException e)
		{
			Log.e(TAG, "replyWithNextEventWithLocation", e);
			try
			{
				replyToMessenger.send(Message.obtain(null, MSG_ERROR,
						e.getMessage()));
			} catch (final RemoteException e1)
			{
				Log.w(TAG, "replyWithNextEventWithLocation in IOException", e);
			}
		}
	}

	/**
	 * Authorizes the service with the given authToken
	 * 
	 * @param authToken
	 *            authToken used to authenticate any Google API queries
	 */
	private void setAuthToken(final String authToken)
	{
		((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
		isAuthenticated = true;
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
