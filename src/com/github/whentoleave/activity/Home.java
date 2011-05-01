package com.github.whentoleave.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.github.whentoleave.R;
import com.github.whentoleave.model.EventEntry;
import com.github.whentoleave.service.AppService;
import com.github.whentoleave.service.AppServiceConnection;

/**
 * Activity which shows the next event with a location, along with quick glance
 * information and buttons to get more details, get a map of the event, and
 * navigate to the event. Works optimally as a tab for TabbedInterface.
 * 
 * @see TabbedInterface
 */
public class Home extends Activity implements Handler.Callback
{
	/**
	 * The current event
	 */
	private EventEntry currentEvent;
	/**
	 * TextView for the event's description
	 */
	private TextView eventDescription;
	/**
	 * TextView for the event's location
	 */
	private TextView eventLocation;
	/**
	 * TextView for the event's title
	 */
	private TextView eventName;
	/**
	 * TextView for the event's start time
	 */
	private TextView eventWhen;
	/**
	 * Connection to the persistent, authorized service
	 */
	private final AppServiceConnection service = new AppServiceConnection(
			new Handler(this));

	/**
	 * Handles an error from the AppService
	 * 
	 * @param errorMessage
	 *            error message to display
	 */
	private void handleError(final String errorMessage)
	{
		eventName.setText("Error Getting Next Event");
		eventLocation.setText("");
		eventDescription.setText(errorMessage);
		eventWhen.setText("");
	}

	/**
	 * Handles a getNextEventWithLocation event from the AppService. Note that
	 * the event returned is saved in the currentEvent field.
	 */
	private void handleGetNextEventWithLocation()
	{
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
			final CharSequence time = android.text.format.DateFormat.format(
					"hh:mma 'on' EEEE, MMM dd",
					currentEvent.when.startTime.value);
			eventWhen.setText(time);
		}
		else
			eventWhen.setText("");
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what)
		{
			case AppService.MSG_ERROR:
				final String errorMessage = (String) msg.obj;
				handleError(errorMessage);
				return true;
			case AppService.MSG_GET_NEXT_EVENT_WITH_LOCATION:
				currentEvent = (EventEntry) msg.obj;
				handleGetNextEventWithLocation();
				return true;
			case AppService.MSG_REFRESH_DATA:
				handleRefreshData();
				return true;
			default:
				return false;
		}
	}

	/**
	 * Handles a refreshData event from the AppService
	 */
	private void handleRefreshData()
	{
		service.requestNextEventWithLocation();
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
		final Button mapButton = (Button) findViewById(R.id.mapButton);
		final Button navButton = (Button) findViewById(R.id.navButton);
		final Button infoButton = (Button) findViewById(R.id.infoButton);
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
		service.unregister();
		getApplicationContext().unbindService(service);
	}
}