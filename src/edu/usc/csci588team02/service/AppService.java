package edu.usc.csci588team02.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;

import edu.usc.csci588team02.activity.LocationAware;
import edu.usc.csci588team02.activity.Refreshable;
import edu.usc.csci588team02.maps.RouteInformation.TravelType;
import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.model.CalendarFeed;
import edu.usc.csci588team02.model.CalendarUrl;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.model.EventEntryComparator;
import edu.usc.csci588team02.model.EventFeed;
import edu.usc.csci588team02.model.Namespace;
import edu.usc.csci588team02.utility.NotificationUtility;

/**
 * Application service, managing all Google account access and authentication,
 * as well as notifications
 */
public class AppService extends Service implements LocationListener
{
	/**
	 * Service binding exposed interface, used when Activities or other services
	 * bind to this Service
	 */
	public class AppServiceBinder extends Binder
	{
		/**
		 * Register a new LocationAware listener to get location change
		 * notifications. Note that if a GPS location had ever been found, the
		 * listener's onLocationChanged is called with this latest location as
		 * part of this method
		 * 
		 * @param listener
		 *            LocationAware listener to register
		 */
		public void addLocationListener(final LocationAware listener)
		{
			AppService.this.addLocationListener(listener);
		}

		/**
		 * Register a new Refreshable listener to get alarm timer notifications.
		 * Note that the listener's refreshData is called as part of this method
		 * 
		 * @param listener
		 *            Refreshable listener to register
		 */
		public void addRefreshOnTimerListener(final Refreshable listener)
		{
			AppService.this.addRefreshOnTimerListener(listener);
		}

		/**
		 * Gets a list of all of the authenticated user's calendars. Assumes
		 * that the service is already authenticated
		 * 
		 * @return the list of all calendars the user has access to
		 * @throws IOException
		 *             on IO error
		 */
		public List<CalendarEntry> getCalendars() throws IOException
		{
			return AppService.this.getCalendars();
		}

		/**
		 * Gets a particular EventEntry given its URL. Assumes that the service
		 * is already authenticated
		 * 
		 * @param eventUrl
		 *            the URL of the EventEntry to return
		 * @return the EventEntry represented by the given URL
		 * @throws IOException
		 *             on IO error
		 */
		public EventEntry getEvent(final String eventUrl) throws IOException
		{
			return AppService.this.getEvent(eventUrl);
		}

		/**
		 * Gets all events in a given Date range. Assumes that the service is
		 * already authenticated
		 * 
		 * @param start
		 *            start date
		 * @param end
		 *            end date
		 * @return all events from all calendars in the Date range, ordered by
		 *         start time
		 * @throws IOException
		 *             on IO error
		 */
		public Set<EventEntry> getEvents(final Date start, final Date end)
				throws IOException
		{
			return AppService.this.getEvents(start, end);
		}

		/**
		 * Gets all events between now (new Date()) and the given end Date.
		 * Assumes that the service is already authenticated
		 * 
		 * @param end
		 *            end date
		 * @return all events from all calendars from now until the given end
		 *         date, ordered by start time
		 * @throws IOException
		 *             on IO error
		 */
		public Set<EventEntry> getEventsStartingNow(final Date end)
				throws IOException
		{
			return AppService.this.getEvents(new Date(), end);
		}

		/**
		 * Finds the next event across all calendars (chronologically) that has
		 * a location. Searches in an exponentially larger date range until it
		 * finds an event (first 1 day, then 2, then 4, etc). Assumes that the
		 * service is already authenticated
		 * 
		 * @return the next event that has a location, null if no events with a
		 *         location are found
		 * @throws IOException
		 *             on IO error
		 */
		public EventEntry getNextEventWithLocation() throws IOException
		{
			return AppService.this.getNextEventWithLocation();
		}

		/**
		 * Effectively logs the user out, invalidating their authentication
		 * token. Note that all queries done between now and future
		 * authentication will fail
		 */
		public void invalidateAuthToken()
		{
			AppService.this.invalidateAuthToken();
		}

		/**
		 * Getter for whether the service is authenticated
		 * 
		 * @return if the service is authenticated
		 */
		public boolean isAuthenticated()
		{
			return AppService.this.isAuthenticated();
		}

		/**
		 * Removes a LocationAware listener, stopping any location update
		 * notifications
		 * 
		 * @param listener
		 *            listener to remove
		 */
		public void removeLocationListener(final LocationAware listener)
		{
			AppService.this.removeLocationListener(listener);
		}

		/**
		 * Removes a Refreshable listener, stopping any alarm notifications
		 * 
		 * @param listener
		 *            listener to remove
		 */
		public void removeRefreshOnTimerListener(final Refreshable listener)
		{
			AppService.this.removeRefreshOnTimerListener(listener);
		}

		/**
		 * Authorizes the service with the given authToken
		 * 
		 * @param authToken
		 *            authToken used to authenticate any Google API queries
		 */
		public void setAuthToken(final String authToken)
		{
			AppService.this.setAuthToken(authToken);
		}
	}

	/**
	 * AlarmManager used to create repeated notification checks
	 */
	private static AlarmManager alarmManager;
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
	 * Logging tag
	 */
	private static final String TAG = "AppService";
	/**
	 * Single binder instance to return on service connection requests
	 */
	private final IBinder binder = new AppServiceBinder();
	/**
	 * Current location of the device
	 */
	private Location currentLocation = null;
	/**
	 * Whether the Google HttpTransport is authenticated or not
	 */
	private boolean isAuthenticated = false;
	/**
	 * List of LocationAware listeners to notify of location changes
	 */
	private final ArrayList<LocationAware> locationListenerList = new ArrayList<LocationAware>();
	/**
	 * LocationManager to start and stop receiving location updates from
	 */
	private LocationManager locationManager;
	/**
	 * NotificationUtility used to send out notifications
	 */
	private NotificationUtility mNotificationUtility = null;
	/**
	 * List of Refreshable listeners to notify on alarm timer ticks
	 */
	private final ArrayList<Refreshable> refreshOnTimerListenerList = new ArrayList<Refreshable>();
	/**
	 * HttpTransport used for Google API queries
	 */
	private HttpTransport transport;

	/**
	 * Register a new LocationAware listener to get location change
	 * notifications. Note that if a GPS location had ever been found, the
	 * listener's onLocationChanged is called with this latest location as part
	 * of this method
	 * 
	 * @param listener
	 *            LocationAware listener to register
	 */
	public void addLocationListener(final LocationAware listener)
	{
		locationListenerList.add(listener);
		final Location lastKnownLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation != null)
			listener.onLocationChanged(lastKnownLocation);
	}

	/**
	 * Register a new Refreshable listener to get alarm timer notifications.
	 * Note that the listener's refreshData is called as part of this method
	 * 
	 * @param listener
	 *            Refreshable listener to register
	 */
	public void addRefreshOnTimerListener(final Refreshable listener)
	{
		refreshOnTimerListenerList.add(listener);
		listener.refreshData();
	}

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
		if (!isAuthenticated())
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
			TravelType travelType = TravelType.DRIVING;
			final String travelTypePref = settings.getString(
					"TransportPreference", "DRIVING");
			if (travelTypePref.equals("BICYCLING"))
				travelType = TravelType.BICYCLING;
			else if (travelTypePref.equals("WALKING"))
				travelType = TravelType.WALKING;
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
	 * Gets a list of all of the authenticated user's calendars. Assumes that
	 * the service is already authenticated
	 * 
	 * @return the list of all calendars the user has access to
	 * @throws IOException
	 *             on IO error
	 */
	public List<CalendarEntry> getCalendars() throws IOException
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
	 * Gets a particular EventEntry given its URL. Assumes that the service is
	 * already authenticated
	 * 
	 * @param eventUrl
	 *            the URL of the EventEntry to return
	 * @return the EventEntry represented by the given URL
	 * @throws IOException
	 *             on IO error
	 */
	public EventEntry getEvent(final String eventUrl) throws IOException
	{
		final EventEntry event = EventEntry.executeGet(transport,
				new GoogleUrl(eventUrl));
		return event;
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
	public Set<EventEntry> getEvents(final Date start, final Date end)
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
	 * Gets all events between now (new Date()) and the given end Date. Assumes
	 * that the service is already authenticated
	 * 
	 * @param end
	 *            end date
	 * @return all events from all calendars from now until the given end date,
	 *         ordered by start time
	 * @throws IOException
	 *             on IO error
	 */
	public Set<EventEntry> getEventsStartingNow(final Date end)
			throws IOException
	{
		return getEvents(new Date(), end);
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
	public EventEntry getNextEventWithLocation() throws IOException
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

	/**
	 * Effectively logs the user out, invalidating their authentication token.
	 * Note that all queries done between now and future authentication will
	 * fail
	 */
	public void invalidateAuthToken()
	{
		((GoogleHeaders) transport.defaultHeaders).remove("Authorization");
		isAuthenticated = false;
	}

	/**
	 * Getter for whether the service is authenticated
	 * 
	 * @return if the service is authenticated
	 */
	public boolean isAuthenticated()
	{
		return isAuthenticated;
	}

	/**
	 * Called when an Activity or Service binds to this Service
	 */
	@Override
	public IBinder onBind(final Intent intent)
	{
		Log.d(TAG, "onBind");
		return binder;
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
		HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
		transport = GoogleTransport.create();
		final GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName("google-calendarandroidsample-1.0");
		headers.gdataVersion = "2";
		final AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Namespace.DICTIONARY;
		transport.addParser(parser);
		// Setup GPS callbacks
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		int interval = settings.getInt("RefreshInterval", 5);
		interval = interval * 1000;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				interval, 0, this);
		// Setup Notification Utility Manager
		final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationUtility = new NotificationUtility(this, nm);
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
		if (location != null)
		{
			Log.d(TAG,
					"Service LOCATION CHANGED: + (Lat/Long): ("
							+ location.getLatitude() + ", "
							+ location.getLongitude() + ")");
			currentLocation = location;
			checkNotifications();
			for (final LocationAware listener : locationListenerList)
				listener.onLocationChanged(location);
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
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		if (intent != null && NOTIFICATION_ACTION.equals(intent.getAction()))
		{
			checkNotifications();
			for (final Refreshable refreshable : refreshOnTimerListenerList)
				refreshable.refreshData();
		}
		else if (settings.getBoolean("EnableNotifications", true))
		{
			final Context context = getBaseContext();
			alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			final Intent alarmIntent = new Intent(context, AppService.class);
			alarmIntent.setAction(NOTIFICATION_ACTION);
			pendingIntent = PendingIntent
					.getService(context, 0, alarmIntent, 0);
			// Set up the alarm to trigger every minute
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					System.currentTimeMillis() + 30000, 60000, pendingIntent);
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
	 * Removes a LocationAware listener, stopping any location update
	 * notifications
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeLocationListener(final LocationAware listener)
	{
		locationListenerList.remove(listener);
	}

	/**
	 * Removes a Refreshable listener, stopping any alarm notifications
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeRefreshOnTimerListener(final Refreshable listener)
	{
		refreshOnTimerListenerList.remove(listener);
	}

	/**
	 * Authorizes the service with the given authToken
	 * 
	 * @param authToken
	 *            authToken used to authenticate any Google API queries
	 */
	public void setAuthToken(final String authToken)
	{
		((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
		isAuthenticated = true;
	}
}
