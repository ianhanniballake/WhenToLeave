package edu.usc.csci588team02.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TabHost;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.service.AppLocationListener;
import edu.usc.csci588team02.utility.NotificationUtility;

public class TabbedInterface extends TabActivity
{
	private static final int DIALOG_TRANSPORTATION = 100;
	protected static final String PREF = "MyPrefs";
	private static final String TAG = "TabbedInterfaceActivity";
	protected final boolean DEBUG = false;
	private AppLocationListener mLocationListener;
	private LocationManager mLocationManager;
	private NotificationUtility mNotificationUtility;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbed_interface);
		final Resources res = getResources(); // Resource object to get
												// Drawables
		final TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		
		// Setup GPS callbacks
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		int interval = settings.getInt("RefreshInterval", 5);
		interval = interval * 1000;
		mLocationListener = new AppLocationListener(this);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0,
				mLocationListener);
		
		// Setup Notification Utility Manager
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationUtility = new NotificationUtility(this, nm);
		
		/*Intent service = new Intent(this,
				edu.usc.csci588team02.service.AppService.class);
		startService(service);*/
		
		// Event tab
		spec = tabHost.newTabSpec("event")
				.setIndicator("", res.getDrawable(R.drawable.ic_tab_home))
				.setContent(new Intent(this, Home.class));
		tabHost.addTab(spec);
		// Agenda tab
		spec = tabHost.newTabSpec("agenda")
				.setIndicator("", res.getDrawable(R.drawable.ic_tab_agenda))
				.setContent(new Intent(this, Agenda.class));
		tabHost.addTab(spec);
		// Map tab
		spec = tabHost.newTabSpec("map")
				.setIndicator("", res.getDrawable(R.drawable.ic_tab_map))
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
	
	public void CalculateTimeToLeaveAndNotify(){
		//TODO: call time to leave calculation
		//TODO: call notification if needed with current event
		
		mNotificationUtility.createSimpleNotification("Location Updated");
		//mNotificationUtility.createSimpleNotification("Location Updated", ee, NotificationUtility.COLOR.GREEN);
	}
}