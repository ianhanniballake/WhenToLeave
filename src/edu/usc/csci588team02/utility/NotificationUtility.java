package edu.usc.csci588team02.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.activity.TabbedInterface;
import edu.usc.csci588team02.model.EventEntry;

/**
 * @author Stephen Barnes
 */
public class NotificationUtility
{
	public enum COLOR {
		GREEN, ORANGE, RED
	}

	private TabbedInterface mainActivity;
	private NotificationManager mNotificationManager;

	public NotificationUtility()
	{
	}

	public NotificationUtility(final TabbedInterface activity,
			final NotificationManager nm)
	{
		// Setup the home activity
		mainActivity = activity;
		// Get the notification manager serivce.
		mNotificationManager = nm;
	}

	public void createSimpleNotification(final String message)
	{
		final Notification notification = new Notification(
				R.drawable.ic_green_square, message, System.currentTimeMillis());
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(mainActivity, message, "",
				makeNotificationIntent());
		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNotificationManager.notify(R.layout.status_bar_notifications,
				notification);
	}

	public void createSimpleNotification(final String message,
			final EventEntry ee, final COLOR c)
	{
		Notification notification = null;
		// TODO: update icon to color coded arrow
		switch (c)
		{
			case RED:
				notification = new Notification(R.drawable.ic_red_square,
						message, System.currentTimeMillis());
				break;
			case ORANGE:
				notification = new Notification(R.drawable.ic_orange_square,
						message, System.currentTimeMillis());
				break;
			case GREEN:
				notification = new Notification(R.drawable.ic_green_square,
						message, System.currentTimeMillis());
				break;
			default:
				notification = new Notification(R.drawable.ic_green_square,
						message, System.currentTimeMillis());
				break;
		}
		final CharSequence time = android.text.format.DateFormat.format(
				"hh:mma", ee.when.startTime.value);
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(mainActivity, ee.title, time + " - "
				+ ee.where.valueString, makeNotificationIntent());
		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNotificationManager.notify(R.layout.status_bar_notifications,
				notification);
	}

	private PendingIntent makeNotificationIntent()
	{
		// The PendingIntent to launch our activity if the user selects this
		// notification. Note the use of FLAG_UPDATE_CURRENT so that if there
		// is already an active matching pending intent, we will update its
		// extras to be the ones passed in here.
		final PendingIntent contentIntent = PendingIntent.getActivity(
				mainActivity, 0,
				new Intent(mainActivity, TabbedInterface.class)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
				PendingIntent.FLAG_UPDATE_CURRENT);
		return contentIntent;
	}
}
