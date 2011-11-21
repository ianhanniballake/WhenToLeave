package com.github.whentoleave.ui;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.github.whentoleave.R;
import com.github.whentoleave.model.EventEntry;
import com.github.whentoleave.service.AppService;
import com.github.whentoleave.service.AppServiceConnection;

/**
 * Activity which serves as the main hub of the application, containing the
 * Home, Agenda, and Map Activities as tabs and a persistent Action Bar
 */
public class MainActivity extends Activity implements Handler.Callback
{
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
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	public static class TabsAdapter extends FragmentPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener
	{
		static final class TabInfo
		{
			private final Bundle args;
			private final Class<?> clss;

			TabInfo(final Class<?> _class, final Bundle _args)
			{
				clss = _class;
				args = _args;
			}
		}

		private final ActionBar mActionBar;
		private final Context mContext;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
		private final ViewPager mViewPager;

		public TabsAdapter(final Activity activity, final ViewPager pager)
		{
			super(activity.getFragmentManager());
			mContext = activity;
			mActionBar = activity.getActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(final ActionBar.Tab tab, final Class<?> clss,
				final Bundle args)
		{
			final TabInfo info = new TabInfo(clss, args);
			tab.setTag(info);
			tab.setTabListener(this);
			mTabs.add(info);
			mActionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public int getCount()
		{
			return mTabs.size();
		}

		@Override
		public Fragment getItem(final int position)
		{
			final TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(),
					info.args);
		}

		@Override
		public void onPageScrolled(final int position,
				final float positionOffset, final int positionOffsetPixels)
		{
		}

		@Override
		public void onPageScrollStateChanged(final int state)
		{
		}

		@Override
		public void onPageSelected(final int position)
		{
			mActionBar.setSelectedNavigationItem(position);
		}

		@Override
		public void onTabReselected(final Tab tab, final FragmentTransaction ft)
		{
		}

		@Override
		public void onTabSelected(final Tab tab, final FragmentTransaction ft)
		{
			final Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++)
				if (mTabs.get(i) == tag)
					mViewPager.setCurrentItem(i);
		}

		@Override
		public void onTabUnselected(final Tab tab, final FragmentTransaction ft)
		{
		}
	}

	/**
	 * Class which handles the persistent Action Bar located above the tabs
	 */
	private class WhenToLeaveIndicator
	{
		/**
		 * Main button for the Action Bar, usually containing when the user
		 * should leave
		 */
		private final Button actionBarButton;

		/**
		 * Creates and sets up the Action Bar
		 */
		public WhenToLeaveIndicator()
		{
			// Setup Listeners for the ActionBar Buttons
			actionBarButton = (Button) findViewById(R.id.actionBar);
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
					break;
				case ORANGE:
					actionBarButton.setBackgroundDrawable(res
							.getDrawable(R.drawable.custom_action_bar_orange));
					break;
				case RED:
					actionBarButton.setBackgroundDrawable(res
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
	}

	/**
	 * Preferences name to load settings from
	 */
	private static final String PREF = "MyPrefs";
	/**
	 * Action Bar controller
	 */
	private WhenToLeaveIndicator actionBar;
	/**
	 * Current location of the device
	 */
	private Location currentLocation = null;
	TabsAdapter mTabsAdapter;
	private ViewPager mViewPager;
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
				actionBar = new WhenToLeaveIndicator();
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
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		// If notifications are enabled, keep the service running after the
		// program exits
		if (settings.getBoolean("EnableNotifications", true))
			startService(new Intent(this, AppService.class));
		startActivityForResult(new Intent(this, Login.class),
				Login.REQUEST_AUTHENTICATE);
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
			case R.id.menu_refresh:
				// Refresh the current tab's data
				final Handler.Callback tab = (Handler.Callback) mTabsAdapter
						.getItem(mViewPager.getCurrentItem());
				tab.handleMessage(Message.obtain(null,
						AppService.MSG_REFRESH_DATA));
				handleMessage(Message.obtain(null, AppService.MSG_REFRESH_DATA));
				return true;
			case R.id.menu_view_calendars:
				startActivity(new Intent(this, CalendarsActivity.class));
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

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
		final MenuItem transportModeMenuItem = menu
				.findItem(R.id.menu_transport_mode);
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
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
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		final SharedPreferences.Editor editor = settings.edit();
		editor.putString("TransportPreference", newTransportMode);
		editor.commit();
		// Request a call to onPrepareOptionsMenu so we can change the transport
		// mode icon
		invalidateOptionsMenu();
	}
}