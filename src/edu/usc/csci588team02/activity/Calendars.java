package edu.usc.csci588team02.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.service.AppService;
import edu.usc.csci588team02.service.AppServiceConnection;

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
 * @author Ian Lake
 */
public final class Calendars extends ListActivity implements Refreshable
{
	private static final String TAG = "Calendars";
	private List<CalendarEntry> calendars = new ArrayList<CalendarEntry>();
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
