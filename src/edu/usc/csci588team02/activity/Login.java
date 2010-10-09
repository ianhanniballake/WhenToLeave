package edu.usc.csci588team02.activity;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;

/**
 * Sample for Google Calendar Data API using the Atom wire format. It shows how
 * to authenticate, get calendars, add a new calendar, update it, and delete it.
 * <p>
 * To enable logging of HTTP requests/responses, run this command: {@code adb
 * shell setprop log.tag.HttpTransport DEBUG}. Then press-and-hold a calendar,
 * and enable "Logging".
 * </p>
 * 
 * @author Ian Lake
 */
public class Login extends Activity
{
	private static final String AUTH_TOKEN_TYPE = "cl";
	private static final int DIALOG_ACCOUNTS = 99;
	private static final String PREF = "MyPrefs";
	public static final int REQUEST_AUTHENTICATE = 99;
	private static final String TAG = "Login";
	private String authToken;

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
				try
				{
					final Bundle bundle = manager.getAuthToken(account,
							AUTH_TOKEN_TYPE, true, null, null).getResult();
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								if (bundle
										.containsKey(AccountManager.KEY_INTENT))
								{
									final Intent intent = bundle
											.getParcelable(AccountManager.KEY_INTENT);
									int flags = intent.getFlags();
									flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
									intent.setFlags(flags);
									startActivityForResult(intent,
											REQUEST_AUTHENTICATE);
								}
								else if (bundle
										.containsKey(AccountManager.KEY_AUTHTOKEN))
								{
									editor.putString(
											"authToken",
											bundle.getString(AccountManager.KEY_AUTHTOKEN));
									editor.commit();
									setResult(RESULT_OK);
									finish();
								}
							} catch (final Exception e)
							{
								handleException(e);
							}
						}
					});
				} catch (final Exception e)
				{
					handleException(e);
				}
			}
		}.start();
	}

	private void gotAccount(final boolean tokenExpired)
	{
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		final String accountName = settings.getString("accountName", null);
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

	private void handleException(final Exception e)
	{
		e.printStackTrace();
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		final boolean log = settings.getBoolean("logging", false);
		if (e instanceof HttpResponseException)
		{
			final HttpResponse response = ((HttpResponseException) e).response;
			final int statusCode = response.statusCode;
			try
			{
				response.ignore();
			} catch (final IOException e1)
			{
				e1.printStackTrace();
			}
			if (statusCode == 401 || statusCode == 403)
			{
				gotAccount(true);
				return;
			}
			if (log)
				try
				{
					Log.e(TAG, response.parseAsString());
				} catch (final IOException parseException)
				{
					parseException.printStackTrace();
				}
		}
		if (log)
			Log.e(TAG, e.getMessage(), e);
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
		gotAccount(false);
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
}
