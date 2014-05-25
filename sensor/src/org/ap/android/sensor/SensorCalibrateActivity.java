package org.ap.android.sensor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * This activity calibrates the sensor before initial use.
 * 
 * @author abhi
 * 
 */
public class SensorCalibrateActivity extends Activity implements SensorEventListener {

	private static final int NUM_SAMPLES = 100;
	private static final int NUM_STABLE_SAMPLES = NUM_SAMPLES / 2;
	private static final float THRESHHOLD_VARIANCE = 0.01f;

	private static final String TAG = SensorCalibrateActivity.class.getName();

	private SensorManager sensorManager;
	private Sensor sensor;
	private int count;
	private float[][] sensorData;
	private long[] sensorDataTimestamps;
	private double[] offset;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibrate_sensor);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		if (sensor == null) {
			Toast.makeText(getApplicationContext(), "Sensor not present.", Toast.LENGTH_LONG).show();
			handleFinish(Activity.RESULT_CANCELED);
		}
	}

	private void init() {
		Log.i(TAG, "Min sensor delay (microseconds): " + sensor.getMinDelay());
		count = 0;
		sensorData = new float[3][NUM_SAMPLES];
		sensorDataTimestamps = new long[NUM_SAMPLES];
		offset = new double[3];
	}

	@Override
	protected void onResume() {
		super.onResume();
		init();
		sensorManager.registerListener(this, sensor, 2000000);
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (count >= NUM_SAMPLES) {
			sensorManager.unregisterListener(this);
			displayCalibrationResults();
			return;
		}
		sensorData[0][count] = event.values[0];
		sensorData[1][count] = event.values[1];
		sensorData[2][count] = event.values[2];
		sensorDataTimestamps[count] = event.timestamp;
		Log.i(TAG, "count: " + count + "; timestamp:" + event.timestamp + "; x:" + event.values[0] + "; y:"
				+ event.values[1] + "; z:" + event.values[2]);
		count++;
	}

	private void displayCalibrationResults() {
		if (isCalibrationStable()) {
			Toast.makeText(getApplicationContext(), "Calibrated successfully", Toast.LENGTH_LONG).show();
			handleFinish(Activity.RESULT_OK);
		} else {
			Toast.makeText(getApplicationContext(),
					"Too much movement. Please keep the gadget on a flat surface and try again.", Toast.LENGTH_LONG)
					.show();
			handleFinish(Activity.RESULT_CANCELED);
		}
	}

	private void handleFinish(int resultCode) {
		if (Activity.RESULT_OK == resultCode) {
			setResult(resultCode, new Intent().putExtra(UseSensorActivity.X_OFFSET_KEY, offset));
		}
		finish();
	}

	private boolean isCalibrationStable() {
		// only interested in the X-axis
		if (isCalibrationStableForAxis(sensorData[0])) {
			// TODO do this in the above method itself
			offset[0] = obtainOffsets(sensorData[0]);
			offset[1] = obtainOffsets(sensorData[1]);
			offset[2] = obtainOffsets(sensorData[2]);
			Log.i(TAG, "X axis offset: " + offset[0] + "Y axis offset: " + offset[1] + "Z axis offset: " + offset[2]);
			return true;
		}
		return false;
	}

	private boolean isCalibrationStableForAxis(float[] coordSample) {
		float prev = 0.0f;
		float max = 0.0f;
		float min = 0.0f;
		int stabilityCount = 0;
		for (float value : coordSample) {
			float diff = value - prev;
			if (Math.abs(diff) <= THRESHHOLD_VARIANCE) {
				stabilityCount++;
			}
			if (value < min) {
				min = value;
			}
			if (value > max) {
				max = value;
			}
			prev = value;
		}
		Log.i(TAG, "Number of samples: " + coordSample.length + "; Number of stable values: " + stabilityCount
				+ "; Required number of stable samples: " + NUM_STABLE_SAMPLES + "; Threshhold value for stability: "
				+ THRESHHOLD_VARIANCE + "; Min Value: " + min + "; Max value: " + max);
		if (stabilityCount >= NUM_STABLE_SAMPLES) {
			return true;
		}

		return false;
	}

	private double obtainOffsets(float[] sample) {
		double sum = 0.0f;
		for (float value : sample) {
			sum += value;
		}
		return sum / sample.length;
	}
}
