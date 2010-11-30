package edu.usc.csci588team02.activity;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;

import edu.usc.csci588team02.service.AppService;
import edu.usc.csci588team02.service.AppServiceConnection;

/**
 * 
 * @author Ian Lake
 */
public class Login extends Activity implements Refreshable
{
	/**
	 * Authorization token type for Google Calendar access
	 */
	private static final String AUTH_TOKEN_TYPE = "cl";
	/**
	 * Dialog ID for account selection dialog box
	 */
	private static final int DIALOG_ACCOUNTS = 99;
	/**
	 * Preferences name to load settings from
	 */
	private static final String PREF = "MyPrefs";
	/**
	 * Activity request code corresponding with requesting authentication
	 */
	public static final int REQUEST_AUTHENTICATE = 99;
	/**
	 * Logging tag
	 */
	private static final String TAG = "Login";
	/**
	 * Connection to the persistent, to be authorized service
	 */
	private final AppServiceConnection service = new AppServiceConnection(this);

	/**
	 * Logs in to the given account, returning the authorization token
	 * 
	 * @param manager
	 *            Account Manager who owns the authorization tokens
	 * @param account
	 *            the account to get the authorization token for
	 * @return the authorization token of the now authenticated user or null on
	 *         error
	 */
	private String getAuthToken(final AccountManager manager,
			final Account account)
	{
		String authToken = null;
		try
		{
			final Bundle bundle = manager.getAuthToken(account,
					AUTH_TOKEN_TYPE, true, null, null).getResult();
			if (bundle.containsKey(AccountManager.KEY_INTENT))
			{
				final Intent intent = bundle
						.getParcelable(AccountManager.KEY_INTENT);
				int flags = intent.getFlags();
				flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
				intent.setFlags(flags);
				startActivityForResult(intent, REQUEST_AUTHENTICATE);
			}
			authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
		} catch (final Exception e)
		{
			Log.e(TAG, "Handling error " + e.getMessage(), e);
			if (e instanceof HttpResponseException)
			{
				final HttpResponse response = ((HttpResponseException) e).response;
				final int statusCode = response.statusCode;
				try
				{
					response.ignore();
				} catch (final IOException e1)
				{
					Log.w(TAG, "Error on ignoring HttpResponse", e1);
				}
				if (statusCode == 401 || statusCode == 403)
				{
					gotAccount(true);
					return authToken;
				}
				try
				{
					Log.e(TAG, response.parseAsString());
				} catch (final IOException parseException)
				{
					Log.w(TAG, "Error on parsing response", parseException);
				}
			}
		}
		return authToken;
	}

	/**
	 * Logs in to the requested account, storing the account name and
	 * authorization token in the preferences to mark that we are now
	 * authenticated. Also authenticates the service with the same authorization
	 * token before calling finish() on this activity.
	 * 
	 * @param manager
	 *            Account Manager who owns the authorization tokens
	 * @param account
	 *            the account to log into
	 */
	private void gotAccount(final AccountManager manager, final Account account)
	{
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		final SharedPreferences.Editor editor = settings.edit();
		editor.putString("accountName", account.name);
		editor.commit();
		new Thread()
		{
			@Override
			public void run()
			{
				final String authToken = getAuthToken(manager, account);
				editor.putString("authToken", authToken);
				editor.commit();
				setResult(RESULT_OK);
				service.setAuthToken(authToken);
				finish();
			}
		}.start();
	}

	/**
	 * Attempts to determine if the user is already logged in. If they are,
	 * optionally expire their token and attempt to authorize the user.
	 * 
	 * @param tokenExpired
	 *            whether the authorization token is expired
	 */
	private void gotAccount(final boolean tokenExpired)
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
					if (tokenExpired)
						manager.invalidateAuthToken("com.google", authToken);
					gotAccount(manager, account);
					return;
				}
		}
		showDialog(DIALOG_ACCOUNTS);
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case REQUEST_AUTHENTICATE:
				if (resultCode == RESULT_OK)
					gotAccount(false);
				else
					showDialog(DIALOG_ACCOUNTS);
				break;
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// As we must authenticate a connected service, we kick off the Login
		// process after we are connected
		bindService(new Intent(this, AppService.class), service,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected Dialog onCreateDialog(final int id)
	{
		switch (id)
		{
			case DIALOG_ACCOUNTS:
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						this);
				builder.setTitle("Please login to a Google Account");
				final AccountManager manager = AccountManager.get(this);
				final Account[] accounts = manager
						.getAccountsByType("com.google");
				final int size = accounts.length;
				final String[] names = new String[size];
				for (int i = 0; i < size; i++)
					names[i] = accounts[i].name;
				builder.setItems(names, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(final DialogInterface dialog,
							final int which)
					{
						gotAccount(manager, accounts[which]);
					}
				});
				return builder.create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(service);
	}

	/**
	 * Kick off the login process as we now know we are connected to the service
	 */
	@Override
	public void refreshData()
	{
		gotAccount(false);
	}
}
