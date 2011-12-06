package com.github.whentoleave.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CursorAdapter;

import com.github.whentoleave.R;
import com.github.whentoleave.maps.RouteInformation;
import com.github.whentoleave.service.LocationService;
import com.github.whentoleave.service.LocationServiceConnection;
import com.google.android.maps.MapActivity;

/**
 * Activity which serves as the main hub of the application, containing the
 * Home, Agenda, and Map Activities as tabs
 */
public class MainActivity extends MapActivity implements
		LoaderCallbacks<Cursor>, Handler.Callback
{
	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated Action Bar tabs.
	 */
	public static class TabsAdapter extends FragmentPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener
	{
		/**
		 * Class which stores information required to create Fragment Tabs
		 */
		private static final class TabInfo
		{
			/**
			 * Arguments to pass on to the Fragment
			 */
			private final Bundle args;
			/**
			 * Fragment class to instantiate
			 */
			private final Class<? extends Fragment> clss;

			/**
			 * Create a new TabInfo
			 * 
			 * @param _class
			 *            Fragment class to instantiate
			 * @param _args
			 *            Arguments to pass on to the Fragment
			 */
			public TabInfo(final Class<? extends Fragment> _class,
					final Bundle _args)
			{
				clss = _class;
				args = _args;
			}
		}

		/**
		 * Reference to the ActionBar
		 */
		private final ActionBar actionBar;
		/**
		 * Reference to the current context
		 */
		private final Context context;
		/**
		 * List of Fragment tabs' information
		 */
		private final ArrayList<TabInfo> tabInfo = new ArrayList<TabInfo>();
		/**
		 * Reference to the ViewPager
		 */
		private final ViewPager viewPager;

		/**
		 * Creates a new TabsAdapter, tying together the ActionBar's tabs and a
		 * ViewPager
		 * 
		 * @param activity
		 *            Activity hosting the ActionBar
		 * @param pager
		 *            ViewPager that will hold the tabs
		 */
		public TabsAdapter(final Activity activity, final ViewPager pager)
		{
			super(activity.getFragmentManager());
			context = activity;
			actionBar = activity.getActionBar();
			viewPager = pager;
			viewPager.setAdapter(this);
			viewPager.setOnPageChangeListener(this);
		}

		/**
		 * @param tab
		 *            Tab to add to the Action Bar
		 * @param clss
		 *            Fragment class to instantiate
		 * @param args
		 *            Arguments to pass on to the Fragment
		 */
		public void addTab(final ActionBar.Tab tab,
				final Class<? extends Fragment> clss, final Bundle args)
		{
			final TabInfo info = new TabInfo(clss, args);
			tab.setTag(info);
			tab.setTabListener(this);
			tabInfo.add(info);
			actionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public int getCount()
		{
			return tabInfo.size();
		}

		@Override
		public Fragment getItem(final int position)
		{
			final TabInfo info = tabInfo.get(position);
			return Fragment
					.instantiate(context, info.clss.getName(), info.args);
		}

		@Override
		public void onPageScrolled(final int position,
				final float positionOffset, final int positionOffsetPixels)
		{
			// Nothing to do
		}

		@Override
		public void onPageScrollStateChanged(final int state)
		{
			// Nothing to do
		}

		@Override
		public void onPageSelected(final int position)
		{
			actionBar.setSelectedNavigationItem(position);
		}

		@Override
		public void onTabReselected(final Tab tab, final FragmentTransaction ft)
		{
			// Nothing to do
		}

		@Override
		public void onTabSelected(final Tab tab, final FragmentTransaction ft)
		{
			final Object tag = tab.getTag();
			for (int i = 0; i < tabInfo.size(); i++)
				if (tabInfo.get(i) == tag)
					viewPager.setCurrentItem(i);
		}

		@Override
		public void onTabUnselected(final Tab tab, final FragmentTransaction ft)
		{
			// Nothing to do
		}
	}

	/**
	 * Preferences name to load settings from
	 */
	private static final String PREF = "MyPrefs";

	/**
	 * Formats the given number of minutes into usable String. For <60 minutes,
	 * returns "MMm", with no leading 0 (i.e., 6m or 15m). For >=60 minutes,
	 * returns "HH:MMh" with no leading hour 0 (i.e., 1:04h or 11:15h)
	 * 
	 * @param leaveInMinutes
	 *            the number of minutes to be formatted
	 * @return a formatted string representing the given leaveInMinutes in "MMm"
	 *         (<60) or "HH:MMh" (>=60)
	 */
	private static String formatWhenToLeave(final long leaveInMinutes)
	{
		final long hoursToGo = Math.abs(leaveInMinutes) / 60;
		final long minutesToGo = Math.abs(leaveInMinutes) % 60;
		final StringBuffer formattedTime = new StringBuffer();
		if (hoursToGo > 0)
		{
			formattedTime.append(hoursToGo);
			formattedTime.append(":");
			if (minutesToGo < 10)
				formattedTime.append("0");
			formattedTime.append(minutesToGo);
			formattedTime.append("h");
		}
		else
		{
			formattedTime.append(minutesToGo);
			formattedTime.append("m");
		}
		return formattedTime.toString();
	}

	/**
	 * Adapter to the retrieved data
	 */
	private CursorAdapter adapter;
	/**
	 * Current location of the device
	 */
	private Location currentLocation = null;
	/**
	 * Tab/ViewPager adapter
	 */
	private TabsAdapter mTabsAdapter;
	/**
	 * Reference to the ViewPager showing the tabs
	 */
	private ViewPager mViewPager;
	/**
	 * Connection to the persistent location service
	 */
	private final LocationServiceConnection service = new LocationServiceConnection(
			new Handler(this));

	@Override
	public boolean handleMessage(final Message msg)
	{
		if (msg.what == LocationService.MSG_LOCATION_UPDATE && msg.obj != null)
		{
			currentLocation = (Location) msg.obj;
			return true;
		}
		return false;
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Need to tie this to EventMapFragment's isRouteDisplayed
		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbed_interface);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		mTabsAdapter.addTab(bar.newTab().setText(getText(R.string.title_home)),
				HomeFragment.class, null);
		mTabsAdapter.addTab(bar.newTab()
				.setText(getText(R.string.title_agenda)), AgendaFragment.class,
				null);
		mTabsAdapter.addTab(bar.newTab().setText(getText(R.string.title_map)),
				EventMapFragment.class, null);
		if (savedInstanceState != null)
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		final SharedPreferences settings = getSharedPreferences(
				MainActivity.PREF, 0);
		// If notifications are enabled, keep the service running after the
		// program exits
		if (settings.getBoolean("EnableNotifications", true))
			startService(new Intent(this, LocationService.class));
		bindService(new Intent(this, LocationService.class), service,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		final Calendar twoWeeksFromNow = Calendar.getInstance();
		twoWeeksFromNow.add(Calendar.DATE, 14);
		final String selection = CalendarContract.Events.DTSTART + ">=? AND "
				+ CalendarContract.Events.DTEND + "<? AND "
				+ CalendarContract.Events.EVENT_LOCATION + " IS NOT NULL";
		final String selectionArgs[] = {
				Long.toString(Calendar.getInstance().getTimeInMillis()),
				Long.toString(twoWeeksFromNow.getTimeInMillis()) };
		final String[] projection = { BaseColumns._ID,
				CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART,
				CalendarContract.Events.EVENT_LOCATION };
		return new CursorLoader(this, CalendarContract.Events.CONTENT_URI,
				projection, selection, selectionArgs,
				CalendarContract.Events.DTSTART);
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
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		adapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		adapter.swapCursor(data);
		if (!data.moveToFirst() || currentLocation == null)
			return;
		setIndicatorTextAndColor(data);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_view_calendars:
				startActivity(new Intent(this, CalendarsActivity.class));
				return true;
			case R.id.menu_preferences:
				startActivity(new Intent(this, Preferences.class));
				return true;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
		final MenuItem transportModeMenuItem = menu
				.findItem(R.id.menu_transport_mode);
		final SharedPreferences settings = getSharedPreferences(
				MainActivity.PREF, 0);
		final String transportMode = settings.getString("TransportPreference",
				"driving");
		if (transportMode.equals("driving"))
			transportModeMenuItem.setIcon(menu.findItem(
					R.id.menu_transport_mode_car).getIcon());
		else if (transportMode.equals("bicycling"))
			transportModeMenuItem.setIcon(menu.findItem(
					R.id.menu_transport_mode_bicycle).getIcon());
		else if (transportMode.equals("walking"))
			transportModeMenuItem.setIcon(menu.findItem(
					R.id.menu_transport_mode_walking).getIcon());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}

	/**
	 * Custom menu onClick handler for transportation mode menu items
	 * 
	 * @param item
	 *            menu item clicked
	 */
	public void onSelectTransportMode(final MenuItem item)
	{
		String newTransportMode = null;
		switch (item.getItemId())
		{
			case R.id.menu_transport_mode_car:
				newTransportMode = "driving";
				break;
			case R.id.menu_transport_mode_bicycle:
				newTransportMode = "bicycling";
				break;
			case R.id.menu_transport_mode_walking:
				newTransportMode = "walking";
				break;
			default:
				return;
		}
		final SharedPreferences settings = getSharedPreferences(
				MainActivity.PREF, 0);
		final SharedPreferences.Editor editor = settings.edit();
		editor.putString("TransportPreference", newTransportMode);
		editor.commit();
		// Request a call to onPrepareOptionsMenu so we can change the transport
		// mode icon
		invalidateOptionsMenu();
	}

	/**
	 * Sets the text and color of the Action Bar
	 * 
	 * @param data
	 *            cursor pointing to the event
	 */
	private void setIndicatorTextAndColor(final Cursor data)
	{
		final SharedPreferences settings = getSharedPreferences(
				MainActivity.PREF, 0);
		final String travelType = settings.getString("TransportPreference",
				"driving");
		final int notifyTimeInMin = settings.getInt("NotifyTime", 3600) / 60;
		final int locationColumnIndex = data
				.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
		final String location = data.getString(locationColumnIndex);
		final int startTimeColumnIndex = data
				.getColumnIndex(CalendarContract.Events.DTSTART);
		final long startTime = data.getLong(startTimeColumnIndex);
		final int travelTime = RouteInformation.getDuration(currentLocation,
				location, travelType);
		final long minutesUntilEvent = (startTime - new Date().getTime()) / 60000;
		final long leaveInMinutes = minutesUntilEvent - travelTime;
		final Button actionBarButton = (Button) findViewById(R.id.actionBar);
		final Resources res = getResources();
		if (leaveInMinutes < notifyTimeInMin * .33333)
			actionBarButton.setBackgroundDrawable(res
					.getDrawable(R.drawable.custom_action_bar_red));
		else if (leaveInMinutes < notifyTimeInMin * .6666)
			actionBarButton.setBackgroundDrawable(res
					.getDrawable(R.drawable.custom_action_bar_orange));
		else
			actionBarButton.setBackgroundDrawable(res
					.getDrawable(R.drawable.custom_action_bar_green));
		final String formattedTime = MainActivity
				.formatWhenToLeave(leaveInMinutes);
		actionBarButton.setText("Leave "
				+ (leaveInMinutes > 0 ? "in " + formattedTime : "Now"));
	}
}