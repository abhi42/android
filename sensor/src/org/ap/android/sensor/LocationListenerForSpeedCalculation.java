package org.ap.android.sensor;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Callback handler that calculates speed using location data delivered by sensors.
 * 
 * @author abhi
 * 
 */
public class LocationListenerForSpeedCalculation implements LocationListener {

	private MapOperations mapOps;
	private Location currentBestLocation;
	private MeasureSpeedUsingLocationSensorsActivity activity;
	private boolean showMap;

	private static final int TIME_INTERVAL = 1000 * 5; // 5 seconds
	private static final String TAG = LocationListenerForSpeedCalculation.class.getName();

	public LocationListenerForSpeedCalculation(
			MeasureSpeedUsingLocationSensorsActivity measureSpeedUsingLocationSensorsActivity, boolean showMap) {
		activity = measureSpeedUsingLocationSensorsActivity;
		this.showMap = showMap;
	}

	private MapOperations getOrCreateMapOperations() {
		if (mapOps == null) {
			mapOps = new MapOperations(activity, activity.getMapFragmentId());
		}
		return mapOps;
	}

	@Override
	public void onLocationChanged(Location newLocation) {
		if (isBetterLocation(newLocation, currentBestLocation)) {
			float speedInMetresPerSec = newLocation.getSpeed();
			double speedInKmph = 0.0;
			if (speedInMetresPerSec != 0.0) {
				// speed is available, convert it to kmph
				speedInKmph = convertSpeedToKmph(speedInMetresPerSec);
			}
			double calculateSpeedThroughAlgorithm = calculateSpeedThroughAlgorithm(newLocation, currentBestLocation);

			currentBestLocation = newLocation;

			// send out new speed information
			activity.setSpeed(speedInKmph);
			activity.setCalculatedSpeed(calculateSpeedThroughAlgorithm);
		}
		if (showMap && currentBestLocation != null) {
			getOrCreateMapOperations().setMarker(currentBestLocation);
		}
	}

	private double calculateSpeedThroughAlgorithm(Location newLocation, Location oldLocation) {
		long timeDelta = getTimeDelta(newLocation, oldLocation);
		if (timeDelta == 0) {
			return 0;
		}
		double timeInSecsDelta = timeDelta / 1000;
		float results[] = new float[3];
		Location.distanceBetween(newLocation.getLatitude(), newLocation.getLongitude(), oldLocation.getLatitude(),
				oldLocation.getLongitude(), results);
		double speedInMetresPerSec = results[0] / timeInSecsDelta;
		Log.i(TAG, "Calculated Speed (mps): " + speedInMetresPerSec + "(" + results[0] + "/" + timeInSecsDelta + ")");
		Log.i(TAG, "-----");
		return convertSpeedToKmph(speedInMetresPerSec);
	}

	private long getTimeDelta(Location newLocation, Location oldLocation) {
		long newTime = newLocation.getTime();
		long oldTime = newTime;
		if (oldLocation != null) {
			oldTime = oldLocation.getTime();
		}
		return newTime - oldTime;
	}

	private double convertSpeedToKmph(double speedInMetresPerSec) {
		return (speedInMetresPerSec * 3600) / 1000;
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TIME_INTERVAL;
		boolean isSignificantlyOlder = timeDelta < -TIME_INTERVAL;
		boolean isNewer = timeDelta > 0;

		// If it's been more than TIME_INTERVAL since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than TIME_INTERVAL older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
