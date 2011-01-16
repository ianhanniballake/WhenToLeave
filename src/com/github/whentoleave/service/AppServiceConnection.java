package com.github.whentoleave.service;

import java.util.Date;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Serves as the primary connection to the AppService when an Activity or
 * Service wishes to bind to the AppService
 */
public class AppServiceConnection implements ServiceConnection
{
	private static final String TAG = "AppServiceConnection";
	/**
	 * Client to send reply messages to
	 */
	private Messenger client = null;
	/**
	 * Register for interval refreshes
	 */
	private boolean intervalRefresh = false;
	/**
	 * Register for location updates
	 */
	private boolean locationUpdates = false;
	/**
	 * Underlying service
	 */
	private Messenger service = null;

	/**
	 * Constructor for a one time refresh
	 * 
	 * @param client
	 *            client to send reply messages to
	 */
	public AppServiceConnection(final Handler client)
	{
		this(client, false, false);
	}

	/**
	 * Constructor for a one time refresh of the given Refreshable instance and
	 * LocationAware listener to register for ongoing location updates
	 * 
	 * @param client
	 *            client to send reply messages to
	 * @param intervalRefresh
	 *            if the client should receive periodic updates
	 * @param locationUpdates
	 *            if the client should receive location updates
	 */
	public AppServiceConnection(final Handler client,
			final boolean intervalRefresh, final boolean locationUpdates)
	{
		this.client = new Messenger(client);
		this.intervalRefresh = intervalRefresh;
		this.locationUpdates = locationUpdates;
	}

	/**
	 * Effectively logs the user out, invalidating their authentication token.
	 * Note that all queries done between now and future authentication will
	 * fail
	 */
	public void invalidateAuthToken()
	{
		sendMessage(AppService.MSG_INVALIDATE_AUTH_TOKEN);
	}

	/**
	 * Returns the connection status of this AppServiceConnection
	 * 
	 * @return if we are connected to the underlying service
	 */
	public boolean isConnected()
	{
		return service != null;
	}

	@Override
	public void onServiceConnected(final ComponentName name,
			final IBinder serviceBinder)
	{
		Log.d(TAG, "onServiceConnected: " + name);
		service = new Messenger(serviceBinder);
		if (locationUpdates)
			sendMessage(AppService.MSG_REGISTER_LOCATION_LISTENER);
		if (intervalRefresh)
			sendMessage(AppService.MSG_REGISTER_REFRESHABLE);
		else
			sendMessage(AppService.MSG_REFRESH_DATA);
	}

	@Override
	public void onServiceDisconnected(final ComponentName name)
	{
		Log.d(TAG, "onServiceDisconnected");
		service = null;
	}

	/**
	 * Gets a list of all of the authenticated user's calendars. Assumes that
	 * the service is already authenticated
	 */
	public void requestCalendars()
	{
		sendMessage(AppService.MSG_GET_CALENDARS);
	}

	/**
	 * Gets a particular EventEntry given its URL. Assumes that the service is
	 * already authenticated
	 * 
	 * @param eventUrl
	 *            the URL of the EventEntry to return
	 */
	public void requestEvent(final String eventUrl)
	{
		sendMessage(AppService.MSG_GET_EVENT, eventUrl);
	}

	/**
	 * Gets all events in a given Date range. Assumes that the service is
	 * already authenticated
	 * 
	 * @param start
	 *            start date
	 * @param end
	 *            end date
	 */
	public void requestEvents(final Date start, final Date end)
	{
		final Date[] dateRange = { start, end };
		sendMessage(AppService.MSG_GET_EVENTS, dateRange);
	}

	/**
	 * Finds the next event across all calendars (chronologically) that has a
	 * location. Searches in an exponentially larger date range until it finds
	 * an event (first 1 day, then 2, then 4, etc). Assumes that the service is
	 * already authenticated
	 */
	public void requestNextEventWithLocation()
	{
		sendMessage(AppService.MSG_GET_NEXT_EVENT_WITH_LOCATION);
	}

	private void sendMessage(final int what)
	{
		sendMessage(what, null);
	}

	private void sendMessage(final int what, final Object obj)
	{
		try
		{
			if (service != null)
			{
				final Message message = Message.obtain(null, what, obj);
				message.replyTo = client;
				service.send(message);
			}
		} catch (final RemoteException e)
		{
			service = null;
		}
	}

	/**
	 * Authorizes the service with the given authToken
	 * 
	 * @param authToken
	 *            authToken used to authenticate any Google API queries
	 */
	public void setAuthToken(final String authToken)
	{
		sendMessage(AppService.MSG_SET_AUTH_TOKEN, authToken);
	}

	public void unregister()
	{
		Log.d(TAG, "unregister");
		if (locationUpdates)
			sendMessage(AppService.MSG_UNREGISTER_LOCATION_LISTENER);
		if (intervalRefresh)
			sendMessage(AppService.MSG_UNREGISTER_REFRESHABLE);
	}
}
