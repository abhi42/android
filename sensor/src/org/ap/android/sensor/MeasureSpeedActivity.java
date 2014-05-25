package org.ap.android.sensor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that displays the speed along X axis.
 * 
 * @author abhi
 * 
 */
public class MeasureSpeedActivity extends Activity implements SensorEventListener {

	private SensorManager sensorManager;
	private Sensor sensor;
	private Display mDisplay;
	private SpeedCalulator speedCalc;
	private TextView speedXDisplayTxt;
	private TextView speedYDisplayTxt;
	private TextView speedZDisplayTxt;
	private Button speedMeasurementBtn;
	private boolean listenerRegistered;
	private double[] offset;

	private static final String BTN_START = "Start";
	private static final String BTN_STOP = "Stop";
	private static final String TAG = MeasureSpeedActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		if (sensor == null) {
			Toast.makeText(getApplicationContext(), "Sensor not present.", Toast.LENGTH_LONG).show();
			finish();
		}

		initLayout();

		offset = getIntent().getDoubleArrayExtra(UseSensorActivity.X_OFFSET_KEY);
		mDisplay = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		speedCalc = new SpeedCalulator(offset);
	}

	public void onSubmitToggleSpeedMeasurement(View view) {
		if (R.id.btn_ToggleSpeedMeasurement != view.getId()) {
			return;
		}
		if (BTN_START.equals(speedMeasurementBtn.getText())) {
			if (!listenerRegistered) {
				registerSensorListener();
			}
			setButtonText(BTN_STOP);
		} else if (BTN_STOP.equals(speedMeasurementBtn.getText())) {
			if (listenerRegistered) {
				unregisterSensorListener();
			}
			setButtonText(BTN_START);
			speedCalc.init(offset);
			speedXDisplayTxt.setText("0.0");
		}
	}

	private void initLayout() {
		setContentView(R.layout.activity_measure_speed);
		speedXDisplayTxt = (TextView) findViewById(R.id.speedDisplayX);
		speedYDisplayTxt = (TextView) findViewById(R.id.speedDisplayY);
		speedZDisplayTxt = (TextView) findViewById(R.id.speedDisplayZ);
		speedMeasurementBtn = (Button) findViewById(R.id.btn_ToggleSpeedMeasurement);
		setButtonText(BTN_START);
	}

	private void setButtonText(String text) {
		speedMeasurementBtn.setText(text);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setButtonText(BTN_STOP);
		registerSensorListener();
	}

	private void registerSensorListener() {
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		listenerRegistered = true;
	}

	private void unregisterSensorListener() {
		sensorManager.unregisterListener(this);
		listenerRegistered = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterSensorListener();
		setButtonText(BTN_START);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) {
			return;
		}

		// switch (mDisplay.getRotation()) {
		// case Surface.ROTATION_0:
		// sensorDataXAxis.add(event.values[0]);
		// break;
		// case Surface.ROTATION_90:
		// sensorDataXAxis.add(-event.values[1]);
		// break;
		// case Surface.ROTATION_180:
		// sensorDataXAxis.add(-event.values[0]);
		// break;
		// case Surface.ROTATION_270:
		// sensorDataXAxis.add(event.values[1]);
		// break;
		// }

		// sensorDataXAxis.add(event.values[0]);
		// sensorTimeStamp.add(event.timestamp);

		// double speed = speedCalc.calculate(event.values[0], event.values[1],
		// event.values[2], event.timestamp);
		Log.i(TAG, "From sensor: ax: " + event.values[0] + ", ay: " + event.values[1] + ", az: " + event.values[2]);

		speedCalc.calculationBasedOnIntegration(event.values[0], event.values[1], event.values[2], event.timestamp);

		speedXDisplayTxt.setText(String.format("%.2f", speedCalc.velocityX));
		speedYDisplayTxt.setText(String.format("%.2f", speedCalc.velocityY));
		speedZDisplayTxt.setText(String.format("%.2f", speedCalc.velocityZ));
	}

	private class SpeedCalulator {

		private double prevVelocity;
		private long prevTimestamp;

		private int countNoAccX = 0;
		private int countNoAccY = 0;
		private int countNoAccZ = 0;

		private double velocityX0 = 0;
		private double velocityY0 = 0;
		private double velocityZ0 = 0;

		private double velocityX = 0;
		private double velocityY = 0;
		private double velocityZ = 0;

		private double accX0 = 0;
		private double accY0 = 0;
		private double accZ0 = 0;

		private double accX = 0;
		private double accY = 0;
		private double accZ = 0;

		private double xOffset;
		private double yOffset;
		private double zOffset;

		private int sampleCount = 0;

		private static final int SAMPLE_COUNT = 64;
		private static final double POSITIVE_DISCRIMINATION = 3;
		private static final double NEGATIVE_DISCRIMINATION = -3;

		public SpeedCalulator(double[] offset) {

			init(offset);
		}

		private void init(double[] offset) {
			prevVelocity = 0;
			prevTimestamp = 0;
			xOffset = offset[0];
			yOffset = offset[1];
			zOffset = offset[2];
			Log.i(TAG, "xOffset: " + xOffset + ", yOffset: " + yOffset + ", zOffset: " + zOffset);
		}

		private double calculate(float accX, float accY, float accZ, long timestamp) {
			if (prevTimestamp != 0) {
				double deltaTimeInSecs = ((double) (timestamp - prevTimestamp)) / 1000000000.0;

				double acc = Math.sqrt(Math.pow(accX, 2) + Math.pow(accY, 2) + Math.pow(accZ, 2));

				// v = u + at
				prevVelocity = prevVelocity + (acc * deltaTimeInSecs);
				Log.i(TAG, "Acc: " + acc + "; Delta Timestamp (secs): " + deltaTimeInSecs + "; velocity (m/sec): "
						+ prevVelocity);
			}
			prevTimestamp = timestamp;

			return prevVelocity;
		}

		private void doMovementStoppedCheck() {
			if (accX == 0) {
				countNoAccX++;
			}
			if (countNoAccX >= 25) {
				velocityX = 0;
			}

			if (accY == 0) {
				countNoAccY++;
			}
			if (countNoAccY >= 25) {
				velocityY = 0;
			}

			if (accZ == 0) {
				countNoAccZ++;
			}
			if (countNoAccZ >= 25) {
				velocityZ = 0;
			}
		}

		private void calculationBasedOnIntegration(float sampleAccX, float sampleAccY, float sampleAccZ, long timestamp) {
			if (sampleCount < SAMPLE_COUNT) {
				accX += accX + sampleAccX;
				accY += accY + sampleAccY;
				accZ += accZ + sampleAccZ;

				sampleCount++;
				return;
			}

			Log.i(TAG, "About to start acc calculation after retrieving " + SAMPLE_COUNT + " samples");

			// calculate the average acceleration
			accX = accX / SAMPLE_COUNT;
			accY = accY / SAMPLE_COUNT;
			accZ = accZ / SAMPLE_COUNT;

			// account for discrepancies found in calibration
			accX = accX - xOffset;
			accY = accY - yOffset;
			accZ = accZ - zOffset;

			// apply discrimination window
			if (accX < POSITIVE_DISCRIMINATION && accX > NEGATIVE_DISCRIMINATION) {
				accX = 0;
			}
			if (accY < POSITIVE_DISCRIMINATION && accY > NEGATIVE_DISCRIMINATION) {
				accY = 0;
			}
			if (accZ < POSITIVE_DISCRIMINATION && accZ > NEGATIVE_DISCRIMINATION) {
				accZ = 0;
			}

			// calculate velocity
			velocityX = velocityX0 + accX0 + ((accX - accX0) / 2);
			velocityY = velocityY0 + accY0 + ((accY - accY0) / 2);
			velocityZ = velocityZ0 + accZ0 + ((accZ - accZ0) / 2);

			accX0 = accX;
			accY0 = accY;
			accZ0 = accZ;

			velocityX0 = velocityX;
			velocityY0 = velocityY;
			velocityZ0 = velocityZ;

			doMovementStoppedCheck();
			sampleCount = 0;
			Log.i(TAG, "vx: " + velocityX + ", vy: " + velocityY + ", vz: " + velocityZ);
		}
	}

}
