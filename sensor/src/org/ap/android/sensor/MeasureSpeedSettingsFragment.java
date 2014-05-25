package org.ap.android.sensor;

import java.text.MessageFormat;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Provides settings for measuring speed.
 * 
 * @author abhi
 * 
 */
public class MeasureSpeedSettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	static final String TIME_INTERVAL_KEY = "pref_measure_speed_time_interval_setting";
	static final String DISTANCE_KEY = "pref_measure_speed_distance_setting";
	static final String MAP_KEY = "pref_measure_speed_show_map";

	private static final String TIME_INTERVAL_SUMARY_MSG = "Every {0} seconds";
	private static final String DISTANCE_INTERVAL_SUMARY_MSG = "Every {0} metres";
	private static final String FREQUENTLY = "As frequently as possible";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// load preferences from the XML file
		addPreferencesFromResource(R.xml.measure_speed_preferences);
		getActivity().getActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();

		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		setSummary(sharedPreferences, TIME_INTERVAL_KEY);
		setSummary(sharedPreferences, DISTANCE_KEY);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		setSummary(sharedPreferences, key);
	}

	private void setSummary(SharedPreferences sharedPreferences, String key) {
		if (TIME_INTERVAL_KEY.equals(key)) {
			setSummaryValue(sharedPreferences, key, 0, TIME_INTERVAL_SUMARY_MSG);
		} else if (DISTANCE_KEY.equals(key)) {
			setSummaryValue(sharedPreferences, key, 0, DISTANCE_INTERVAL_SUMARY_MSG);
		}
	}

	private void setSummaryValue(SharedPreferences sharedPreferences, String key, int specificValue,
			String summaryMessageTemplate) {
		Preference preference = findPreference(key);
		// choose a default value that cannot be selected in the preference.
		// this is to distinguish the case where the preference could not be found
		String strValue = sharedPreferences.getString(key, "-1");
		int value = Integer.parseInt(strValue);
		if (value == specificValue) {
			preference.setSummary(FREQUENTLY);
		} else {
			preference.setSummary(MessageFormat.format(summaryMessageTemplate, sharedPreferences.getString(key, "")));
		}
	}
}
