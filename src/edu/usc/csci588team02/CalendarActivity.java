package edu.usc.csci588team02;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;

import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.model.CalendarFeed;
import edu.usc.csci588team02.model.CalendarUrl;
import edu.usc.csci588team02.model.Namespace;

/**
 * Sample for Google Calendar Data API using the Atom wire format. It shows how
 * to authenticate, get calendars, add a new calendar, update it, and delete it.
 * <p>
 * To enable logging of HTTP requests/responses, run this command: {@code adb
 * shell setprop log.tag.HttpTransport DEBUG}. Then press-and-hold a calendar,
 * and enable "Logging".
 * </p>
 * 
 * @author Yaniv Inbar
 */
public final class CalendarActivity extends ListActivity
{
	private static final String AUTH_TOKEN_TYPE = "cl";
	private static final int CONTEXT_DELETE = 1;
	private static final int CONTEXT_EDIT = 0;
	private static final int CONTEXT_LOGGING = 2;
	private static final int DIALOG_ACCOUNTS = 0;
	private static final int MENU_ACCOUNTS = 1;
	private static final int MENU_ADD = 0;
	private static final String PREF = "MyPrefs";
	private static final int REQUEST_AUTHENTICATE = 0;
	private static final String TAG = "CalendarSample";
	private static HttpTransport transport;
	private String authToken;
	private final List<CalendarEntry> calendars = new ArrayList<CalendarEntry>();

	public CalendarActivity()
	{
		HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
		transport = GoogleTransport.create();
		final GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName("google-calendarandroidsample-1.0");
		headers.gdataVersion = "2";
		final AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Namespace.DICTIONARY;
		transport.addParser(parser);
	}

	private void authenticated()
	{
		executeRefreshCalendars();
	}

	private void authenticatedClientLogin(final String newAuthToken)
	{
		authToken = newAuthToken;
		((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
		authenticated();
	}

	private void executeRefreshCalendars()
	{
		String[] calendarNames;
		calendars.clear();
		try
		{
			final CalendarUrl url = CalendarUrl.forAllCalendarsFeed();
			// page through results
			while (true)
			{
				final CalendarFeed feed = CalendarFeed.executeGet(transport,
						url);
				if (feed.calendars != null)
					calendars.addAll(feed.calendars);
				final String nextLink = feed.getNextLink();
				if (nextLink == null)
					break;
			}
			final int numCalendars = calendars.size();
			calendarNames = new String[numCalendars];
			for (int i = 0; i < numCalendars; i++)
				calendarNames[i] = calendars.get(i).title;
		} catch (final IOException e)
		{
			handleException(e);
			calendarNames = new String[] { e.getMessage() };
			calendars.clear();
		}
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, calendarNames));
	}

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
									authenticatedClientLogin(bundle
											.getString(AccountManager.KEY_AUTHTOKEN));
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
			final int size = accounts.length;
			for (int i = 0; i < size; i++)
			{
				final Account account = accounts[i];
				if (accountName.equals(account.name))
				{
					if (tokenExpired)
						manager.invalidateAuthToken("com.google", authToken);
					gotAccount(manager, account);
					return;
				}
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
	public boolean onContextItemSelected(final MenuItem item)
	{
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final CalendarEntry calendar = calendars.get((int) info.id);
		try
		{
			switch (item.getItemId())
			{
				case CONTEXT_EDIT:
					final CalendarEntry patchedCalendar = calendar.clone();
					patchedCalendar.title = calendar.title + " UPDATED "
							+ new DateTime(new Date());
					patchedCalendar.executePatchRelativeToOriginal(transport,
							calendar);
					executeRefreshCalendars();
					return true;
				case CONTEXT_DELETE:
					calendar.executeDelete(transport);
					executeRefreshCalendars();
					return true;
				default:
					return super.onContextItemSelected(item);
			}
		} catch (final IOException e)
		{
			handleException(e);
		}
		return false;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getListView().setTextFilterEnabled(true);
		registerForContextMenu(getListView());
		getIntent();
		gotAccount(false);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_EDIT, 0, "Update Title");
		menu.add(0, CONTEXT_DELETE, 0, "Delete");
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		final boolean logging = settings.getBoolean("logging", false);
		menu.add(0, CONTEXT_LOGGING, 0, "Logging").setCheckable(true)
				.setChecked(logging);
	}

	@Override
	protected Dialog onCreateDialog(final int id)
	{
		switch (id)
		{
			case DIALOG_ACCOUNTS:
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						this);
				builder.setTitle("Select a Google account");
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
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, MENU_ADD, 0, "New calendar");
		menu.add(0, MENU_ACCOUNTS, 0, "Switch Account");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_ADD:
				final CalendarUrl url = CalendarUrl.forOwnCalendarsFeed();
				final CalendarEntry calendar = new CalendarEntry();
				calendar.title = "Calendar " + new DateTime(new Date());
				try
				{
					calendar.executeInsert(transport, url);
				} catch (final IOException e)
				{
					handleException(e);
				}
				executeRefreshCalendars();
				return true;
			case MENU_ACCOUNTS:
				showDialog(DIALOG_ACCOUNTS);
				return true;
		}
		return false;
	}
}
