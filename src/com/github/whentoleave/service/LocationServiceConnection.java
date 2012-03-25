package com.github.whentoleave.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.github.whentoleave.BuildConfig;

/**
 * Serves as the primary connection to the AppService when an Activity or
 * Service wishes to bind to the AppService
 */
public class LocationServiceConnection implements ServiceConnection
{
	/**
	 * Logging tag
	 */
	private static final String TAG = "AppServiceConnection";
	/**
	 * Client to send reply messages to
	 */
	private Messenger client = null;
	/**
	 * Underlying service
	 */
	private Messenger service = null;

	/**
	 * Constructor for a one time LocationAware listener to register for ongoing
	 * location updates
	 * 
	 * @param client
	 *            client to send reply messages to
	 */
	public LocationServiceConnection(final Handler client)
	{
		this.client = new Messenger(client);
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
		if (BuildConfig.DEBUG)
			Log.d(LocationServiceConnection.TAG, "onServiceConnected: " + name);
		service = new Messenger(serviceBinder);
		sendMessage(LocationService.MSG_REGISTER_LOCATION_LISTENER);
	}

	@Override
	public void onServiceDisconnected(final ComponentName name)
	{
		if (BuildConfig.DEBUG)
			Log.d(LocationServiceConnection.TAG, "onServiceDisconnected");
		service = null;
	}

	/**
	 * Sends out the specified type of message
	 * 
	 * @param what
	 *            type of message
	 */
	private void sendMessage(final int what)
	{
		sendMessage(what, null);
	}

	/**
	 * Sends out specified type of message with an attached object
	 * 
	 * @param what
	 *            type of message
	 * @param obj
	 *            attached object
	 */
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
	 * Unregisters the component from location updates and interval refreshes as
	 * appropriate
	 */
	public void unregister()
	{
		if (BuildConfig.DEBUG)
			Log.d(LocationServiceConnection.TAG, "unregister");
		sendMessage(LocationService.MSG_UNREGISTER_LOCATION_LISTENER);
	}
}
