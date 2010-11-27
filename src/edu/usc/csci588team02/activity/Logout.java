package edu.usc.csci588team02.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.SharedPreferences;
import edu.usc.csci588team02.service.AppServiceConnection;

public class Logout extends Activity implements Refreshable
{
	private static final String PREF = "MyPrefs";
	public static final int REQUEST_LOGOUT = 100;
	private final AppServiceConnection service = new AppServiceConnection(this);

	@Override
	public void refreshData()
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
