package edu.usc.csci588team02.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TabHost;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.maps.RouteInformation.TravelType;
import edu.usc.csci588team02.service.AppService;
import edu.usc.csci588team02.service.AppServiceConnection;
import edu.usc.csci588team02.service.NotificationService;

public class TabbedInterface extends TabActivity implements LocationAware
{
	public class ActionBar
	{
		private final Button actionBarButton;
		private final ImageButton refreshButton;
		private final ImageButton transportButton;

		public ActionBar()
		{
			final SharedPreferences settings = getSharedPreferences(PREF, 0);
			TravelType tt = TravelType.DRIVING;
			final String travelTypePref = settings.getString(
					"TransportPreference", "DRIVING");
			if (travelTypePref.equals("BICYCLING"))
				tt = TravelType.BICYCLING;
			else if (travelTypePref.equals("WALKING"))
				tt = TravelType.WALKING;
			// Setup Listeners for the ActionBar Buttons
			actionBarButton = (Button) findViewById(R.id.actionBar);
			transportButton = (ImageButton) findViewById(R.id.transportModeButton);
			transportButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View view)
				{
					showDialog(DIALOG_TRANSPORTATION);
				}
			});
			setTransportMode(tt);
			refreshButton = (ImageButton) findViewById(R.id.refreshButton);
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

		public void setColor(final COLOR c)
		{
			final Resources res = getResources();
			switch (c)
			{
				case GREEN:
					actionBarButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_green));
					transportButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_green));
					refreshButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_green));
					break;
				case ORANGE:
					actionBarButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_orange));
					transportButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_orange));
					refreshButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_orange));
					break;
				case RED:
					actionBarButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_red));
					transportButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_red));
					refreshButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_red));
					break;
			}
		}

		public void setText(final String text)
		{
			actionBarButton.setText(text);
		}

		public void setTextAndColor(final long leaveInMinutes,
				final int notifyTimeInMin)
		{
			COLOR actionBarColor = COLOR.GREEN;
			if (leaveInMinutes < notifyTimeInMin * .33333)
				actionBarColor = COLOR.RED;
			else if (leaveInMinutes < notifyTimeInMin * .6666)
				actionBarColor = COLOR.ORANGE;
			setColor(actionBarColor);
			setText("Leave "
					+ (leaveInMinutes > 0 ? "in " + leaveInMinutes + "m"
							: "Now"));
		}

		public void setTransportMode(final TravelType tt)
		{
			final Resources res = getResources();
			switch (tt)
			{
				case DRIVING:
					transportButton.setImageDrawable(res
							.getDrawable(R.drawable.car_white55));
					break;
				case BICYCLING:
					transportButton.setImageDrawable(res
							.getDrawable(R.drawable.bicycle_white55));
					break;
				case WALKING:
					transportButton.setImageDrawable(res
							.getDrawable(R.drawable.person_white55));
					break;
			}
		}
	}

	public enum COLOR {
		GREEN, ORANGE, RED
	}

	private static final int DIALOG_TRANSPORTATION = 100;
	private static final String PREF = "MyPrefs";
	private static final String TAG = "TabbedInterfaceActivity";
	public ActionBar actionBar;
	protected final boolean DEBUG = false;
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
				// tabHost.setup();
				TabHost.TabSpec spec; // Reusable TabSpec for each tab
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
				actionBar = new ActionBar();
				bindService(new Intent(this, AppService.class), service,
						Context.BIND_AUTO_CREATE);
				break;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		// If notifications are enabled, keep the service running after the
		// program exits
		if (settings.getBoolean("EnableNotifications", true))
		{
			startService(new Intent(this, AppService.class));
			// Set up the Notification alarm
			final Intent intent = new Intent(this, NotificationService.class);
			final PendingIntent pendingIntent = PendingIntent.getService(
					getBaseContext(), AlarmManager.RTC_WAKEUP, intent, 0);
			final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis() + 60 * 1000, 60000,
					pendingIntent);
		}
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
						actionBar.setTransportMode(TravelType.DRIVING);
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
						actionBar.setTransportMode(TravelType.BICYCLING);
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
						actionBar.setTransportMode(TravelType.WALKING);
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
		// TODO: Update actionBar
	}
}