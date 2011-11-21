package com.github.whentoleave.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TabHost;

import com.github.whentoleave.R;
import com.github.whentoleave.model.EventEntry;
import com.github.whentoleave.service.AppService;
import com.github.whentoleave.service.AppServiceConnection;

/**
 * Activity which serves as the main hub of the application, containing the
 * Home, Agenda, and Map Activities as tabs and a persistent Action Bar
 */
public class TabbedInterface extends TabActivity implements Handler.Callback
{
	/**
	 * Class which handles the persistent Action Bar located above the tabs
	 */
	private class ActionBar
	{
		/**
		 * Main button for the Action Bar, usually containing when the user
		 * should leave
		 */
		private final Button actionBarButton;
		/**
		 * Button to manually trigger the data to be refreshed
		 */
		private final ImageButton refreshButton;
		/**
		 * Button to trigger the transportation mode dialog
		 */
		private final ImageButton transportButton;

		/**
		 * Creates and sets up the Action Bar
		 */
		public ActionBar()
		{
			final SharedPreferences settings = getSharedPreferences(PREF, 0);
			final String travelType = settings.getString("TransportPreference",
					"driving");
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
			setTransportMode(travelType);
			refreshButton = (ImageButton) findViewById(R.id.refreshButton);
			refreshButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View view)
				{
					// Refresh the current tab's data
					final String tabTag = getTabHost().getCurrentTabTag();
					final Handler.Callback tab = (Handler.Callback) getLocalActivityManager()
							.getActivity(tabTag);
					tab.handleMessage(Message.obtain(null,
							AppService.MSG_REFRESH_DATA));
					handleMessage(Message.obtain(null,
							AppService.MSG_REFRESH_DATA));
				}
			});
		}

		/**
		 * Sets the color of the Action Bar
		 * 
		 * @param c
		 *            the color to set
		 */
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

		/**
		 * Sets the text on the actionBarButton
		 * 
		 * @param text
		 *            text to display
		 */
		public void setText(final String text)
		{
			actionBarButton.setText(text);
		}

		/**
		 * Sets the text and color of the Action Bar
		 * 
		 * @param leaveInMinutes
		 *            time until the user must leave
		 * @param notifyTimeInMin
		 *            user preference on when to be notified, used to determine
		 *            color
		 */
		public void setTextAndColor(final long leaveInMinutes,
				final int notifyTimeInMin)
		{
			COLOR actionBarColor = COLOR.GREEN;
			if (leaveInMinutes < notifyTimeInMin * .33333)
				actionBarColor = COLOR.RED;
			else if (leaveInMinutes < notifyTimeInMin * .6666)
				actionBarColor = COLOR.ORANGE;
			setColor(actionBarColor);
			final String formattedTime = EventEntry
					.formatWhenToLeave(leaveInMinutes);
			setText("Leave "
					+ (leaveInMinutes > 0 ? "in " + formattedTime : "Now"));
		}

		/**
		 * Sets the transportation mode icon
		 * 
		 * @param travelType
		 *            the transportation mode to set
		 */
		public void setTransportMode(final String travelType)
		{
			final Resources res = getResources();
			if (travelType.equals("driving"))
				transportButton.setImageDrawable(res
						.getDrawable(R.drawable.car_white55));
			else if (travelType.equals("bicycling"))
				transportButton.setImageDrawable(res
						.getDrawable(R.drawable.bicycle_white55));
			else if (travelType.equals("walking"))
				transportButton.setImageDrawable(res
						.getDrawable(R.drawable.person_white55));
		}
	}

	/**
	 * Possible Action Bar colors
	 */
	public enum COLOR {
		/**
		 * Green = Greater than 66% of Notify Time preference remaining
		 */
		GREEN, /**
		 * Orange = 33% - 66% of Notify Time preference remaining
		 */
		ORANGE, /**
		 * Red = <33% of Notify Time preference remaining
		 */
		RED
	}

	/**
	 * Dialog ID for transportation mode dialog box
	 */
	private static final int DIALOG_TRANSPORTATION = 100;
	/**
	 * Preferences name to load settings from
	 */
	private static final String PREF = "MyPrefs";
	/**
	 * Logging tag
	 */
	private static final String TAG = "TabbedInterfaceActivity";
	/**
	 * Action Bar controller
	 */
	private ActionBar actionBar;
	/**
	 * Current location of the device
	 */
	private Location currentLocation = null;
	/**
	 * Connection to the persistent, authorized service
	 */
	private final AppServiceConnection service = new AppServiceConnection(
			new Handler(this), true, true);

	/**
	 * Handles a getNextEventWithLocation event from the AppService
	 * 
	 * @param nextEvent
	 *            newly returned next event with a location
	 */
	private void handleGetNextEventWithLocation(final EventEntry nextEvent)
	{
		if (nextEvent == null)
			return;
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		final String travelType = settings.getString("TransportPreference",
				"driving");
		final int notifyTimeInMin = settings.getInt("NotifyTime", 3600) / 60;
		actionBar.setTextAndColor(
				nextEvent.getWhenToLeaveInMinutes(currentLocation, travelType),
				notifyTimeInMin);
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what)
		{
			case AppService.MSG_GET_NEXT_EVENT_WITH_LOCATION:
				final EventEntry nextEvent = (EventEntry) msg.obj;
				handleGetNextEventWithLocation(nextEvent);
				return true;
			case AppService.MSG_LOCATION_UPDATE:
				currentLocation = (Location) msg.obj;
				handleRefreshData();
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
		// Can't show WhenToLeave if we don't know where we are
		if (currentLocation == null)
			return;
		service.requestNextEventWithLocation();
	}

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
			case Logout.REQUEST_LOGOUT:
				finish();
				break;
			case Login.REQUEST_AUTHENTICATE:
				setContentView(R.layout.tabbed_interface);
				final Resources res = getResources(); // Resource object to get
				final TabHost tabHost = getTabHost(); // The activity TabHost
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
			startService(new Intent(this, AppService.class));
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
						editor.putString("TransportPreference", "driving");
						editor.commit();
						actionBar.setTransportMode("driving");
						Log.v(TAG,
								"Committed travel pref: "
										+ settings.getString(
												"TransportPreference",
												"driving"));
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
						editor.putString("TransportPreference", "bicycling");
						editor.commit();
						actionBar.setTransportMode("bicycling");
						Log.v(TAG,
								"Committed travel pref: "
										+ settings.getString(
												"TransportPreference",
												"bicycling"));
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
						editor.putString("TransportPreference", "walking");
						editor.commit();
						actionBar.setTransportMode("walking");
						Log.v(TAG,
								"Committed travel pref: "
										+ settings.getString(
												"TransportPreference",
												"walking"));
						transportDialog.dismiss();
					}
				});
				return transportDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		service.unregister();
		unbindService(service);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_view_calendars:
				startActivity(new Intent(this, Calendars.class));
				return true;
			case R.id.menu_preferences:
				startActivity(new Intent(this, Preferences.class));
				return true;
			case R.id.menu_logout:
				startActivityForResult(new Intent(this, Logout.class),
						Logout.REQUEST_LOGOUT);
				return true;
		}
		return false;
	}
}