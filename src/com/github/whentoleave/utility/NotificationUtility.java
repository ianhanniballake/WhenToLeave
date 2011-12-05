package com.github.whentoleave.utility;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

import com.github.whentoleave.R;
import com.github.whentoleave.ui.MainActivity;

/**
 * Utility class to manage Notifications
 */
public class NotificationUtility
{
	/**
	 * Enum for the color of the Notification
	 */
	public enum COLOR {
		/**
		 * Green = Greater than 66% of Notify Time preference remaining
		 */
		GREEN, /**
		 * Orange = 33% - 66% of Notify Time preference remaining
		 */
		ORANGE, /**
		 * Red = <33% of Notify Time preference remaining
		 */
		RED
	}

	/**
	 * Logging tag
	 */
	private static final String TAG = "NotificationUtility";
	/**
	 * NotificationManager service
	 */
	private final NotificationManager mNotificationManager;
	/**
	 * Context to use for notifications and activity launching
	 */
	private final Context myContext;

	/**
	 * Basic constructor
	 * 
	 * @param context
	 *            Context to use for notifications and activity launching
	 * @param nm
	 *            NotificationManager service
	 */
	public NotificationUtility(final Context context,
			final NotificationManager nm)
	{
		myContext = context;
		mNotificationManager = nm;
	}

	/**
	 * Create a simple notification for the given message and Event, along with
	 * pre-computed leaveInMinutes and notifyTimeInMin
	 * 
	 * @param title
	 *            Title of the event
	 * @param startTime
	 *            Start time of the event
	 * @param location
	 *            String representation of the location of the event
	 * @param leaveInMinutes
	 *            how many minutes until user needs to leave for this event
	 * @param notifyTimeInMin
	 *            user preference on when they would like to be notified
	 */
	public void createSimpleNotification(final String title,
			final long startTime, final String location,
			final long leaveInMinutes, final int notifyTimeInMin)
	{
		Log.d(TAG, "Creating Message: " + title);
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
						title, System.currentTimeMillis());
				break;
			case ORANGE:
				notification = new Notification(R.drawable.ic_orange_arrow72,
						title, System.currentTimeMillis());
				break;
			case GREEN:
				notification = new Notification(R.drawable.ic_green_arrow72,
						title, System.currentTimeMillis());
				break;
			default:
				notification = new Notification(R.drawable.ic_green_arrow72,
						title, System.currentTimeMillis());
				break;
		}
		// Auto cancels the notification when clicked
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		final CharSequence time = DateFormat.format("hh:mma", new Date(
				startTime));
		// Set the info for the views that show in the notification panel.
		final long hoursToGo = Math.abs(leaveInMinutes) / 60;
		final long minutesToGo = Math.abs(leaveInMinutes) % 60;
		final StringBuffer formattedTime = new StringBuffer();
		if (hoursToGo > 0)
		{
			formattedTime.append(hoursToGo);
			formattedTime.append(":");
			if (minutesToGo < 10)
				formattedTime.append("0");
			formattedTime.append(minutesToGo);
			formattedTime.append("h");
		}
		else
		{
			formattedTime.append(minutesToGo);
			formattedTime.append("m");
		}
		notification.setLatestEventInfo(myContext, title,
				"Leave "
						+ (leaveInMinutes > 0 ? "in " + formattedTime + " - "
								: "Now -") + location + " @" + time,
				makeNotificationIntent());
		// Send the notification.
		mNotificationManager.notify(0, notification);
	}

	/**
	 * Creates the PendingIntent used to launch the application when the
	 * notification is clicked
	 * 
	 * @return PendingIntent to launch when the notification is clicked
	 */
	private PendingIntent makeNotificationIntent()
	{
		// The PendingIntent to launch our activity if the user selects this
		// notification. Note the use of FLAG_UPDATE_CURRENT so that if there
		// is already an active matching pending intent, we will update its
		// extras to be the ones passed in here.
		final PendingIntent contentIntent = PendingIntent.getActivity(
				myContext, 0, new Intent(myContext, MainActivity.class)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
				PendingIntent.FLAG_UPDATE_CURRENT);
		return contentIntent;
	}
}
