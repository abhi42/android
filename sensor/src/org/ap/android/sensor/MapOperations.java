package org.ap.android.sensor;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * This class handles map operations.
 * 
 * @author abhi
 * 
 */
public class MapOperations {

	private Activity activity;
	private GoogleMap map;
	private Marker marker;

	private static final String TAG = MapOperations.class.getName();
	private Polyline path;
	private List<LatLng> pathPoints = new ArrayList<LatLng>();

	private static final int INTERVAL = 3;

	/**
	 * Default constructor.
	 * 
	 * @param activity
	 *            the activity that contains the map
	 */
	public MapOperations(Activity activity, int mapFragmentId) {
		this.activity = activity;
		MapFragment mapFragment = (MapFragment) activity.getFragmentManager().findFragmentById(mapFragmentId);
		if (mapFragment == null) {
			Log.w(TAG, "Map fragment not found...");
			return;
		}
		map = mapFragment.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		path = map.addPolyline(new PolylineOptions().width(25).color(Color.BLUE).geodesic(true));
	}

	public void setMarker(Location location) {
		Log.i(TAG, "latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude());
		if (marker != null) {
			marker.setVisible(false);
		}
		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		marker = map.addMarker(new MarkerOptions().position(latLng).title("My Location"));
		pathPoints.add(latLng);
		if (pathPoints.size() >= INTERVAL) {
			displayPath();
		}
	}

	private void displayPath() {
		path.setPoints(pathPoints);
	}

}
