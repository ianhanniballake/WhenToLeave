package com.github.whentoleave.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.github.whentoleave.service.LocationService;
import com.github.whentoleave.service.LocationServiceConnection;

/**
 * Activity which logs the user out and returns to the calling activity
 */
public class Logout extends Activity implements Handler.Callback
{
	/**
	 * Preferences name to load settings from
	 */
	private static final String PREF = "MyPrefs";
	/**
	 * Activity request code corresponding with requesting logging out
	 */
	public static final int REQUEST_LOGOUT = 100;
	/**
	 * Connection to the persistent, authorized service
	 */
	private final LocationServiceConnection service = new LocationServiceConnection(
			new Handler(this));

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what)
		{
			case LocationService.MSG_REFRESH_DATA:
				handleRefreshData();
				return true;
			default:
				return false;
		}
	}

	/**
	 * Logs the user out and calls finish() on this activity
	 */
	public void handleRefreshData()
	{
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		final String accountName = settings.getString("accountName", null);
		final String authToken = settings.getString("authToken", null);
		if (accountName != null)
		{
			final AccountManager manager = AccountManager.get(this);
			final Account[] accounts = manager.getAccountsByType("com.google");
			for (final Account account : accounts)
				if (accountName.equals(account.name))
				{
					manager.invalidateAuthToken("com.google", authToken);
					service.invalidateAuthToken();
					final SharedPreferences.Editor editor = settings.edit();
					editor.remove("accountName");
					editor.remove("authToken");
					editor.commit();
					setResult(RESULT_OK);
					finish();
				}
		}
		finish();
	}
}
