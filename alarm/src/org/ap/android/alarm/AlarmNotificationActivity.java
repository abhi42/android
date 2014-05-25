package org.ap.android.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

public class AlarmNotificationActivity extends Activity {

	private Ringtone ringtone;
	private int numAlarmOccurrences = -1;
	private int originalVolume = NOT_INITIALISED_VOLUME;

	private static final String NUM_REMAINING_ALARM_OCCURRENCES = "numRemainingAlarmOccurrences";
	private static final int NON_EXISTENT = 0;
	private static final int NOT_INITIALISED_VOLUME = -1;
	private static final int STREAM = AudioManager.STREAM_RING;
	private static final double FRACTION_OF_MAX_VOLUME = 0.5;

	private static final int NOTIFICATION_ID = 42;
	private static final String TAG = AlarmNotificationActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		int numAlarmOccurrencesLeftRead = sharedPreferences.getInt(NUM_REMAINING_ALARM_OCCURRENCES, NON_EXISTENT);
		Log.i(TAG, "number of alarm occurrences left, as read from shared preferences: " + numAlarmOccurrencesLeftRead);
		if (numAlarmOccurrencesLeftRead > NON_EXISTENT) {
			numAlarmOccurrences = numAlarmOccurrencesLeftRead;
		} else {
			// first time, read it from the intent
			numAlarmOccurrences = getIntent().getIntExtra(MyAlarmBroadcastReceiver.NUM_OCCURENCES_KEY, 1);
			setSharedPreferences();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification_alarm);

		handleEvent();
	}

	private void emptySharedPreferences() {
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();
		edit.remove(NUM_REMAINING_ALARM_OCCURRENCES);
		edit.apply();
		Log.i(TAG, "This was the last occurrence of the alarm, removing its count from app's shared preferences");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (numAlarmOccurrences != NON_EXISTENT) {
			setSharedPreferences();
		}
	}

	private void setSharedPreferences() {
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		Editor edit = sharedPreferences.edit();
		edit.putInt(NUM_REMAINING_ALARM_OCCURRENCES, numAlarmOccurrences);
		edit.apply();
	}

	private void handleEvent() {
		numAlarmOccurrences--;
		playRingtone();
		displayNotification();
		if (numAlarmOccurrences <= NON_EXISTENT) {
			clearAlarm();
		}
	}

	private void clearAlarm() {
		emptySharedPreferences();
		cancelAlarm();
	}

	private void cancelAlarm() {
		Log.i(TAG, "Cancelling alarm");
		PendingIntent pi = AlarmActivity.createPendingIntent(this);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(pi);
	}

	private void playRingtone() {
		Ringtone ringtone = getOrCreateRingtone();
		setRingtoneVolumeIfRequired();
		ringtone.play();
	}

	private Ringtone getOrCreateRingtone() {
		if (ringtone == null) {
			ringtone = createRingtone();
		}
		return ringtone;
	}

	private Ringtone createRingtone() {
		Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		return RingtoneManager.getRingtone(this, defaultUri);
	}

	private void displayNotification() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle("Eye Drops");
		builder.setContentText("Take your eye drops now!");

		builder.setContentIntent(createActivityIntent());

		NotificationManager notMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notMgr.notify(NOTIFICATION_ID, builder.build());
	}

	private PendingIntent createActivityIntent() {
		// this activity will be invoked when the notification is clicked
		Intent resultIntent = new Intent(this, AlarmNotificationActivity.class);

		// build a stack builder which will return to the Home screen when one
		// navigates backwards from the activity.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// add the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(AlarmNotificationActivity.class);
		// add the intent that starts the activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		return resultPendingIntent;
	}

	public void handleStopAlarm(View view) {
		stopRingtone();
		closeNotification();
		finish();
	}

	private void closeNotification() {
		NotificationManager notMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notMgr.cancel(NOTIFICATION_ID);
	}

	private void stopRingtone() {
		if (ringtone != null && ringtone.isPlaying()) {
			ringtone.stop();
			Log.i(TAG, "Ringtone stopped");
		} else {
			Log.i(TAG, "Curiously, the ringtone is not playing!!!");
		}
		restoreRingtoneVolume();
	}

	private void setRingtoneVolumeIfRequired() {
		AudioManager audioMgr = getAudioManager();
		originalVolume = audioMgr.getStreamVolume(STREAM);
		int maxVolume = audioMgr.getStreamMaxVolume(STREAM);
		Log.d(TAG, "Current Volume: " + originalVolume + ". Max Volume: " + maxVolume);
		if (originalVolume < maxVolume * FRACTION_OF_MAX_VOLUME) {
			audioMgr.setStreamVolume(STREAM, (int) (maxVolume * FRACTION_OF_MAX_VOLUME), 0);
		}
	}

	private AudioManager getAudioManager() {
		return (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	private void restoreRingtoneVolume() {
		if (originalVolume != NOT_INITIALISED_VOLUME) {
			AudioManager audioManager = getAudioManager();
			audioManager.setStreamVolume(STREAM, originalVolume, 0);
			Log.d(TAG, "Restored Volume to " + originalVolume);
		}
	}
}
