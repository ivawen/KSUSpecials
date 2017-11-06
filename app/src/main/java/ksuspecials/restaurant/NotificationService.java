package ksuspecials.restaurant;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

public class NotificationService extends Service {
	private NotificationManager mManager;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);

		this.getApplicationContext();
		mManager = (NotificationManager) this.getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		/*
		 * When the user taps the notification we have to show the Home Screen
		 * of our App, this job can be done with the help of the following
		 * Intent.
		 */
		Intent intent1 = new Intent(this.getApplicationContext(), Home.class);

		// Notification notification = new Notification(R.drawable.ic_launcher,
		// "book table", System.currentTimeMillis());
		Notification notification = new Notification();
		Context context = getApplicationContext();
		String notificationTitle = "Restaurant";
		String notificationText = getString(R.string.book);
		intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent1,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder builder = new Notification.Builder(context).setContentIntent(pendingNotificationIntent)
				.setSmallIcon(R.drawable.ic_launcher).setContentTitle(notificationTitle)
				.setContentText(notificationText);
		builder.setAutoCancel(true);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification = builder.build();
		// notification.setLatestEventInfo(context, notificationText,
		// notificationTitle, pendingNotificationIntent);
		// notification.flags = Notification.FLAG_AUTO_CANCEL;

		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		notification.sound = uri;
		mManager.notify(0, notification);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
