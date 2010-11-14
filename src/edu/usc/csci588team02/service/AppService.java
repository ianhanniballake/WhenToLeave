package edu.usc.csci588team02.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author Kevin Kirkpatrick
 */
public class AppService extends Service
{
	private static final long INTERVAL = 30000; // 30 seconds = 30000
	private static final String TAG = "AppService";
	private final Timer timer = new Timer();

	@Override
	public IBinder onBind(final Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate()
	{
		// Toast.makeText(this, "App Servcie Created",
		// Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		startUpService();
	}

	@Override
	public void onDestroy()
	{
		// Toast.makeText(this, "App Service Stops", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onStart(final Intent intent, final int startid)
	{
		// Toast.makeText(this, "App Service Started",
		// Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
	}

	private void startUpService()
	{
		timer.scheduleAtFixedRate(new TimerTask()
		{
			// this activity will run every defined interval.
			@Override
			public void run()
			{
				Log.d(TAG, "TimerKick");
			}
		}, 0, INTERVAL);
	}
}
