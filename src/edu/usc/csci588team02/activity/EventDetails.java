package edu.usc.csci588team02.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.manager.EventManager;
import edu.usc.csci588team02.maps.RouteInformation;
import edu.usc.csci588team02.model.EventEntry;

public class EventDetails extends Activity
{
	private static EventManager eventManager = new EventManager();
	private static final int MENU_LOGOUT = 1;
	private static final String PREF = "MyPrefs";

	private void loadData()
	{
		final Bundle passedInValue = getIntent().getExtras();
		final String eventUrl = passedInValue.getString("eventUrl");
		final TextView eventDetailsName = (TextView) findViewById(R.id.eventDetailsName);
		try
		{
			final EventEntry event = eventManager.getEvent(eventUrl);
			eventDetailsName.setText(event.title);
			final TextView eventDetailsLocation = (TextView) findViewById(R.id.eventDetailsLocation);
			final TextView eventDetailsDescription = (TextView) findViewById(R.id.eventDetailsDescription);
			final Button eventDetailsMapButton = (Button) findViewById(R.id.eventDetailsMapButton);
			final Button eventDetailsNavButton = (Button) findViewById(R.id.eventDetailsNavButton);
			if (event.content != null)
				eventDetailsDescription.setText(event.content);
			if (event.where != null && event.where.valueString != null)
			{
				eventDetailsLocation.setText(event.where.valueString);
				eventDetailsMapButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View v)
					{
						final GeoPoint geoPoint = RouteInformation
								.getLocation(event.where.valueString);
						final String latLng = geoPoint.getLatitudeE6() / 1E6
								+ "," + geoPoint.getLongitudeE6() / 1E6;
						final Intent map = new Intent(
								Intent.ACTION_VIEW,
								Uri.parse("geo:"
										+ latLng
										+ "?z=16&q="
										+ RouteInformation
												.formatAddress(event.where.valueString)));
						startActivity(map);
					}
				});
				eventDetailsMapButton.setVisibility(View.VISIBLE);
				eventDetailsMapButton.setClickable(true);
				eventDetailsNavButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View v)
					{
						final Intent map = new Intent(
								Intent.ACTION_VIEW,
								Uri.parse("google.navigation:q="
										+ RouteInformation
												.formatAddress(event.where.valueString)));
						startActivity(map);
					}
				});
				eventDetailsNavButton.setVisibility(View.VISIBLE);
				eventDetailsNavButton.setClickable(true);
			}
			else
			{
				eventDetailsMapButton.setVisibility(View.GONE);
				eventDetailsMapButton.setClickable(false);
				eventDetailsNavButton.setVisibility(View.GONE);
				eventDetailsNavButton.setClickable(false);
			}
		} catch (final IOException e)
		{
			e.printStackTrace();
			eventDetailsName.setText(e.toString());
		}
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
					final String authToken = settings.getString("authToken",
							null);
					eventManager.setAuthToken(authToken);
					loadData();
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
		setContentView(R.layout.event_details);
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
}
