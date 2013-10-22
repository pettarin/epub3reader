package it.angrydroids.epub3reader;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class ChangeCSSMenu extends DialogFragment {

	protected float value = (float) 0.2;
	protected Button[] buttons = new Button[5];
	protected Builder builder;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout

		View view = inflater.inflate(R.layout.change_css, null);
		builder.setTitle("Style");
		builder.setView(view);
		builder.setPositiveButton(getString(R.string.OK),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		builder.setNegativeButton(getString(R.string.Cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		buttons[0] = (Button) view.findViewById(R.id.button_Black);
		buttons[0].setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EpubReaderMain.setColor(getString(R.string.black_rgb));
			}
		});
		buttons[1] = (Button) view.findViewById(R.id.button_Red);
		buttons[1].setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EpubReaderMain.setColor(getString(R.string.red_rgb));
			}
		});

		buttons[2] = (Button) view.findViewById(R.id.button_Blue);
		buttons[2].setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EpubReaderMain.setColor(getString(R.string.blue_rgb));
			}
		});
		buttons[3] = (Button) view.findViewById(R.id.button_Green);
		buttons[3].setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EpubReaderMain.setColor(getString(R.string.green_rgb));
			}
		});
		buttons[4] = (Button) view.findViewById(R.id.button_White);
		buttons[4].setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EpubReaderMain.setColor(getString(R.string.white_rgb));
			}
		});
		return builder.create();
	}
}
