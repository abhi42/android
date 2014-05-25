package org.ap.android.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

/**
 * Displays preference settings for measuring speed.
 * 
 * @author abhi
 * 
 */
public class MeasureSpeedSettingsActivity extends Activity {

	private static final String TAG = MeasureSpeedSettingsActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new MeasureSpeedSettingsFragment())
				.addToBackStack(null).commit();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.i(TAG, "In onStop() of " + getClass().getName());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case android.R.id.home:
			// this is called when the home (up) button is pressed in the action bar
			Log.i(TAG, "About to start " + MeasureSpeedUsingLocationSensorsActivity.class.getName()
					+ " activity with flags");
			Intent intent = new Intent(this, MeasureSpeedUsingLocationSensorsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			this.finish();
			return true;
		}
		return true;
	}
}
