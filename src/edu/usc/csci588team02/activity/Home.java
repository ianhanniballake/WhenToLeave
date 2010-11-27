package edu.usc.csci588team02.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.service.AppService;
import edu.usc.csci588team02.service.AppServiceConnection;

public class Home extends Activity implements Refreshable
{
	public enum EventActionType {
		EVENT_DETAIL, EVENT_LEFT, EVENT_RIGHT, MAP_LAUNCHER, NAV_LAUNCHER
	}

	private static final String TAG = "Home";
	private EventEntry currentEvent;
	private TextView eventDescription;
	private TextView eventLocation;
	private TextView eventName;
	private TextView eventWhen;
	private Button infoButton;
	private Button mapButton;
	private Button navButton;
	private final AppServiceConnection service = new AppServiceConnection(this);

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
				if (currentEvent != null)
				{
					final Intent detailsIntent = new Intent(Home.this,
							EventDetails.class);
					detailsIntent.putExtra("eventUrl",
							currentEvent.getSelfLink());
					startActivity(detailsIntent);
				}
			}
		});
		mapButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				final Intent map = new Intent(Intent.ACTION_VIEW, Uri
						.parse("geo:0,0?q="
								+ currentEvent.where.valueString.replace(' ',
										'+')));
				startActivity(map);
			}
		});
		navButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				final Intent nav = new Intent(Intent.ACTION_VIEW, Uri
						.parse("google.navigation:q="
								+ currentEvent.where.valueString.replace(' ',
										'+')));
				startActivity(nav);
			}
		});
		// Need to use getApplicationContext as this activity is used as a Tab
		getApplicationContext().bindService(new Intent(this, AppService.class),
				service, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		getApplicationContext().unbindService(service);
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
			if (currentEvent != null)
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
			Log.e(TAG, "Error while refreshing data", e);
		}
	}
}