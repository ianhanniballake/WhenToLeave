package edu.usc.csci588team02.activity;

import java.io.IOException;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TabHost;

import com.google.api.client.util.DateTime;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.maps.RouteInformation;
import edu.usc.csci588team02.maps.RouteInformation.TravelType;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.service.AppServiceConnection;
import edu.usc.csci588team02.utility.NotificationUtility;

public class TabbedInterface extends TabActivity implements LocationAware
{
	private static final int DIALOG_TRANSPORTATION = 100;
	private static final String PREF = "MyPrefs";
	private static final String TAG = "TabbedInterfaceActivity";
	protected final boolean DEBUG = false;
	private NotificationUtility mNotificationUtility;
	private final AppServiceConnection service = new AppServiceConnection(this);

	/**
	 * This method is called when the Login activity (started in onCreate)
	 * returns, ensuring that authentication is finished before setting up
	 * remaining interface and tabs
	 */
	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case Login.REQUEST_AUTHENTICATE:
				setContentView(R.layout.tabbed_interface);
				final Resources res = getResources(); // Resource object to get
				// Drawables
				final TabHost tabHost = getTabHost(); // The activity TabHost
				TabHost.TabSpec spec; // Reusable TabSpec for each tab
				// Setup Notification Utility Manager
				final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				mNotificationUtility = new NotificationUtility(this, nm);
				// Home tab
				spec = tabHost
						.newTabSpec("event")
						.setIndicator("",
								res.getDrawable(R.drawable.ic_tab_home))
						.setContent(new Intent(this, Home.class));
				tabHost.addTab(spec);
				// Agenda tab
				spec = tabHost
						.newTabSpec("agenda")
						.setIndicator("",
								res.getDrawable(R.drawable.ic_tab_agenda))
						.setContent(new Intent(this, Agenda.class));
				tabHost.addTab(spec);
				// Map tab
				spec = tabHost
						.newTabSpec("map")
						.setIndicator("",
								res.getDrawable(R.drawable.ic_tab_map))
						.setContent(new Intent(this, Map.class));
				tabHost.addTab(spec);
				// Set default starting tab to Event/Home
				tabHost.setCurrentTab(0);
				// Setup Listeners for the ActionBar Buttons
				final ImageButton transportButton = (ImageButton) findViewById(R.id.transportModeButton);
				transportButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View view)
					{
						showDialog(DIALOG_TRANSPORTATION);
					}
				});
				final ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
				refreshButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View view)
					{
						// Refresh the current tab's data
						final String tabTag = getTabHost().getCurrentTabTag();
						final Refreshable tab = (Refreshable) getLocalActivityManager()
								.getActivity(tabTag);
						tab.refreshData();
						// TODO: Refresh the GPS and the Time to Leave
					}
				});
				bindService(new Intent(this,
						edu.usc.csci588team02.service.AppService.class),
						service, Context.BIND_AUTO_CREATE);
				break;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		startActivityForResult(new Intent(this, Login.class),
				Login.REQUEST_AUTHENTICATE);
	}

	@Override
	protected Dialog onCreateDialog(final int id)
	{
		switch (id)
		{
			case DIALOG_TRANSPORTATION:
				final AlertDialog transportDialog;
				AlertDialog.Builder builder;
				final Context mContext = getApplicationContext();
				final LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				final View layout = inflater.inflate(
						R.layout.transportation_dialog,
						(ViewGroup) findViewById(R.id.layout_root));
				builder = new AlertDialog.Builder(TabbedInterface.this);
				builder.setView(layout);
				builder.setTitle("Choose Your Mode of Transport");
				transportDialog = builder.create();
				// Setup Custom Dialog Item Listeners and Settings
				final ImageButton carButton = (ImageButton) layout
						.findViewById(R.id.carButton);
				carButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View view)
					{
						final SharedPreferences settings = getSharedPreferences(
								PREF, 0);
						final SharedPreferences.Editor editor = settings.edit();
						editor.putString("TransportPreference", "DRIVING");
						editor.commit();
						if (DEBUG)
							Log.d(TAG,
									"Committed travel pref: "
											+ settings.getString(
													"TransportPreference",
													"DRIVING"));
						transportDialog.dismiss();
					}
				});
				final ImageButton publicButton = (ImageButton) layout
						.findViewById(R.id.publicButton);
				publicButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View view)
					{
						final SharedPreferences settings = getSharedPreferences(
								PREF, 0);
						final SharedPreferences.Editor editor = settings.edit();
						editor.putString("TransportPreference", "BICYCLING");
						editor.commit();
						if (DEBUG)
							Log.d(TAG,
									"Committed travel pref: "
											+ settings.getString(
													"TransportPreference",
													"BICYCLING"));
						transportDialog.dismiss();
					}
				});
				final ImageButton walkButton = (ImageButton) layout
						.findViewById(R.id.walkButton);
				walkButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View view)
					{
						final SharedPreferences settings = getSharedPreferences(
								PREF, 0);
						final SharedPreferences.Editor editor = settings.edit();
						editor.putString("TransportPreference", "WALKING");
						editor.commit();
						if (DEBUG)
							Log.d(TAG,
									"Committed travel pref: "
											+ settings.getString(
													"TransportPreference",
													"WALKING"));
						transportDialog.dismiss();
					}
				});
				return transportDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unbindService(service);
	}

	@Override
	public void onLocationChanged(final Location location)
	{
		// TODO: call notification if needed with current event
		// TODO: update actionbar time and color
		// Get shared preferences
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		// Don't create notifications if they are disabled
		if (settings.getBoolean("EnableNotifications", true))
		{
			// Get Current Location
			final String curLocation = location.getLatitude() + ","
					+ location.getLongitude();
			EventEntry ee = null;
			try
			{
				ee = service.getNextEventWithLocation();
				if (ee != null)
					// determine duration between current location and next
					// event
					if (ee.where.valueString != null)
					{
						// Convert the shared travel preference to a TravelType
						// enum
						TravelType tt = TravelType.DRIVING;
						final String travelTypePref = settings.getString(
								"TransportPreference", "DRIVING");
						if (travelTypePref.equals("BICYCLING"))
							tt = TravelType.BICYCLING;
						else if (travelTypePref.equals("WALKING"))
							tt = TravelType.WALKING;
						final int dur = RouteInformation.getDuration(
								curLocation, ee.where.valueString, tt);
						Log.d(TAG, "Duration=" + dur);
						final long durationTime = dur * 60 * 1000;
						final DateTime eventStart = ee.when.startTime;
						final long timeToLeave = eventStart.value
								- durationTime;
						final Date date = new Date(timeToLeave);
						final Date curDate = new Date(
								System.currentTimeMillis());
						Log.d(TAG,
								"TimeToLeave: "
										+ DateFormat.format("MM/dd/yy h:mmaa",
												date));
						Log.d(TAG,
								"CurrentTime: "
										+ DateFormat.format("MM/dd/yy h:mmaa",
												curDate));
						Log.d(TAG,
								"AppointmentTime: "
										+ DateFormat.format("MM/dd/yy h:mmaa",
												eventStart.value));
						// Setup notifcation color to send
						// TODO: send color to action bar
						long leaveInMinutes = 0;
						if (date.getTime() - curDate.getTime() > 0)
							leaveInMinutes = date.getTime() - curDate.getTime();
						leaveInMinutes = leaveInMinutes / (1000 * 60);
						int notifyTimeInMin = settings.getInt("NotifyTime",
								3600);
						notifyTimeInMin = notifyTimeInMin / 60;
						mNotificationUtility.createSimpleNotification(ee.title,
								ee, leaveInMinutes, notifyTimeInMin);
					}
					else
						Log.d(TAG, "Address does not exist");
			} catch (final IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}