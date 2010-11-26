package edu.usc.csci588team02.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

	private NotificationManager mNotificationManager;
	private Context myContext;

	public NotificationUtility()
	{
	}

	public NotificationUtility(final Context context,
			final NotificationManager nm)
	{
		// Setup the home activity
		myContext = context;
		// Get the notification manager serivce.
		mNotificationManager = nm;
	}

	public void createSimpleNotification(final String message)
	{
		final Notification notification = new Notification(
				R.drawable.ic_green_square, message, System.currentTimeMillis());
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(myContext, message, "",
				makeNotificationIntent());
		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNotificationManager.notify(R.layout.status_bar_notifications,
				notification);
	}

	public void createSimpleNotification(final String message,
			final EventEntry ee, final long leaveInMinutes,
			final int notifyTimeInMin)
	{
		Log.d("NotificationUtility", "Creating Message: " + message);
		Notification notification = null;
		NotificationUtility.COLOR notifcationColor = NotificationUtility.COLOR.GREEN;
		if (leaveInMinutes < notifyTimeInMin * .33333)
			notifcationColor = NotificationUtility.COLOR.RED;
		else if (leaveInMinutes < notifyTimeInMin * .6666)
			notifcationColor = NotificationUtility.COLOR.ORANGE;
		switch (notifcationColor)
		{
			case RED:
				notification = new Notification(R.drawable.ic_red_arrow72,
						message, System.currentTimeMillis());
				break;
			case ORANGE:
				notification = new Notification(R.drawable.ic_orange_arrow72,
						message, System.currentTimeMillis());
				break;
			case GREEN:
				notification = new Notification(R.drawable.ic_green_arrow72,
						message, System.currentTimeMillis());
				break;
			default:
				notification = new Notification(R.drawable.ic_green_arrow72,
						message, System.currentTimeMillis());
				break;
		}
		final CharSequence time = android.text.format.DateFormat.format(
				"hh:mma", ee.when.startTime.value);
		// Set the info for the views that show in the notification panel.
		final String formattedTime = EventEntry
				.formatWhenToLeave(leaveInMinutes);
		notification
				.setLatestEventInfo(myContext, ee.title,
						"Leave "
								+ (leaveInMinutes > 0 ? "in " + formattedTime
										+ " - " : "Now -")
								+ ee.where.valueString + " @" + time,
						makeNotificationIntent());
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
				myContext, 0, new Intent(myContext, TabbedInterface.class)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
				PendingIntent.FLAG_UPDATE_CURRENT);
		return contentIntent;
	}
}
