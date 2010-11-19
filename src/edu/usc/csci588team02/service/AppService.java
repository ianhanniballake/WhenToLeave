package edu.usc.csci588team02.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;

import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.model.CalendarFeed;
import edu.usc.csci588team02.model.CalendarUrl;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.model.EventEntryComparator;
import edu.usc.csci588team02.model.EventFeed;
import edu.usc.csci588team02.model.EventUrl;
import edu.usc.csci588team02.model.Namespace;

/**
 * @author Kevin Kirkpatrick
 */
public class AppService extends Service
{
	public class AppServiceBinder extends Binder
	{
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

		public void setAuthToken(final String authToken)
		{
			AppService.this.setAuthToken(authToken);
		}
	}

	private static final long INTERVAL = 30000; // 30 seconds = 30000
	private static final String TAG = "AppService";
	private final IBinder binder = new AppServiceBinder();
	private boolean isAuthenticated = false;
	private final Timer timer = new Timer();
	private HttpTransport transport;

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
			final CalendarUrl eventFeedUrl = new CalendarUrl(
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
		while (daysToAdd < 2048)
		{
			final Set<EventEntry> events = getEvents(queryFrom.getTime(),
					queryTo.getTime());
			for (final EventEntry event : events)
				if (event.where != null && event.where.valueString != null)
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

	@Override
	public IBinder onBind(final Intent intent)
	{
		return binder;
	}

	@Override
	public void onCreate()
	{
		// Toast.makeText(this, "App Servcie Created",
		// Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
		transport = GoogleTransport.create();
		final GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName("google-calendarandroidsample-1.0");
		headers.gdataVersion = "2";
		final AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Namespace.DICTIONARY;
		transport.addParser(parser);
		startUpService();
	}

	@Override
	public void onDestroy()
	{
		// Toast.makeText(this, "App Service Stops", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		timer.cancel();
	}

	@Override
	public void onStart(final Intent intent, final int startid)
	{
		// Toast.makeText(this, "App Service Started",
		// Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
	}

	public void setAuthToken(final String authToken)
	{
		((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
		isAuthenticated = true;
	}

	private void startUpService()
	{
		timer.scheduleAtFixedRate(new TimerTask()
		{
			// this activity will run every defined interval.
			@Override
			public void run()
			{
				Log.d(TAG, "TimerKick");
			}
		}, 0, INTERVAL);
	}
}
