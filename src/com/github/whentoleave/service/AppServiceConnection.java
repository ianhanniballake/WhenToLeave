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

/**
 * Serves as the primary connection to the AppService when an Activity or
 * Service wishes to bind to the AppService
 */
public class AppServiceConnection implements ServiceConnection
{
	/**
	 * Flag if we should register toRefresh for alarm timer callbacks
	 */
	private boolean refreshOnTimer = false;
	/**
	 * Underlying service
	 */
	private AppServiceBinder service = null;
	/**
	 * Refreshable instance to refresh upon connection and optionally register
	 * for alarm timer callbacks
	 */
	private Refreshable toRefresh = null;
	/**
	 * LocationAware instance to register for location updates
	 */
	private LocationAware toUpdateLocation = null;

	/**
	 * Constructor for a one time refresh
	 * 
	 * @param toRefreshOnConnected
	 *            Refreshable instance to refresh upon connection
	 */
	public AppServiceConnection(final Refreshable toRefreshOnConnected)
	{
		this(toRefreshOnConnected, null, false);
	}

	/**
	 * Constructor for a one time refresh of the given Refreshable instance and
	 * LocationAware listener to register for ongoing location updates
	 * 
	 * @param toRefreshOnConnected
	 *            Refreshable instance to refresh upon connection
	 * @param toUpdateLocation
	 *            LocationAware instance to register for location updates
	 */
	public AppServiceConnection(final Refreshable toRefreshOnConnected,
			final LocationAware toUpdateLocation)
	{
		this(toRefreshOnConnected, toUpdateLocation, false);
	}

	/**
	 * Constructor for a one time refresh of the given Refreshable instance and
	 * LocationAware listener to register for ongoing location updates.
	 * Optionally registers the Refreshable instance for alarm timer callbacks
	 * 
	 * @param toRefresh
	 *            Refreshable instance to refresh upon connection
	 * @param toUpdateLocation
	 *            LocationAware instance to register for location updates
	 * @param refreshOnTimer
	 *            whether toRefresh should be registered for alarm timer
	 *            callbacks
	 */
	public AppServiceConnection(final Refreshable toRefresh,
			final LocationAware toUpdateLocation, final boolean refreshOnTimer)
	{
		this.toRefresh = toRefresh;
		this.toUpdateLocation = toUpdateLocation;
		this.refreshOnTimer = refreshOnTimer;
	}

	/**
	 * Gets a list of all of the authenticated user's calendars. Assumes that
	 * the service is already authenticated
	 * 
	 * @return the list of all calendars the user has access to
	 * @throws IOException
	 *             on IO error
	 */
	public List<CalendarEntry> getCalendars() throws IOException
	{
		return service.getCalendars();
	}

	/**
	 * Gets a particular EventEntry given its URL. Assumes that the service is
	 * already authenticated
	 * 
	 * @param eventUrl
	 *            the URL of the EventEntry to return
	 * @return the EventEntry represented by the given URL
	 * @throws IOException
	 *             on IO error
	 */
	public EventEntry getEvent(final String eventUrl) throws IOException
	{
		return service.getEvent(eventUrl);
	}

	/**
	 * Gets all events in a given Date range. Assumes that the service is
	 * already authenticated
	 * 
	 * @param start
	 *            start date
	 * @param end
	 *            end date
	 * @return all events from all calendars in the Date range, ordered by start
	 *         time
	 * @throws IOException
	 *             on IO error
	 */
	public Set<EventEntry> getEvents(final Date start, final Date end)
			throws IOException
	{
		return service.getEvents(start, end);
	}

	/**
	 * Gets all events between now (new Date()) and the given end Date. Assumes
	 * that the service is already authenticated
	 * 
	 * @param end
	 *            end date
	 * @return all events from all calendars from now until the given end date,
	 *         ordered by start time
	 * @throws IOException
	 *             on IO error
	 */
	public Set<EventEntry> getEventsStartingNow(final Date end)
			throws IOException
	{
		return service.getEvents(new Date(), end);
	}

	/**
	 * Finds the next event across all calendars (chronologically) that has a
	 * location. Searches in an exponentially larger date range until it finds
	 * an event (first 1 day, then 2, then 4, etc). Assumes that the service is
	 * already authenticated
	 * 
	 * @return the next event that has a location, null if no events with a
	 *         location are found
	 * @throws IOException
	 *             on IO error
	 */
	public EventEntry getNextEventWithLocation() throws IOException
	{
		return service.getNextEventWithLocation();
	}

	/**
	 * Effectively logs the user out, invalidating their authentication token.
	 * Note that all queries done between now and future authentication will
	 * fail
	 */
	public void invalidateAuthToken()
	{
		service.invalidateAuthToken();
	}

	/**
	 * Getter for whether the service is authenticated
	 * 
	 * @return if the service is authenticated
	 */
	public boolean isAuthenticated()
	{
		return service.isAuthenticated();
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

	/**
	 * Authorizes the service with the given authToken
	 * 
	 * @param authToken
	 *            authToken used to authenticate any Google API queries
	 */
	public void setAuthToken(final String authToken)
	{
		service.setAuthToken(authToken);
	}
}
