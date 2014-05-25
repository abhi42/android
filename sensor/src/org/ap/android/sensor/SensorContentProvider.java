package org.ap.android.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * This class provides data about the sensors present in the device in the form of {@link SensorDTO} objects.
 * 
 * @author abhi
 * 
 */
public class SensorContentProvider {

	private Activity activity;
	private List<SensorDTO> sensors;
	
	private static SensorContentProvider INSTANCE;

	private SensorContentProvider(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Returns a singleton instance of this class.
	 * 
	 * @return instance of this class
	 */
	public static synchronized SensorContentProvider getInstance(Activity activity) {
		if (INSTANCE == null) {
			INSTANCE = new SensorContentProvider(activity);
		}
		return INSTANCE;
	}
	
	public List<SensorDTO> getAvailableSensors() {
		if (sensors == null) {
			createDisplayableSensorList();
		}
		return Collections.unmodifiableList(sensors);
	}
	
	private void createDisplayableSensorList() {
		sensors = new ArrayList<SensorDTO>();
		List<Sensor> sensorList = obtainAvailableSensorsOnDevice();
		for (Sensor sensor : sensorList) {
			SensorDTO sensorDTO = new SensorDTO(sensor);
			sensors.add(sensorDTO);
		}
	}

	private SensorManager getSensorManager() {
		return (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
	}

	private List<Sensor> obtainAvailableSensorsOnDevice() {
		SensorManager sensorManager = getSensorManager();
		return sensorManager.getSensorList(Sensor.TYPE_ALL);
	}
}
