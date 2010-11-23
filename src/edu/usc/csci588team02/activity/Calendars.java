package edu.usc.csci588team02.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
 */
public final class Calendars extends ListActivity implements Refreshable
{
	private static final int MENU_LOGOUT = 1;
	private List<CalendarEntry> calendars = new ArrayList<CalendarEntry>();
	private final AppServiceConnection service = new AppServiceConnection(this);

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case Logout.REQUEST_LOGOUT:
				finish();
				break;
		}
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
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, MENU_LOGOUT, 0, "Logout");
		return true;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unbindService(service);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_LOGOUT:
				startActivityForResult(new Intent(this, Logout.class),
						Logout.REQUEST_LOGOUT);
				return true;
		}
		return false;
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
			e.printStackTrace();
			calendarNames = new String[] { e.getMessage() };
			calendars.clear();
		}
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, calendarNames));
	}
}
