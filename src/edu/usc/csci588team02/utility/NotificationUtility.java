package edu.usc.csci588team02.utility;

import edu.usc.csci588team02.activity.TabbedInterface;
import edu.usc.csci588team02.model.EventEntry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import edu.usc.csci588team02.R;

/**
 * @author Stephen Barnes
 */
public class NotificationUtility
{
	private NotificationManager mNotificationManager;
	private TabbedInterface mainActivity;
	
	public enum COLOR { RED, ORANGE, GREEN };
	
	public NotificationUtility()
	{}
	
	public NotificationUtility(TabbedInterface activity, NotificationManager nm)
	{
		// Setup the home activity
		mainActivity = activity;
		// Get the notification manager serivce.
        mNotificationManager = nm;
        createSimpleNotification("Notification Utility Created");
	}
	
	public void createSimpleNotification(String message)
	{
		 Notification notification = new Notification(R.drawable.ic_green_square, message,
	                System.currentTimeMillis());

	     // Set the info for the views that show in the notification panel.
	     notification.setLatestEventInfo(mainActivity, "Event Title",
	                    "12:00 - Event Location", makeNotificationIntent(1)); 
	     				//might have to update this makeNotificationIntent id

	     // Send the notification.
	     // We use a layout id because it is a unique number.  We use it later to cancel.
	     mNotificationManager.notify(R.layout.status_bar_notifications, notification);
	}
	
	public void createSimpleNotification(String message, EventEntry ee, COLOR c )
	{
		Notification notification = null;
		
		//TODO: update icon to color coded arrow
		switch (c)
		{
			case RED:
				 notification = new Notification(R.drawable.ic_red_square, message,
			                System.currentTimeMillis());
				 break;
			case ORANGE:
				 notification = new Notification(R.drawable.ic_orange_square, message,
			                System.currentTimeMillis());
				 break;
			case GREEN:
				 notification = new Notification(R.drawable.ic_green_square, message,
			                System.currentTimeMillis());
				 break;
			default:
				 notification = new Notification(R.drawable.ic_green_square, message,
			                System.currentTimeMillis());
				 break;
		}

	     // Set the info for the views that show in the notification panel.
	     notification.setLatestEventInfo(mainActivity, ee.title,
	                    ee.when.startTime.toString() + " - " + ee.where.valueString
	                    , makeNotificationIntent(1)); 
	     				//might have to update this makeNotificationIntent id

	     // Send the notification.
	     // We use a layout id because it is a unique number.  We use it later to cancel.
	     mNotificationManager.notify(R.layout.status_bar_notifications, notification);
	}
	
    private PendingIntent makeNotificationIntent(int id) {
        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_UPDATE_CURRENT so that if there
        // is already an active matching pending intent, we will update its
        // extras to be the ones passed in here.
        PendingIntent contentIntent = PendingIntent.getActivity(mainActivity, 0,
                new Intent(mainActivity, TabbedInterface.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }
}
