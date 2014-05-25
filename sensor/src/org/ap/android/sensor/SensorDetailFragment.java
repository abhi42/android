package org.ap.android.sensor;

import java.util.List;

import org.ap.android.sensor.dummy.DummyContent;

import android.content.Intent;
import android.hardware.Sensor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A fragment representing a single Sensor detail screen. This fragment is either contained in a
 * {@link SensorListActivity} in two-pane mode (on tablets) or a {@link SensorDetailActivity} on handsets.
 */
public class SensorDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	
	private static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * The dummy content this fragment is presenting.
	 */
	private DummyContent.DummyItem mItem;
	private Sensor sensor;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation
	 * changes).
	 */
	public SensorDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
//			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
			obtainSelectedSensor();
		}
	}
	
	private void obtainSelectedSensor() {
		sensor = getSensorFromSelection();
	}

	private Sensor getSensorFromSelection() {
		String sensorId = getArguments().getString(ARG_ITEM_ID);
		if (sensorId == null) {
			return null;
		}
		Integer offset = null;
		try {
			offset = Integer.valueOf(sensorId);
		} catch (NumberFormatException e) {
			return null;
		}
		return getSensorFromAvailableSensors(offset);
	}

	private Sensor getSensorFromAvailableSensors(int offset) {
		SensorContentProvider sensorContentProvider = SensorContentProvider.getInstance(getActivity());
		List<SensorDTO> availableSensors = sensorContentProvider.getAvailableSensors();
		SensorDTO sensorDTO = availableSensors.get(offset);
		return sensorDTO.getSensor();
	}
	
	private String getDisplayableText() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("NAME: ");
		builder.append(sensor.getName());
		builder.append(NEWLINE);
		
		builder.append("VENDOR: ");
		builder.append(sensor.getVendor());
		builder.append(NEWLINE);
		
		builder.append("TYPE: ");
		builder.append(sensor.getType());
		builder.append(NEWLINE);
		
		builder.append("VERSION: ");
		builder.append(sensor.getVersion());
		builder.append(NEWLINE);
		
		builder.append("POWER CONSUMPTION: ");
		builder.append(sensor.getPower());
		builder.append(NEWLINE);
		
		builder.append("MAX RANGE: ");
		builder.append(sensor.getMaximumRange());
		builder.append(NEWLINE);
		
		builder.append("RESOLUTION: ");
		builder.append(sensor.getResolution());
		builder.append(NEWLINE);
		
		return builder.toString();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_sensor_detail, container, false);

		// Show the dummy content as text in a TextView.
//		if (mItem != null) {
//			((TextView) rootView.findViewById(R.id.sensor_detail)).setText(mItem.content);
//		}
		if (sensor != null) {
			Button b = (Button) rootView.findViewById(R.id.sensor_detail_button);
			b.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
//					Toast.makeText(getActivity(), "Not yet implemented", Toast.LENGTH_LONG).show();
					
					Intent useSensorIntent = new Intent(getActivity(), UseSensorActivity.class);
					useSensorIntent.putExtra(UseSensorActivity.KEY_SENSOR_ID, "");
					startActivity(useSensorIntent);
				}
			});
			((TextView) rootView.findViewById(R.id.sensor_detail_text)).setText(getDisplayableText());
		}

		return rootView;
	}
}
