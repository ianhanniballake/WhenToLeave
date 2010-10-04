package edu.usc.csci588team02.activity;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
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
import edu.usc.csci588team02.R;

public class Agenda extends Activity
{
	private static final int MENU_VIEW_CALENDARS = 0;
	private final DateFormat dateFormat = DateFormat
			.getDateInstance(DateFormat.SHORT);
	private final DateFormat timeFormat = DateFormat
			.getTimeInstance(DateFormat.SHORT);

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
		refreshData();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, MENU_VIEW_CALENDARS, 0, "View Calendars");
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
		}
		return false;
	}

	/**
	 * Refresh the data for the MainScreen activity
	 */
	public void refreshData()
	{
		// TODO Move MainScreen refresh data out of the UI thread
		// Set the last refreshed to a while refreshing text
		final TextView lastRefreshed = (TextView) findViewById(R.id.lastRefreshed);
		lastRefreshed.setText(getText(R.string.whileRefreshing));
		// Load the data
		// If this takes a while, the UI thread will be frozen
		final ListView mainList = (ListView) findViewById(R.id.agendaList);
		// TODO Load real data into the MainScreen
		final String calendarEvents[] = new String[50];
		for (int h = 0; h < 50; h++)
			calendarEvents[h] = "Event " + (h + 1);
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