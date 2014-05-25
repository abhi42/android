package org.ap.android.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by abhi on 14/12/13.
 */
public class MyAlarmBroadcastReceiver extends BroadcastReceiver {

	static final String NUM_OCCURENCES_KEY = "numOccurences";
	private int numOccurrences = AlarmActivity.DEFAULT_NUM_ALARM_OCCURRENCES;

	private static final String TAG = MyAlarmBroadcastReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("My Receiver", "received broadcast****************");

		if (intent.hasExtra(AlarmActivity.NUM_ALARM_OCCURRENCES)) {
			numOccurrences = intent.getIntExtra(AlarmActivity.NUM_ALARM_OCCURRENCES,
					AlarmActivity.DEFAULT_NUM_ALARM_OCCURRENCES);

			Log.i(TAG, "*****number of occurrences as specified in intent: " + numOccurrences);

			intent.removeExtra(AlarmActivity.NUM_ALARM_OCCURRENCES);
		}

		if (numOccurrences > 0) {
			handleBroadcast(context);
		}
	}

	private void handleBroadcast(Context context) {
		startActivity(context);
	}

	private void startActivity(Context context) {
		Intent intent = new Intent(context, AlarmNotificationActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(NUM_OCCURENCES_KEY, numOccurrences);
		context.startActivity(intent);
	}
}
