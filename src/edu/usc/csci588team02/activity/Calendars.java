package edu.usc.csci588team02.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.model.CalendarFeed;
import edu.usc.csci588team02.model.CalendarUrl;
import edu.usc.csci588team02.model.Namespace;

/**
 * Sample for Google Calendar Data API using the Atom wire format. It shows how
 * to authenticate, get calendars, add a new calendar, update it, and delete it.
 * <p>
 * To enable logging of HTTP requests/responses, run this command: {@code adb
 * shell setprop log.tag.HttpTransport DEBUG}. Then press-and-hold a calendar,
 * and enable "Logging".
 * </p>
 * 
 * @author Yaniv Inbar
 */
public final class Calendars extends ListActivity
{
	private static final int CONTEXT_DELETE = 1;
	private static final int CONTEXT_EDIT = 0;
	private static final int CONTEXT_LOGGING = 2;
	private static final int MENU_ADD = 0;
	private static final int MENU_LOGOUT = 1;
	private static final String PREF = "MyPrefs";
	private static final String TAG = "Calendars";
	private static HttpTransport transport;
	private final List<CalendarEntry> calendars = new ArrayList<CalendarEntry>();

	public Calendars()
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

	private void executeRefreshCalendars()
	{
		String[] calendarNames;
		calendars.clear();
		try
		{
			final CalendarUrl url = CalendarUrl.forAllCalendarsFeed();
			// page through results
			while (true)
			{
				final CalendarFeed feed = CalendarFeed.executeGet(transport,
						url);
				if (feed.calendars != null)
					calendars.addAll(feed.calendars);
				final String nextLink = feed.getNextLink();
				if (nextLink == null)
					break;
			}
			final int numCalendars = calendars.size();
			calendarNames = new String[numCalendars];
			for (int i = 0; i < numCalendars; i++)
				calendarNames[i] = calendars.get(i).title;
		} catch (final IOException e)
		{
			handleException(e);
			calendarNames = new String[] { e.getMessage() };
			calendars.clear();
		}
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, calendarNames));
	}

	private void handleException(final Exception e)
	{
		e.printStackTrace();
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		final boolean log = settings.getBoolean("logging", false);
		if (e instanceof HttpResponseException)
		{
			final HttpResponse response = ((HttpResponseException) e).response;
			final int statusCode = response.statusCode;
			try
			{
				response.ignore();
			} catch (final IOException e1)
			{
				e1.printStackTrace();
			}
			if (statusCode == 401 || statusCode == 403)
			{
				startActivityForResult(new Intent(this, Login.class),
						Login.REQUEST_AUTHENTICATE);
				return;
			}
			if (log)
				try
				{
					Log.e(TAG, response.parseAsString());
				} catch (final IOException parseException)
				{
					parseException.printStackTrace();
				}
		}
		if (log)
			Log.e(TAG, e.getMessage(), e);
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
					executeRefreshCalendars();
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

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final CalendarEntry calendar = calendars.get((int) info.id);
		try
		{
			switch (item.getItemId())
			{
				case CONTEXT_EDIT:
					final CalendarEntry patchedCalendar = calendar.clone();
					patchedCalendar.title = calendar.title + " UPDATED "
							+ new DateTime(new Date());
					patchedCalendar.executePatchRelativeToOriginal(transport,
							calendar);
					executeRefreshCalendars();
					return true;
				case CONTEXT_DELETE:
					calendar.executeDelete(transport);
					executeRefreshCalendars();
					return true;
				default:
					return super.onContextItemSelected(item);
			}
		} catch (final IOException e)
		{
			handleException(e);
		}
		return false;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getListView().setTextFilterEnabled(true);
		registerForContextMenu(getListView());
		startActivityForResult(new Intent(this, Login.class),
				Login.REQUEST_AUTHENTICATE);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_EDIT, 0, "Update Title");
		menu.add(0, CONTEXT_DELETE, 0, "Delete");
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		final boolean logging = settings.getBoolean("logging", false);
		menu.add(0, CONTEXT_LOGGING, 0, "Logging").setCheckable(true)
				.setChecked(logging);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, MENU_ADD, 0, "New calendar");
		menu.add(0, MENU_LOGOUT, 0, "Logout");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_ADD:
				final CalendarUrl url = CalendarUrl.forOwnCalendarsFeed();
				final CalendarEntry calendar = new CalendarEntry();
				calendar.title = "Calendar " + new DateTime(new Date());
				try
				{
					calendar.executeInsert(transport, url);
				} catch (final IOException e)
				{
					handleException(e);
				}
				executeRefreshCalendars();
				return true;
			case MENU_LOGOUT:
				startActivityForResult(new Intent(this, Logout.class),
						Logout.REQUEST_LOGOUT);
				return true;
		}
		return false;
	}
}
