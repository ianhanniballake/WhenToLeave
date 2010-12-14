package com.github.whentoleave.service;

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
import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpTransport;
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
	public static final int MSG_ERROR = 1;
	public static final int MSG_GET_CALENDARS = 2;
	public static final int MSG_GET_EVENT = 3;
	public static final int MSG_GET_EVENTS = 4;
	public static final int MSG_GET_NEXT_EVENT_WITH_LOCATION = 5;
	public static final int MSG_INVALIDATE_AUTH_TOKEN = 6;
	public static final int MSG_LOCATION_UPDATE = 7;
	public static final int MSG_REFRESH_DATA = 8;
	public static final int MSG_REGISTER_LOCATION_LISTENER = 9;
	public static final int MSG_REGISTER_REFRESHABLE = 10;
	public static final int MSG_SET_AUTH_TOKEN = 11;
	public static final int MSG_UNREGISTER_LOCATION_LISTENER = 12;
	public static final int MSG_UNREGISTER_REFRESHABLE = 13;
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
				locationListenerList.add(msg.replyTo);
				return true;
			case MSG_SET_AUTH_TOKEN:
				final String authToken = (String) msg.obj;
				setAuthToken(authToken);
				return true;
			case MSG_UNREGISTER_LOCATION_LISTENER:
				locationListenerList.remove(msg.replyTo);
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
	protected void invalidateAuthToken()
	{
		((GoogleHeaders) transport.defaultHeaders).remove("Authorization");
		isAuthenticated = false;
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
		HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
		transport = GoogleTransport.create();
		final GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName(R.string.app_name + "-" + R.string.version);
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
		if (location != null)
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

	protected void replyWithCalendars(final Messenger replyToMessenger)
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
	 */
	protected void replyWithEvent(final String eventUrl,
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

	protected void replyWithEvents(final Date start, final Date end,
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

	protected void replyWithNextEventWithLocation(
			final Messenger replyToMessenger)
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
	protected void setAuthToken(final String authToken)
	{
		((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
		isAuthenticated = true;
	}
}
