package it.angrydroids.epub3reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class SetSize extends DialogFragment {

	protected SeekBar seekbar;
	protected float value = (float) 0.2;
	protected Button button;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.setsize, null);
		seekbar = (SeekBar) view.findViewById(R.id.seekBar1);

		builder.setTitle(getString(R.string.SetSizeTitle))
				.setView(view)
				// (inflater.inflate(R.layout.setsize, null))
				// Add action buttons
				.setPositiveButton(getString(R.string.OK),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								float actual = (float) seekbar.getProgress();
								value = actual / (float) seekbar.getMax();
								if (value <= 0.0)
									value = (float) 0.1;
								if (value >= 0.9)
									value = (float) 0.9;
								((EpubReaderMain) getActivity())
										.changeViewsSize(value);
							}
						})
				.setNegativeButton(getString(R.string.Cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		return builder.create();
	}
}
