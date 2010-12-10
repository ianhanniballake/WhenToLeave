package com.github.whentoleave.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.github.whentoleave.R;

/**
 * Activity to manage user preferences
 */
public class Preferences extends Activity
{
	/**
	 * Listener for when users select a new notification time setting
	 */
	private class NotifyTimeListener implements OnItemSelectedListener
	{
		@Override
		public void onItemSelected(final AdapterView<?> parent, final View v,
				final int pos, final long row)
		{
			final SharedPreferences settings = getSharedPreferences(PREF, 0);
			final SharedPreferences.Editor editor = settings.edit();
			final Resources r = getResources();
			final int[] iValues = r.getIntArray(R.array.notify_time_values);
			int notifyTime = 3600;
			if (pos > 0 && pos <= iValues.length)
				notifyTime = iValues[pos];
			editor.putInt("NotifyTime", notifyTime);
			editor.commit();
			Log.v(TAG, "Clicked on: " + notifyTime);
			Log.v(TAG, "Committed: " + settings.getInt("NotifyTime", 0));
		}

		@Override
		public void onNothingSelected(final AdapterView<?> parent)
		{
		}
	}

	/**
	 * Listener for when users select a new refresh interval setting
	 */
	private class RefreshTimeListener implements OnItemSelectedListener
	{
		@Override
		public void onItemSelected(final AdapterView<?> parent, final View v,
				final int pos, final long row)
		{
			final SharedPreferences settings = getSharedPreferences(PREF, 0);
			final SharedPreferences.Editor editor = settings.edit();
			final Resources r = getResources();
			final int[] iValues = r.getIntArray(R.array.interval_values);
			int interval = 60;
			if (pos > 0 && pos <= iValues.length)
				interval = iValues[pos];
			editor.putInt("RefreshInterval", interval);
			editor.commit();
			Log.v(TAG, "Clicked on: " + interval);
			Log.v(TAG, "Committed: " + settings.getInt("RefreshInterval", 0));
		}

		@Override
		public void onNothingSelected(final AdapterView<?> parent)
		{
		}
	}

	/**
	 * Preferences name to load settings from
	 */
	private static final String PREF = "MyPrefs";
	/**
	 * Logging tag
	 */
	private static final String TAG = "PreferencesActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		final Resources r = getResources();
		final int[] iValues = r.getIntArray(R.array.interval_values);
		final int[] notifyValues = r.getIntArray(R.array.notify_time_values);
		// Get current preferences
		final SharedPreferences prefs = getSharedPreferences(PREF, 0);
		final int interval = prefs.getInt("RefreshInterval", 60);
		final int notifyTime = prefs.getInt("NotifyTime", 3600);
		final boolean enableNotifications = prefs.getBoolean(
				"EnableNotifications", true);
		final String actionBarPref = prefs.getString("ActionBarPreference",
				"EventDetails");
		// Setup refresh spinner data and callback
		final Spinner refreshTime = (Spinner) findViewById(R.id.spinnerRefreshTime);
		ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(
				this, R.array.intervals, android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		refreshTime.setAdapter(mAdapter);
		refreshTime.setOnItemSelectedListener(new RefreshTimeListener());
		// set initial value to current preference for spinner
		refreshTime.setSelection(
				java.util.Arrays.binarySearch(iValues, interval), false);
		// Setup notify before spinner data and callback
		final Spinner notifyTimeSpinner = (Spinner) findViewById(R.id.spinnerNotifyTime);
		mAdapter = ArrayAdapter.createFromResource(this, R.array.notify_time,
				android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		notifyTimeSpinner.setAdapter(mAdapter);
		notifyTimeSpinner.setOnItemSelectedListener(new NotifyTimeListener());
		// set initial value to current preference for spinner
		notifyTimeSpinner.setSelection(
				java.util.Arrays.binarySearch(notifyValues, notifyTime), false);
		// Setup radio button data and callbacks
		final RadioButton rbActionButtonPrefDetails = (RadioButton) findViewById(R.id.rbActionButtonPrefDetails);
		rbActionButtonPrefDetails.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				final SharedPreferences settings = getSharedPreferences(PREF, 0);
				final SharedPreferences.Editor editor = settings.edit();
				editor.putString("ActionBarPreference", "EventDetails");
				editor.commit();
				Log.v(TAG,
						"Should have commit rbActionButtonPrefDetails: "
								+ settings.getString("ActionBarPreference",
										"EventDetails"));
			}
		});
		final RadioButton rbActionButtonPrefMap = (RadioButton) findViewById(R.id.rbActionButtonPrefMap);
		rbActionButtonPrefMap.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				final SharedPreferences settings = getSharedPreferences(PREF, 0);
				final SharedPreferences.Editor editor = settings.edit();
				editor.putString("ActionBarPreference", "Map");
				editor.commit();
				Log.v(TAG, "Should have commit rbActionButtonPrefMap: "
						+ settings.getString("ActionBarPreference", "Map"));
			}
		});
		final RadioButton rbActionButtonPrefNav = (RadioButton) findViewById(R.id.rbActionButtonPrefNav);
		rbActionButtonPrefNav.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				final SharedPreferences settings = getSharedPreferences(PREF, 0);
				final SharedPreferences.Editor editor = settings.edit();
				editor.putString("ActionBarPreference", "Navigate");
				editor.commit();
				Log.v(TAG, "Should have commit rbActionButtonPrefNav: "
						+ settings.getString("ActionBarPreference", "Navigate"));
			}
		});
		// Setup radio button initial configuration
		if (actionBarPref.equals("EventDetails"))
			rbActionButtonPrefDetails.setChecked(true);
		else if (actionBarPref.equals("Map"))
			rbActionButtonPrefMap.setChecked(true);
		else if (actionBarPref.equals("Navigate"))
			rbActionButtonPrefNav.setChecked(true);
		else
			rbActionButtonPrefDetails.setChecked(true);
		// Setup Notifications Enabled checkbox
		final CheckBox cbEnableNotifications = (CheckBox) findViewById(R.id.cbEnableNotifications);
		cbEnableNotifications.setChecked(enableNotifications);
		cbEnableNotifications
				.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(
							final CompoundButton buttonView,
							final boolean isChecked)
					{
						final SharedPreferences settings = getSharedPreferences(
								PREF, 0);
						final SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean("EnableNotifications", isChecked);
						editor.commit();
						Log.v(TAG,
								"Should have commit EnableNotifications: "
										+ settings.getBoolean(
												"EnableNotifications", true));
					}
				});
		Log.v(TAG, "Creating Preferences Activity, and interval is: "
				+ interval);
		Log.v(TAG, "Creating Preferences Activity, and pref is: "
				+ actionBarPref);
	}
}