package edu.usc.csci588team02.activity;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.model.CalendarFeed;
import edu.usc.csci588team02.model.CalendarUrl;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.model.EventFeed;
import edu.usc.csci588team02.model.Namespace;

public class Agenda extends Activity
{
	private static final int MENU_LOGOUT = 1;
	private static final int MENU_VIEW_CALENDARS = 0;
	private static final String PREF = "MyPrefs";
	private static HttpTransport transport;
	private final DateFormat dateFormat = DateFormat
			.getDateInstance(DateFormat.SHORT);
	private final DateFormat timeFormat = DateFormat
			.getTimeInstance(DateFormat.SHORT);

	public Agenda()
	{
		HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
		transport = GoogleTransport.create();
		final GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName("google-calendarandroidsample-1.0");
		headers.gdataVersion = "2";
		final AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Namespace.DICTIONARY;
		transport.addParser(parser);
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case Login.REQUEST_AUTHENTICATE:
				if (resultCode == RESULT_OK)
				{
					final SharedPreferences settings = getSharedPreferences(
							PREF, 0);
					((GoogleHeaders) transport.defaultHeaders)
							.setGoogleLogin(settings.getString("authToken",
									null));
					refreshData();
				}
				else
				{
					Toast.makeText(this, R.string.loginCanceled,
							Toast.LENGTH_SHORT);
					finish();
				}
				break;
			case Logout.REQUEST_LOGOUT:
				finish();
				break;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agenda);
		final ListView agendaList = (ListView) findViewById(R.id.agendaList);
		agendaList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id)
			{
				// Make a popup (Toast) until we have a details view activity
				Toast.makeText(getApplicationContext(),
						((TextView) view).getText(), Toast.LENGTH_SHORT).show();
			}
		});
		final Button refreshButton = (Button) findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				refreshData();
			}
		});
		startActivityForResult(new Intent(this, Login.class),
				Login.REQUEST_AUTHENTICATE);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, MENU_VIEW_CALENDARS, 0, "View Calendars");
		menu.add(0, MENU_LOGOUT, 0, "Logout");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_VIEW_CALENDARS:
				final Intent i = new Intent(this, Calendars.class);
				startActivity(i);
				return true;
			case MENU_LOGOUT:
				startActivityForResult(new Intent(this, Logout.class),
						Logout.REQUEST_LOGOUT);
				return true;
		}
		return false;
	}

	/**
	 * Refresh the data for the MainScreen activity
	 */
	public void refreshData()
	{
		// TODO Move refresh data out of the UI thread
		// Set the last refreshed to a while refreshing text
		final TextView lastRefreshed = (TextView) findViewById(R.id.lastRefreshed);
		lastRefreshed.setText(getText(R.string.whileRefreshing));
		// Load the data
		// If this takes a while, the UI thread will be frozen
		final ListView mainList = (ListView) findViewById(R.id.agendaList);
		String[] calendarEvents;
		final List<CalendarEntry> calendars = new ArrayList<CalendarEntry>();
		try
		{
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
			final TreeSet<EventEntry> events = new TreeSet<EventEntry>(
					new Comparator<EventEntry>()
					{
						@Override
						public int compare(final EventEntry event1,
								final EventEntry event2)
						{
							final boolean event1NullWhen = event1.when == null
									|| event1.when.startTime == null;
							final boolean event2NullWhen = event2.when == null
									|| event2.when.startTime == null;
							if (event1NullWhen && !event2NullWhen)
								return 1;
							else if (!event1NullWhen && event2NullWhen)
								return -1;
							else if (!event1NullWhen && !event2NullWhen)
							{
								final long timeDiff = event1.when.startTime.value
										- event2.when.startTime.value;
								if (timeDiff != 0)
									return timeDiff < 0 ? -1 : 1;
							}
							// Either both are null or they have the same time.
							// In those cases, compare by title
							final int titleCompare = event1.title
									.compareTo(event2.title);
							if (titleCompare != 0)
								return titleCompare;
							return 0;
						}
					});
			for (final CalendarEntry calendar : calendars)
			{
				final Calendar twoWeeksFromNow = Calendar.getInstance();
				twoWeeksFromNow.add(Calendar.DATE, 14);
				final CalendarUrl eventFeedUrl = new CalendarUrl(
						calendar.getEventFeedLink() + "?start-min="
								+ new DateTime(new Date()) + "&start-max="
								+ new DateTime(twoWeeksFromNow.getTime())
								+ "&orderby=starttime" + "&singleevents=true");
				final EventFeed eventFeed = EventFeed.executeGet(transport,
						eventFeedUrl);
				events.addAll(eventFeed.getEntries());
			}
			int h = 0;
			calendarEvents = new String[events.size()];
			for (final EventEntry event : events)
			{
				calendarEvents[h++] = event.title;
				if (event.when != null && event.when.startTime != null)
					calendarEvents[h - 1] = calendarEvents[h - 1] + " at "
							+ event.when.startTime.toString();
			}
		} catch (final IOException e)
		{
			e.printStackTrace();
			calendarEvents = new String[] { e.getMessage() };
			calendars.clear();
		}
		mainList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.agenda_item, calendarEvents));
		// Update the last refreshed text
		final CharSequence lastRefreshedBase = getText(R.string.lastRefreshedBase);
		final Date currentDate = new Date();
		lastRefreshed.setText(lastRefreshedBase + " "
				+ dateFormat.format(currentDate) + " "
				+ timeFormat.format(currentDate));
	}
}