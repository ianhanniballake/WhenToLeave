package edu.usc.csci588team02.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import edu.usc.csci588team02.activity.LocationAware;
import edu.usc.csci588team02.activity.Refreshable;
import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.service.AppService.AppServiceBinder;

public class AppServiceConnection implements ServiceConnection
{
	private boolean refreshOnTimer = false;
	private AppServiceBinder service = null;
	private Refreshable toRefresh = null;
	private LocationAware toUpdateLocation = null;

	public AppServiceConnection()
	{
		this(null, null, false);
	}

	public AppServiceConnection(final LocationAware toUpdateLocation)
	{
		this(null, toUpdateLocation, false);
	}

	public AppServiceConnection(final Refreshable toRefreshOnConnected)
	{
		this(toRefreshOnConnected, null, false);
	}

	public AppServiceConnection(final Refreshable toRefreshOnConnected,
			final LocationAware toUpdateLocation)
	{
		this(toRefreshOnConnected, toUpdateLocation, false);
	}

	public AppServiceConnection(final Refreshable toRefresh,
			final LocationAware toUpdateLocation, final boolean refreshOnTimer)
	{
		this.toRefresh = toRefresh;
		this.toUpdateLocation = toUpdateLocation;
		this.refreshOnTimer = refreshOnTimer;
	}

	public List<CalendarEntry> getCalendars() throws IOException
	{
		return service.getCalendars();
	}

	public EventEntry getEvent(final String eventUrl) throws IOException
	{
		return service.getEvent(eventUrl);
	}

	public Set<EventEntry> getEvents(final Date start, final Date end)
			throws IOException
	{
		return service.getEvents(start, end);
	}

	public Set<EventEntry> getEventsStartingNow(final Date end)
			throws IOException
	{
		return service.getEvents(new Date(), end);
	}

	public EventEntry getNextEventWithLocation() throws IOException
	{
		return service.getNextEventWithLocation();
	}

	public boolean isAuthenticated()
	{
		return service.isAuthenticated();
	}

	public boolean isConnected()
	{
		return service != null;
	}

	@Override
	public void onServiceConnected(final ComponentName name,
			final IBinder serviceBinder)
	{
		service = (AppServiceBinder) serviceBinder;
		if (toUpdateLocation != null)
			service.addLocationListener(toUpdateLocation);
		if (refreshOnTimer)
			service.addRefreshOnTimerListener(toRefresh);
		else if (toRefresh != null)
			toRefresh.refreshData();
	}

	@Override
	public void onServiceDisconnected(final ComponentName name)
	{
		if (toUpdateLocation != null)
			service.removeLocationListener(toUpdateLocation);
		if (refreshOnTimer)
			service.removeRefreshOnTimerListener(toRefresh);
		service = null;
	}

	public void setAuthToken(final String authToken)
	{
		service.setAuthToken(authToken);
	}
}
