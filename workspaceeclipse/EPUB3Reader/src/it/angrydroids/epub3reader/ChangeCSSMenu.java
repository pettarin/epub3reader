package it.angrydroids.epub3reader;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class ChangeCSSMenu extends DialogFragment {

	protected float value = (float) 0.2;
	protected Button[] buttons = new Button[5];
	protected Builder builder;
	protected Spinner spinColor;
	protected Spinner spinBack;
	protected Spinner spinFontStyle;
	protected Spinner spinAlignText;
	protected EditText editTextFontSize;
	protected EditText editTextLineH;
	protected Button defaultButton;
	protected EditText editTextTop;
	protected EditText editTextBottom;
	protected EditText editTextLeft;
	protected EditText editTextRight;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout

		View view = inflater.inflate(R.layout.change_css, null);
		spinColor = (Spinner) view.findViewById(R.id.spinnerColor);
		spinBack = (Spinner) view.findViewById(R.id.spinnerBack);
		spinFontStyle = (Spinner) view.findViewById(R.id.spinnerFontFamily);
		spinAlignText = (Spinner) view.findViewById(R.id.spinnerAlign);
		editTextFontSize = (EditText) view.findViewById(R.id.editText_Size);
		editTextLineH = (EditText) view.findViewById(R.id.editTextLH);
		defaultButton = (Button) view.findViewById(R.id.buttonDefault);
		editTextTop = (EditText) view.findViewById(R.id.editText1);
		editTextBottom = (EditText) view.findViewById(R.id.editText2);
		editTextLeft = (EditText) view.findViewById(R.id.editText3);
		editTextRight = (EditText) view.findViewById(R.id.editText4);

		builder.setTitle("Style");
		builder.setView(view);

		spinColor
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						switch ((int) id) {
						case 0:
							EpubReaderMain
									.setColor(getString(R.string.black_rgb));
							break;
						case 1:
							EpubReaderMain
									.setColor(getString(R.string.red_rgb));
							break;
						case 2:
							EpubReaderMain
									.setColor(getString(R.string.green_rgb));
							break;
						case 3:
							EpubReaderMain
									.setColor(getString(R.string.blue_rgb));
							break;
						case 4:
							EpubReaderMain
									.setColor(getString(R.string.white_rgb));
							break;
						default:
							break;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		spinBack.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch ((int) id) {
				case 0:
					EpubReaderMain.setBackColor(getString(R.string.white_rgb));
					break;
				case 1:
					EpubReaderMain.setBackColor(getString(R.string.red_rgb));
					break;
				case 2:
					EpubReaderMain.setBackColor(getString(R.string.green_rgb));
					break;
				case 3:
					EpubReaderMain.setBackColor(getString(R.string.blue_rgb));
					break;
				case 4:
					EpubReaderMain.setBackColor(getString(R.string.black_rgb));
					break;
				default:
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		spinFontStyle
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						switch ((int) id) {
						case 0:
							EpubReaderMain
									.setFontType(getString(R.string.Arial));
							break;
						case 1:
							EpubReaderMain
									.setFontType(getString(R.string.Serif));
							break;
						case 2:
							EpubReaderMain
									.setFontType(getString(R.string.Monospace));
							break;
						default:
							break;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		spinAlignText
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						switch ((int) id) {
						case 0:
							EpubReaderMain
									.setAlign(getString(R.string.Left_Align));
							break;
						case 1:
							EpubReaderMain
									.setAlign(getString(R.string.Center_Align));
							break;
						case 2:
							EpubReaderMain
									.setAlign(getString(R.string.Right_Align));
							break;
						default:
							break;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		defaultButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				EpubReaderMain.setColor("");
				EpubReaderMain.setBackColor("");
				EpubReaderMain.setFontType("");
				EpubReaderMain.setFontSize("");
				EpubReaderMain.setLineHeight("");
				EpubReaderMain.setAlign("");
				EpubReaderMain.setMargin("", "", "", "");
				((EpubReaderMain) getActivity()).setCSS();
				dismiss();
			}
		});

		builder.setPositiveButton(getString(R.string.OK),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EpubReaderMain.setFontSize(editTextFontSize.getText()
								.toString());
						EpubReaderMain.setLineHeight(editTextLineH.getText()
								.toString());
						EpubReaderMain.setMargin(editTextTop.getText()
								.toString(), editTextBottom.getText()
								.toString(), editTextLeft.getText().toString(),
								editTextRight.getText().toString());
						((EpubReaderMain) getActivity()).setCSS();
					}
				});
		builder.setNegativeButton(getString(R.string.Cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		return builder.create();
	}
}
