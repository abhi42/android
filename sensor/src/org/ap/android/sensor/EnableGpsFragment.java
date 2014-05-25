package org.ap.android.sensor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Displays a dialog to the user where she can choose to go to the activity for enabling GPs, a pre-requisite for using
 * this app.
 * 
 * @author abhi
 * 
 */
public class EnableGpsFragment extends DialogFragment {

	protected static final int GPS_ENABLEMENT_REQUEST_CODE = 43;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.enable_gps_dialog_title).setPositiveButton(R.string.enable_gps_dialog_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		});
		builder.setNegativeButton(R.string.enable_gps_dialog_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getActivity(), "Speed calculation will not function without GPS. Returning...", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getActivity(), UseSensorActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				getActivity().finish();
			}
		});
		builder.setMessage("Speed Calculation requires GPS to be enabled. Enable GPS?");
		
		return builder.create();
	}}
