package edu.usc.csci588team02.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
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
import android.text.format.DateFormat;
import android.util.Log;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;

import edu.usc.csci588team02.activity.LocationAware;
import edu.usc.csci588team02.maps.RouteInformation;
import edu.usc.csci588team02.maps.RouteInformation.TravelType;
import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.model.CalendarFeed;
import edu.usc.csci588team02.model.CalendarUrl;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.model.EventEntryComparator;
import edu.usc.csci588team02.model.EventFeed;
import edu.usc.csci588team02.model.EventUrl;
import edu.usc.csci588team02.model.Namespace;
import edu.usc.csci588team02.utility.NotificationUtility;

/**
 * @author Kevin Kirkpatrick
 */
public class AppService extends Service implements LocationListener
{
	public class AppServiceBinder extends Binder
	{
		public void addLocationListener(final LocationAware listener)
		{
			AppService.this.addLocationListener(listener);
		}

		public List<CalendarEntry> getCalendars() throws IOException
		{
			return AppService.this.getCalendars();
		}

		public EventEntry getEvent(final String eventUrl) throws IOException
		{
			return AppService.this.getEvent(eventUrl);
		}

		public Set<EventEntry> getEvents(final Date start, final Date end)
				throws IOException
		{
			return AppService.this.getEvents(start, end);
		}

		public Set<EventEntry> getEventsStartingNow(final Date end)
				throws IOException
		{
			return AppService.this.getEvents(new Date(), end);
		}

		public EventEntry getNextEventWithLocation() throws IOException
		{
			return AppService.this.getNextEventWithLocation();
		}

		public boolean isAuthenticated()
		{
			return AppService.this.isAuthenticated();
		}

		public void removeLocationListener(final LocationAware listener)
		{
			AppService.this.removeLocationListener(listener);
		}

		public void setAuthToken(final String authToken)
		{
			AppService.this.setAuthToken(authToken);
		}

		public long getLeaveInMinutes()
		{
			return AppService.this.getLeaveInMinutes();
		}

		public int getNotifyTimeInMinutes()
		{
			return AppService.this.getNotifyTimeInMinutes();
		}
	}

	private static final String PREF = "MyPrefs";
	private static final String TAG = "AppService";
	private final IBinder binder = new AppServiceBinder();
	private boolean isAuthenticated = false;
	private final ArrayList<LocationAware> locationListenerList = new ArrayList<LocationAware>();
	private LocationManager locationManager;
	private final Timer timer = new Timer();
	private HttpTransport transport;
	private NotificationUtility mNotificationUtility;
	private long leaveInMinutes = 0;
	private int notifyTimeInMin = 0;

	// private final AppServiceConnection service = new AppServiceConnection();
	public void addLocationListener(final LocationAware listener)
	{
		locationListenerList.add(listener);
		final Location lastKnownLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation != null)
			listener.onLocationChanged(lastKnownLocation);
	}

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

	public EventEntry getEvent(final String eventUrl) throws IOException
	{
		final EventEntry event = EventEntry.executeGet(transport, new EventUrl(
				eventUrl));
		return event;
	}

	public Set<EventEntry> getEvents(final Date start, final Date end)
			throws IOException
	{
		final TreeSet<EventEntry> events = new TreeSet<EventEntry>(
				new EventEntryComparator());
		final List<CalendarEntry> calendars = getCalendars();
		for (final CalendarEntry calendar : calendars)
		{
			final CalendarUrl eventFeedUrl = new CalendarUrl(calendar
					.getEventFeedLink()
					+ "?start-min="
					+ new DateTime(start)
					+ "&start-max="
					+ new DateTime(end)
					+ "&orderby=starttime"
					+ "&singleevents=true");
			final EventFeed eventFeed = EventFeed.executeGet(transport,
					eventFeedUrl);
			events.addAll(eventFeed.getEntries());
		}
		return events;
	}

	public Set<EventEntry> getEventsStartingNow(final Date end)
			throws IOException
	{
		return getEvents(new Date(), end);
	}

	/**
	 * Finds the next event (chronologically) that has a location. Searches in
	 * an exponentially larger date range until it finds an event (first 1 day,
	 * then 2, then 4, etc)
	 * 
	 * @return the next event that has a location, null if no events with a
	 *         location are found
	 * @throws IOException
	 *             on error
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

	public boolean isAuthenticated()
	{
		return isAuthenticated;
	}

	public long getLeaveInMinutes()
	{
		return leaveInMinutes;
	}

	public int getNotifyTimeInMinutes()
	{
		return notifyTimeInMin;
	}

	@Override
	public IBinder onBind(final Intent intent)
	{
		Log.d(TAG, "onBind");
		return binder;
	}

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
		// Setup Notification Utility Manager
		final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationUtility = new NotificationUtility(this, nm);
		startUpService();
	}

	@Override
	public void onDestroy()
	{
		// Toast.makeText(this, "App Service Stops", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		locationManager.removeUpdates(this);
		timer.cancel();
	}

	@Override
	public void onLocationChanged(final Location location)
	{
		if (location != null)
		{
			Log.d("Service LOCATION CHANGED", "Lat:  " + location.getLatitude()
					+ "");
			Log.d("Service LOCATION CHANGED", "Long: "
					+ location.getLongitude() + "");
			for (final LocationAware listener : locationListenerList)
				listener.onLocationChanged(location);
			// Setup Alarm Test Code
			Intent intent = new Intent(this, AppAlarmReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					getBaseContext(), AlarmManager.RTC_WAKEUP, intent, 0);
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, System
					.currentTimeMillis()
					+ (20 * 1000), pendingIntent);
			Log.d(TAG, "Alarm set in 20 sec");
			// TODO: call notification if needed with current event
			// TODO: update actionbar time and color
			// Get shared preferences
			final SharedPreferences settings = getSharedPreferences(PREF, 0);
			// Don't create notifications if they are disabled
			if (settings.getBoolean("EnableNotifications", true))
			{
				// Get Current Location
				final String curLocation = location.getLatitude() + ","
						+ location.getLongitude();
				EventEntry ee = null;
				try
				{
					ee = getNextEventWithLocation();
					if (ee != null)
						// determine duration between current location and next
						// event
						if (ee.where.valueString != null)
						{
							// Convert the shared travel preference to a
							// TravelType
							// enum
							TravelType tt = TravelType.DRIVING;
							final String travelTypePref = settings.getString(
									"TransportPreference", "DRIVING");
							if (travelTypePref.equals("BICYCLING"))
								tt = TravelType.BICYCLING;
							else if (travelTypePref.equals("WALKING"))
								tt = TravelType.WALKING;
							final int dur = RouteInformation.getDuration(
									curLocation, ee.where.valueString, tt);
							Log.d(TAG, "Duration=" + dur);
							final long durationTime = dur * 60 * 1000;
							final DateTime eventStart = ee.when.startTime;
							final long timeToLeave = eventStart.value
									- durationTime;
							final Date date = new Date(timeToLeave);
							final Date curDate = new Date(System
									.currentTimeMillis());
							Log.d(TAG, "TimeToLeave: "
									+ DateFormat
											.format("MM/dd/yy h:mmaa", date));
							Log.d(TAG, "CurrentTime: "
									+ DateFormat.format("MM/dd/yy h:mmaa",
											curDate));
							Log.d(TAG, "AppointmentTime: "
									+ DateFormat.format("MM/dd/yy h:mmaa",
											eventStart.value));
							// Setup notifcation color to send
							// TODO: send color to action bar
							if (date.getTime() - curDate.getTime() > 0)
								leaveInMinutes = date.getTime()
										- curDate.getTime();
							leaveInMinutes = leaveInMinutes / (1000 * 60);
							int notifyTimeInMin = settings.getInt("NotifyTime",
									3600);
							notifyTimeInMin = notifyTimeInMin / 60;
							mNotificationUtility.createSimpleNotification(
									ee.title, ee, leaveInMinutes,
									notifyTimeInMin);
							int tnotifyTimeInMin = settings.getInt(
									"NotifyTime", 3600);
							notifyTimeInMin = tnotifyTimeInMin / 60;
						}
						else
							Log.d(TAG, "Address does not exist");
				} catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId)
	{
		Log.d(TAG, "onStart");
		return START_STICKY;
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras)
	{
		// Nothing to do
	}

	public void removeLocationListener(final LocationAware listener)
	{
		locationListenerList.remove(listener);
	}

	public void setAuthToken(final String authToken)
	{
		((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
		isAuthenticated = true;
	}

	private void startUpService()
	{
		// Setup GPS callbacks
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		int interval = settings.getInt("RefreshInterval", 5);
		interval = interval * 1000;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				interval, 0, this);
	}
}
