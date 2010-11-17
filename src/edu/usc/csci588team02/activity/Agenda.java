package edu.usc.csci588team02.activity;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.manager.EventManager;
import edu.usc.csci588team02.model.EventEntry;

public class Agenda extends Activity implements Refreshable
{
	private static EventManager eventManager = new EventManager();
	private static final int MENU_LOGOUT = 1;
	private static final int MENU_PREFERENCES = 2;
	private static final int MENU_VIEW_CALENDARS = 0;
	private static final String PREF = "MyPrefs";
	private final ArrayList<EventEntry> eventList = new ArrayList<EventEntry>();
	private final ArrayList<HashMap<String,String>> eventHashMapList = new ArrayList<HashMap<String,String>>();

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
					final String authToken = settings.getString("authToken",
							null);
					eventManager.setAuthToken(authToken);
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
				final Intent detailsIntent = new Intent(Agenda.this,
						EventDetails.class);
				detailsIntent.putExtra("eventUrl", eventList.get(position)
						.getSelfLink());
				startActivity(detailsIntent);
			}
		});
		
		//For complex hashmap layout
		SimpleAdapter adapterForList = new SimpleAdapter(Agenda.this,
				eventHashMapList, R.layout.agenda_item,
                new String[] {"title", "when", "where", "imageUri"},
                new int[] { R.id.agendaItemTitle, R.id.agendaItemWhen, R.id.agendaItemWhere});
		final ListView mainList = (ListView) findViewById(R.id.agendaList);
		mainList.setAdapter(adapterForList);
				
		startActivityForResult(new Intent(this, Login.class),
				Login.REQUEST_AUTHENTICATE);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, MENU_VIEW_CALENDARS, 0, "View Calendars");
		menu.add(0, MENU_LOGOUT, 0, "Logout");
		menu.add(0, MENU_PREFERENCES, 0, "Preferences");
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
			case MENU_PREFERENCES:
				final Intent j = new Intent(this, Preferences.class);
				startActivity(j);
				return true;
		}
		return false;
	}

	/**
	 * Refresh the data for the MainScreen activity
	 */
	@Override
	public void refreshData()
	{
		// Set the last refreshed to a while refreshing text
		final TextView lastRefreshed = (TextView) findViewById(R.id.lastRefreshed);
		lastRefreshed.setText(getText(R.string.whileRefreshing));
		// Load the data
		//final ListView mainList = (ListView) findViewById(R.id.agendaList);
		//String[] calendarEvents;
		eventHashMapList.clear();
		try
		{
			final Calendar twoWeeksFromNow = Calendar.getInstance();
			twoWeeksFromNow.add(Calendar.DATE, 14);
			final Set<EventEntry> events = eventManager
					.getEventsStartingNow(twoWeeksFromNow.getTime());

			//For simple 1string layout
			/*int h = 0;
			calendarEvents = new String[events.size()];*/
			
			//For complex hashmap layout
			int h=0;
			for (final EventEntry event : events)
			{
				//For simple 1string layout
				/*	calendarEvents[h++] = event.title;
				if (event.when != null && event.when.startTime != null)
					calendarEvents[h - 1] = calendarEvents[h - 1] + " on "
							+ event.when.startTime.toString();
				if (event.where != null && event.where.valueString != null
						&& !event.where.valueString.equals(""))
					calendarEvents[h - 1] = calendarEvents[h - 1] + " at "
							+ event.where.valueString; */
				
				//For complex hashmap layout
				HashMap<String, String> calendarEventHashMap = new HashMap<String, String>();	
				calendarEventHashMap.put("title", event.title);
				
				if (event.when != null && event.when.startTime != null)
					calendarEventHashMap.put("when", event.when.startTime.toString());
				else
					calendarEventHashMap.put("when", "");
				
				if (event.where != null && event.where.valueString != null && !event.where.valueString.equals(""))
					calendarEventHashMap.put("where", event.where.valueString);
				else
					calendarEventHashMap.put("where", "No Location");
				
				eventHashMapList.add(calendarEventHashMap);
				//eventHashMapList.set(h, calendarEventHashMap);
				//h++;
			}
			eventList.clear();
			eventList.addAll(events);
		} catch (final IOException e)
		{
			e.printStackTrace();
			//calendarEvents = new String[] { e.getMessage() };
			HashMap<String, String> calendarEventHashMap = new HashMap<String, String>();
			calendarEventHashMap.put("title", e.getMessage());
			calendarEventHashMap.put("when", "");
			calendarEventHashMap.put("where", "");
			eventHashMapList.add(calendarEventHashMap);
		}
		//For simple 1string layout
		/*mainList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.agenda_item, calendarEvents));*/
		//eventHashMapList.set(index, entitiesHashMap);

		// Update the last refreshed text
		final CharSequence lastRefreshedBase = getText(R.string.lastRefreshedBase);
		final Date currentDate = new Date();
		final DateFormat dateFormat = DateFormat
				.getDateInstance(DateFormat.SHORT);
		final DateFormat timeFormat = DateFormat
				.getTimeInstance(DateFormat.SHORT);
		lastRefreshed.setText(lastRefreshedBase + " "
				+ dateFormat.format(currentDate) + " "
				+ timeFormat.format(currentDate));
	}
}