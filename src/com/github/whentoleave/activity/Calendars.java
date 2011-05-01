package com.github.whentoleave.activity;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
public final class Calendars extends ListActivity implements Handler.Callback
{
	/**
	 * Connection to the persistent, authorized service
	 */
	private final AppServiceConnection service = new AppServiceConnection(
			new Handler(this));

	/**
	 * Handles an error from the AppService
	 * 
	 * @param errorMessage
	 *            message to display
	 */
	private void handleError(final String errorMessage)
	{
		final String[] calendarNames = new String[] { errorMessage };
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, calendarNames));
	}

	/**
	 * Handles a getCalendars event from the AppService
	 * 
	 * @param calendars
	 *            newly returned calendars
	 */
	private void handleGetCalendars(final List<CalendarEntry> calendars)
	{
		final int numCalendars = calendars.size();
		final String[] calendarNames = new String[numCalendars];
		for (int i = 0; i < numCalendars; i++)
			calendarNames[i] = calendars.get(i).title;
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, calendarNames));
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what)
		{
			case AppService.MSG_REFRESH_DATA:
				handleRefreshData();
				return true;
			case AppService.MSG_GET_CALENDARS:
				@SuppressWarnings("unchecked")
				final List<CalendarEntry> calendars = (List<CalendarEntry>) msg.obj;
				handleGetCalendars(calendars);
				return true;
			case AppService.MSG_ERROR:
				final String errorMessage = (String) msg.obj;
				handleError(errorMessage);
				return true;
			default:
				return false;
		}
	}

	/**
	 * Handles a refreshData event from the AppService
	 */
	public void handleRefreshData()
	{
		service.requestCalendars();
	}

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
		service.unregister();
		unbindService(service);
	}
}
