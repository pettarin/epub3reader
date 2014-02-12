/*
The MIT License (MIT)

Copyright (c) 2013, V. Giacometti, M. Giuriato, B. Petrantuono

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package it.angrydroids.epub3reader;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

// Abstract fragment that represents a general panel containing only the closing button
public abstract class SplitPanel extends Fragment {

	private RelativeLayout generalLayout;
	protected int index;
	protected RelativeLayout layout;
	protected Button closeButton;
	protected EpubNavigator navigator;
	protected int screenWidth;
	protected int screenHeight;
	protected float weight = 0.5f; // weight of the generalLayout
	protected boolean created; // tells whether the fragment has been created

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		navigator = ((MainActivity) getActivity()).navigator;
		View v = inflater.inflate(R.layout.activity_split_panel, container,
				false);
		created = false;
		return v;
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		created = true;
		super.onActivityCreated(saved);
		generalLayout = (RelativeLayout) getView().findViewById(
				R.id.GeneralLayout);
		layout = (RelativeLayout) getView().findViewById(R.id.Content);
		closeButton = (Button) getView().findViewById(R.id.CloseButton);

		// ----- get activity screen size
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		// -----

		changeWeight(weight);

		// ----- VIEW CLOSING
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeView();
			}
		});
	}

	protected void closeView() {
		navigator.closeView(index);
	}

	// change the weight of the general layout
	public void changeWeight(float value) {
		weight = value;
		if (created) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, value);
			generalLayout.setLayoutParams(params);
		}
	}

	public float getWeight() {
		return weight;
	}

	public void setKey(int value) {
		index = value;
	}

	public void errorMessage(String message) {
		((MainActivity) getActivity()).errorMessage(message);
	}

	public void saveState(Editor editor) {
		editor.putFloat("weight" + index, weight);
	}

	public void loadState(SharedPreferences preferences) {
		changeWeight(preferences.getFloat("weight" + index, 0.5f));
	}
}
