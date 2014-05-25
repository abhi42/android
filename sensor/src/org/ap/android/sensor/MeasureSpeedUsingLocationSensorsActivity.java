package org.ap.android.sensor;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.google.android.gms.maps.MapFragment;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Measures speed using location sensors.
 * 
 * @author abhi
 * 
 */
public class MeasureSpeedUsingLocationSensorsActivity extends Activity {

	private LocationManager locationMgr;
	private TextView speedValueText;
	private TextView calculatedSpeedValueText;
	private Button speedMeasurementBtn;
	private boolean isListenerRegistered = false;
	private LocationListenerForSpeedCalculation locationListener;

	private long timeInterval;
	private float minDistance;
	private MapFragment mMapFragment;
	private boolean showMap = false;
	private boolean wasSensorEnabledBeforeBeingPaused = false;

	private static NumberFormat DF = DecimalFormat.getInstance();

	private static final String BTN_START = "Start Measurement";
	private static final String BTN_STOP = "Stop Measurement";
	private static final String DEFAULT_SPEED = "0.0";

	private static final int GPS_ENABLEMENT_REQUEST_CODE = 42;
	private static final String TAG = MeasureSpeedUsingLocationSensorsActivity.class.getName();

	static {
		DF.setMinimumFractionDigits(2);
		DF.setMaximumFractionDigits(4);
		DF.setRoundingMode(RoundingMode.DOWN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "in onCreate() of " + getClass().getName());

		initLayout();

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.measure_speed_menu, menu);

		// in order to show the menu, return true.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.menu_measure_speed_settings:
			Intent intent = new Intent(getApplicationContext(), MeasureSpeedSettingsActivity.class);
			startActivity(intent);
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume() of " + getClass().getName());
		super.onResume();

		enableGpsIfNotEnabled();

		usePreferences();

		if (isListenerRegistered || wasSensorEnabledBeforeBeingPaused) {
			registerSensorListener();
			setButtonText(BTN_STOP);
		} else {
			setButtonText(BTN_START);
		}
	}

	private void createLocationManagerIfNecessary() {
		if (locationMgr == null) {
			locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		}
	}

	private void enableGpsIfNotEnabled() {
		if (!isGpsEnabled()) {
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.add(new EnableGpsFragment(), "EnableGPSDialogFragment");
			transaction.commit();
		}
	}

	private boolean isGpsEnabled() {
		createLocationManagerIfNecessary();
		return locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GPS_ENABLEMENT_REQUEST_CODE && !isGpsEnabled()) {
			finish();
		}
	}

	private void usePreferences() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String distanceStr = preferences.getString(MeasureSpeedSettingsFragment.DISTANCE_KEY, "");
		String timeStr = preferences.getString(MeasureSpeedSettingsFragment.TIME_INTERVAL_KEY, "");
		showMap = preferences.getBoolean(MeasureSpeedSettingsFragment.MAP_KEY, false);

		try {
			minDistance = Integer.parseInt(distanceStr);
		} catch (NumberFormatException e) {
			minDistance = 0; // TODO read this from the default value specified in the XML file
		}

		try {
			timeInterval = Long.parseLong(timeStr);
		} catch (NumberFormatException e) {
			timeInterval = 0; // TODO read this from the default value specified in the XML file
		}

		if (showMap) {
			addMap();
		} else if (mMapFragment != null) {
			// if map is being displayed, remove it
			getFragmentManager().beginTransaction().remove(mMapFragment).commit();
			mMapFragment = null;
		}

		Log.i(TAG, "Time interval: " + timeInterval);
		Log.i(TAG, "Min distance: " + minDistance);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "in onPause() of " + getClass().getName() + ", value of wasSensorEnabledBeforeBeingPaused: "
				+ wasSensorEnabledBeforeBeingPaused);
		if (isListenerRegistered) {
			wasSensorEnabledBeforeBeingPaused = true;
		} else {
			wasSensorEnabledBeforeBeingPaused = false;
		}
		unregisterSensorListener();
	}

	private void initLayout() {
		setContentView(R.layout.activity_measure_speed_use_location_sensors);

		speedValueText = (TextView) findViewById(R.id.speedValueUsingSensor);
		speedValueText.setText(DEFAULT_SPEED);

		calculatedSpeedValueText = (TextView) findViewById(R.id.calculatedSpeedValueUsingSensor);
		calculatedSpeedValueText.setText(DEFAULT_SPEED);

		speedMeasurementBtn = (Button) findViewById(R.id.btn_ToggleSpeedMeasurementUsingSensors);
		setButtonText(BTN_START);
	}

	private void addMap() {
		if (mMapFragment != null) {
			Log.i(TAG, "Map fragment has already been instantiated");
			// we do not wish to reset the map if it is already existing
			return;
		}
		Log.i(TAG, "Instantiating new Map fragment");
		mMapFragment = MapFragment.newInstance();
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.measure_speed_use_location_sensor_map_container, mMapFragment);
		fragmentTransaction.commit();
	}

	private void setButtonText(String text) {
		speedMeasurementBtn.setText(text);
	}

	public void onSubmitToggleSpeedMeasurement(View view) {
		if (R.id.btn_ToggleSpeedMeasurementUsingSensors != view.getId()) {
			return;
		}
		if (BTN_START.equals(speedMeasurementBtn.getText())) {
			if (!isListenerRegistered) {
				registerSensorListener();
			}
			setButtonText(BTN_STOP);
		} else if (BTN_STOP.equals(speedMeasurementBtn.getText())) {
			if (isListenerRegistered) {
				unregisterSensorListener();
			}
			setButtonText(BTN_START);
			speedValueText.setText("0.0");
			calculatedSpeedValueText.setText("0.0");
		}
	}

	private void unregisterSensorListener() {
		if (!isListenerRegistered) {
			return;
		}
		locationMgr.removeUpdates(locationListener);
		isListenerRegistered = false;
	}

	private void registerSensorListener() {
		wasSensorEnabledBeforeBeingPaused = true;
		if (isListenerRegistered) {
			return;
		}
		createLocationManagerIfNecessary();
		if (locationListener == null) {
			locationListener = new LocationListenerForSpeedCalculation(this, showMap);
		}
		locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeInterval, minDistance, locationListener);
		isListenerRegistered = true;
	}

	void setSpeed(double speed) {
		speedValueText.setText(DF.format(speed));
	}

	void setCalculatedSpeed(double speed) {
		calculatedSpeedValueText.setText(DF.format(speed));
	}

	int getMapFragmentId() {
		return mMapFragment.getId();
	}
}
