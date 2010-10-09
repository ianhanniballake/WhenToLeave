package edu.usc.csci588team02.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Logout extends Activity
{
	private static final String PREF = "MyPrefs";
	public static final int REQUEST_LOGOUT = 100;

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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
