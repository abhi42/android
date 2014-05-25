package org.ap.android.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * This activity provides the UI to use a particular sensor.
 * 
 * @author abhi
 * 
 */
public class UseSensorActivity extends Activity {

	private static final int REQUEST_CODE = 1;
	static final String X_OFFSET_KEY = "xOffsetKey";
	static final String KEY_SENSOR_ID = "sensorId";

	private double [] offset;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_use_sensor);
	}

	public void onSubmitCalibrateSensor(View view) {
		if (R.id.btn_Calibrate_Submit == view.getId()) {
			Intent calibrateSensorIntent = new Intent(this, SensorCalibrateActivity.class);
			startActivityForResult(calibrateSensorIntent, REQUEST_CODE);
		}
	}

	public void onSubmitCollectSensorData(View view) {
		if (R.id.btn_CollectData_Submit == view.getId()) {
//			Intent measureSpeedIntent = new Intent(this, MeasureSpeedActivity.class);
//			measureSpeedIntent.putExtra(X_OFFSET_KEY, offset);
			Intent measureSpeedIntent = new Intent(this, MeasureSpeedUsingLocationSensorsActivity.class);
			startActivity(measureSpeedIntent);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Activity.RESULT_OK == resultCode) {
			offset = data.getDoubleArrayExtra(X_OFFSET_KEY);
		}
	}
}
