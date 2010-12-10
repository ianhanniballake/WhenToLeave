package com.github.whentoleave.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.github.whentoleave.model.CalendarEntry;
import com.github.whentoleave.service.AppService;
import com.github.whentoleave.service.AppServiceConnection;

/**
 * Sample for Google Calendar Data API using the Atom wire format to retrieve a
 * list of the user's calendars<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-android-sample/src/com/google/api/client/sample/calendar/android/CalendarAndroidSample.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public final class Calendars extends ListActivity implements Refreshable
{
	/**
	 * Logging tag
	 */
	private static final String TAG = "Calendars";
	/**
	 * List of the calendars the user has access to
	 */
	private List<CalendarEntry> calendars = new ArrayList<CalendarEntry>();
	/**
	 * Connection to the persistent, authorized service
	 */
	private final AppServiceConnection service = new AppServiceConnection(this);

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getListView().setTextFilterEnabled(true);
		bindService(new Intent(this, AppService.class), service,
				Context.BIND_AUTO_CREATE);
		startActivityForResult(new Intent(this, Login.class),
				Login.REQUEST_AUTHENTICATE);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unbindService(service);
	}

	@Override
	public void refreshData()
	{
		String[] calendarNames;
		calendars.clear();
		try
		{
			calendars = service.getCalendars();
			final int numCalendars = calendars.size();
			calendarNames = new String[numCalendars];
			for (int i = 0; i < numCalendars; i++)
				calendarNames[i] = calendars.get(i).title;
		} catch (final IOException e)
		{
			Log.e(TAG, "Error while refreshing data", e);
			calendarNames = new String[] { e.getMessage() };
			calendars.clear();
		}
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, calendarNames));
	}
}
