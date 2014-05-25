package org.ap.android.alarm;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class AlarmActivity extends Activity {

	private EditText alarmStart;
	private EditText alarmInterval;
	private EditText alarmStop;
	private PendingIntent pi;

	static final String NUM_ALARM_OCCURRENCES = "numAlarmOccurrences";
	static final int DEFAULT_NUM_ALARM_OCCURRENCES = 5;

	private static final String TAG = AlarmActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);

		init();
	}

	private void init() {
		alarmStart = (EditText) findViewById(R.id.alarmStart);
		alarmInterval = (EditText) findViewById(R.id.alarmInterval);
		alarmStop = (EditText) findViewById(R.id.alarmStop);

		TextView currentTime = (TextView) findViewById(R.id.currentTime);
		Calendar instance = Calendar.getInstance();
		instance.setTimeInMillis(System.currentTimeMillis());
		int hr = instance.get(Calendar.HOUR_OF_DAY);
		int min = instance.get(Calendar.MINUTE);
		currentTime.setText(String.valueOf(hr) + ":" + String.valueOf(min));
	}

	public void handleSubmit(View view) {
		Calendar alarmStartCal = getAlarmStart();
		int alarmIntervalInt = getAlarmInterval();

		createPendingIntent();

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP,
				alarmStartCal.getTimeInMillis(), 1000 * 60 * alarmIntervalInt,
				pi);
		Log.i(TAG, "registered alarm****************");
		finish();
	}

	public void handleAlarmCancel(View view) {
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		if (pi == null) {
			createPendingIntent();
		}
		am.cancel(pi);
		Log.i(TAG, "alarm cancelled****************");
		finish();
	}

	private void createPendingIntent() {
		Intent bcIntent = new Intent(getApplicationContext(),
				MyAlarmBroadcastReceiver.class);

		bcIntent.putExtra(NUM_ALARM_OCCURRENCES, getNumAlarmOccurrences());

		bcIntent.setClass(getApplicationContext(),
				MyAlarmBroadcastReceiver.class);
		pi = PendingIntent
				.getBroadcast(getApplicationContext(), 0, bcIntent, 0);

		Log.i(TAG, "pending intent for alarm created****************");
	}
	
	static PendingIntent createPendingIntent(Activity activity) {
		Intent bcIntent = new Intent(activity, MyAlarmBroadcastReceiver.class);

		bcIntent.setClass(activity, MyAlarmBroadcastReceiver.class);
		return PendingIntent.getBroadcast(activity, 0, bcIntent, 0);
	}

	private int getNumAlarmOccurrences() {
		if (alarmStop != null) {
			String numAlarmOccurrencesStr = alarmStop.getText().toString();
			try {
				return Integer.valueOf(numAlarmOccurrencesStr);
			} catch (NumberFormatException e) {
				return DEFAULT_NUM_ALARM_OCCURRENCES;
			}
		}
		return DEFAULT_NUM_ALARM_OCCURRENCES;
	}

	private int getAlarmInterval() {
		final String alarmIntervalStr = alarmInterval.getText().toString();
		return Integer.valueOf(alarmIntervalStr);
	}

	private Calendar getAlarmStart() {
		// e.g. 2100
		final String alarmStartStr = alarmStart.getText().toString();
		final String hourStr = alarmStartStr.substring(0, 2);
		final String minuteStr = alarmStartStr.substring(2);
		final Long hour = Long.valueOf(hourStr);
		final Long minute = Long.valueOf(minuteStr);

		final Calendar instance = Calendar.getInstance();
		instance.setTimeInMillis(System.currentTimeMillis());
		instance.set(Calendar.HOUR_OF_DAY, hour.intValue());
		instance.set(Calendar.MINUTE, minute.intValue());

		Log.i(TAG, "Alarm start time: " + instance.getTime());

		return instance;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarm, menu);
		return true;
	}

}
