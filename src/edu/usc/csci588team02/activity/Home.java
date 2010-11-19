package edu.usc.csci588team02.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.service.AppServiceConnection;

public class Home extends Activity implements Refreshable
{
	public enum EventActionType {
		EVENT_DETAIL, EVENT_LEFT, EVENT_RIGHT, MAP_LAUNCHER, NAV_LAUNCHER
	}

	private static final int MENU_LOGOUT = 1;
	private static final int MENU_PREFERENCES = 2;
	private static final int MENU_VIEW_CALENDARS = 3;
	private EventEntry currentEvent;
	private TextView eventDescription;
	private TextView eventLocation;
	private TextView eventName;
	private TextView eventWhen;
	private Button infoButton;
	private Button mapButton;
	private Button navButton;
	private final AppServiceConnection service = new AppServiceConnection(this);

	private void launch(final EventActionType action)
	{
		// Gives user a choice between Browser and Maps
		/*
		 * Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
		 * Uri.parse
		 * ("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"
		 * )); startActivity(intent);
		 */
		if (currentEvent != null)
			if (currentEvent.where != null)
				switch (action)
				{
					case MAP_LAUNCHER:
						final Intent map = new Intent(Intent.ACTION_VIEW,
								Uri.parse("geo:0,0?q="
										+ currentEvent.where.valueString
												.replace(' ', '+')));
						startActivity(map);
						break;
					case NAV_LAUNCHER:
						final Intent nav = new Intent(Intent.ACTION_VIEW,
								Uri.parse("google.navigation:q="
										+ currentEvent.where.valueString
												.replace(' ', '+')));
						startActivity(nav);
						break;
					default:
						break;
				}
	}

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		// Setup Listeners for the ActionBar Buttons
		eventName = (TextView) findViewById(R.id.eventName);
		eventLocation = (TextView) findViewById(R.id.eventLocation);
		eventDescription = (TextView) findViewById(R.id.eventDescription);
		eventWhen = (TextView) findViewById(R.id.eventWhen);
		mapButton = (Button) findViewById(R.id.mapButton);
		navButton = (Button) findViewById(R.id.navButton);
		infoButton = (Button) findViewById(R.id.infoButton);
		infoButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				final Intent detailsIntent = new Intent(Home.this,
						EventDetails.class);
				detailsIntent.putExtra("eventUrl", currentEvent.getSelfLink());
				startActivity(detailsIntent);
			}
		});
		mapButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				launch(EventActionType.MAP_LAUNCHER);
			}
		});
		navButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				launch(EventActionType.NAV_LAUNCHER);
			}
		});
		// Need to use getApplicationContext as this activity is used as a Tab
		getApplicationContext()
				.bindService(
						new Intent(this,
								edu.usc.csci588team02.service.AppService.class),
						service, Context.BIND_AUTO_CREATE);
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
	 * Refresh the data for the Home Screen activity
	 */
	@Override
	public void refreshData()
	{
		// Configure Home Screen Text
		try
		{
			currentEvent = service.getNextEventWithLocation();
			if (currentEvent != null && currentEvent.title != null)
				eventName.setText(currentEvent.title);
			else
				eventName.setText("No Events");
			if (currentEvent != null && currentEvent.where != null)
				eventLocation.setText(currentEvent.where.valueString);
			else
				eventLocation.setText("");
			if (currentEvent != null && currentEvent.content != null)
				eventDescription.setText(currentEvent.content);
			else
				eventDescription.setText("");
			if (currentEvent != null && currentEvent.when.startTime != null)
			{
				final CharSequence time = android.text.format.DateFormat
						.format("hh:mma 'on' EEEE, MMM dd",
								currentEvent.when.startTime.value);
				eventWhen.setText(time);
			}
			else
				eventWhen.setText("");
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
}