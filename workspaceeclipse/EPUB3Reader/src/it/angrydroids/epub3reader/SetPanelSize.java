package it.angrydroids.epub3reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

public class SetPanelSize extends DialogFragment {

	protected SeekBar seekbar;
	protected float value = (float) 0.2;
	protected int sBv = 50;
	protected Context context;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout

		View view = inflater.inflate(R.layout.set_panel_size, null);

		final SharedPreferences preferences = ((MainActivity) getActivity())
				.getPreferences(Context.MODE_PRIVATE);

		sBv = preferences.getInt("seekBarValue", 50);
		seekbar = (SeekBar) view.findViewById(R.id.progressBar);
		seekbar.setProgress(sBv);

		builder.setTitle(getString(R.string.SetSizeTitle));
		builder.setView(view);

		// (inflater.inflate(R.layout.setsize, null))
		// Add action buttons
		builder.setPositiveButton(getString(R.string.OK),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						float actual = (float) seekbar.getProgress();
						value = actual / (float) seekbar.getMax();
						if (value <= 0.1)
							value = (float) 0.1;
						if (value >= 0.9)
							value = (float) 0.9;

						((MainActivity) getActivity()).changeViewsSize(value);
						SharedPreferences.Editor editor = preferences.edit();
						sBv = seekbar.getProgress();
						editor.putInt("seekBarValue", sBv);
						editor.commit();
					}
				});
		builder.setNegativeButton(getString(R.string.Cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		return builder.create();
	}

}
