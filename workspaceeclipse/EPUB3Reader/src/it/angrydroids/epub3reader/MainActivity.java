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

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected EpubNavigator navigator;
	protected int bookSelector;
	protected int panelCount;
	protected String[] settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		navigator = new EpubNavigator(2, this);

		panelCount = 0;
		settings = new String[8];

		// LOADSTATE
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		loadState(preferences);
		navigator.loadViews(preferences);
		if (panelCount == 0) {
			bookSelector = 0;
			Intent goToChooser = new Intent(this, FileChooser.class);
			startActivityForResult(goToChooser, 0);
		}
	}

	protected void onResume() {
		super.onResume();
		if (panelCount == 0) {
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			navigator.loadViews(preferences);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Editor editor = preferences.edit();
		saveState(editor);
		editor.commit();
	}

	// load the selected book
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (panelCount == 0) {
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			navigator.loadViews(preferences);
		}

		if (resultCode == Activity.RESULT_OK) {
			String path = data.getStringExtra(getString(R.string.bpath));
			navigator.openBook(path, bookSelector);
		}
	}

	// ---- Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (navigator.isParallelTextOn() == false
				&& navigator.exactlyOneBookOpen() == false) {
			menu.findItem(R.id.meta1).setVisible(true);
			menu.findItem(R.id.meta2).setVisible(true);
			menu.findItem(R.id.toc1).setVisible(true);
			menu.findItem(R.id.toc2).setVisible(true);
			menu.findItem(R.id.FirstFront).setVisible(true);
			menu.findItem(R.id.SecondFront).setVisible(true);
		}

		if (navigator.exactlyOneBookOpen() == false) {
			menu.findItem(R.id.Synchronize).setVisible(true);
			menu.findItem(R.id.Align).setVisible(true);
			// menu.findItem(R.id.SyncScroll).setVisible(true);
			menu.findItem(R.id.StyleBook1).setVisible(true);
			menu.findItem(R.id.StyleBook2).setVisible(true);
			menu.findItem(R.id.firstAudio).setVisible(true);
			menu.findItem(R.id.secondAudio).setVisible(true);
		}

		if (navigator.exactlyOneBookOpen() == true
				|| navigator.isParallelTextOn() == true) {
			menu.findItem(R.id.meta1).setVisible(false);
			menu.findItem(R.id.meta2).setVisible(false);
			menu.findItem(R.id.toc1).setVisible(false);
			menu.findItem(R.id.toc2).setVisible(false);
			menu.findItem(R.id.FirstFront).setVisible(false);
			menu.findItem(R.id.SecondFront).setVisible(false);
		}

		if (navigator.exactlyOneBookOpen() == true) {
			menu.findItem(R.id.Synchronize).setVisible(false);
			menu.findItem(R.id.Align).setVisible(false);
			menu.findItem(R.id.SyncScroll).setVisible(false);
			menu.findItem(R.id.StyleBook1).setVisible(false);
			menu.findItem(R.id.StyleBook2).setVisible(false);
			menu.findItem(R.id.firstAudio).setVisible(false);
			menu.findItem(R.id.secondAudio).setVisible(false);
		}

		// if there is only one view, option "changeSizes" is not visualized
		if (panelCount == 1)
			menu.findItem(R.id.changeSize).setVisible(false);
		else
			menu.findItem(R.id.changeSize).setVisible(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.FirstEPUB:
			bookSelector = 0;
			Intent goToChooser1 = new Intent(this, FileChooser.class);
			goToChooser1.putExtra(getString(R.string.second),
					getString(R.string.time));
			startActivityForResult(goToChooser1, 0);
			return true;

		case R.id.SecondEPUB:
			bookSelector = 1;
			Intent goToChooser2 = new Intent(this, FileChooser.class);
			goToChooser2.putExtra(getString(R.string.second),
					getString(R.string.time));
			startActivityForResult(goToChooser2, 0);
			return true;

		case R.id.Front:
			if (navigator.exactlyOneBookOpen() == true
					|| navigator.isParallelTextOn() == true)
				chooseLanguage(0);
			return true;

		case R.id.FirstFront:
			chooseLanguage(0);
			return true;

		case R.id.SecondFront:
			if (navigator.exactlyOneBookOpen() == false)
				chooseLanguage(1);
			else
				errorMessage(getString(R.string.error_onlyOneBookOpen));
			return true;

		case R.id.PconS:
			try {
				boolean yes = navigator.synchronizeView(1, 0);
				if (!yes) {
					errorMessage(getString(R.string.error_onlyOneBookOpen));
				}
			} catch (Exception e) {
				errorMessage(getString(R.string.error_cannotSynchronize));
			}
			return true;

		case R.id.SconP:
			try {
				boolean ok = navigator.synchronizeView(0, 1);
				if (!ok) {
					errorMessage(getString(R.string.error_onlyOneBookOpen));
				}
			} catch (Exception e) {
				errorMessage(getString(R.string.error_cannotSynchronize));
			}
			return true;

		case R.id.Synchronize:

			boolean sync = navigator.flipSynchronizedReadingActive();
			if (!sync) {
				errorMessage(getString(R.string.error_onlyOneBookOpen));
			}
			return true;

		case R.id.Metadata:
			if (navigator.exactlyOneBookOpen() == true
					|| navigator.isParallelTextOn() == true) {
				navigator.displayMetadata(0);
			} else {
			}
			return true;

		case R.id.meta1:
			if (!navigator.displayMetadata(0))
				errorMessage(getString(R.string.error_metadataNotFound));
			return true;

		case R.id.meta2:
			if (!navigator.displayMetadata(1))
				errorMessage(getString(R.string.error_metadataNotFound));
			return true;

		case R.id.tableOfContents:
			if (navigator.exactlyOneBookOpen() == true
					|| navigator.isParallelTextOn() == true)
				navigator.displayTOC(0);
			return true;

		case R.id.toc1:
			if (!navigator.displayTOC(0))
				errorMessage(getString(R.string.error_tocNotFound));
			return true;
		case R.id.toc2:
			if (navigator.displayTOC(1))
				errorMessage(getString(R.string.error_tocNotFound));
			return true;
		case R.id.changeSize:
			try {
				DialogFragment newFragment = new SetPanelSize();
				newFragment.show(getFragmentManager(), "");
			} catch (Exception e) {
				errorMessage(getString(R.string.error_cannotChangeSizes));
			}
			return true;
		case R.id.Style: // work in progress...
			try {
				if (navigator.exactlyOneBookOpen() == true) {
					DialogFragment newFragment = new ChangeCSSMenu();
					newFragment.show(getFragmentManager(), "");
					bookSelector = 0;
				}
			} catch (Exception e) {
				errorMessage(getString(R.string.error_CannotChangeStyle));
			}
			return true;

		case R.id.StyleBook1:
			try {
				{
					DialogFragment newFragment = new ChangeCSSMenu();
					newFragment.show(getFragmentManager(), "");
					bookSelector = 0;
				}
			} catch (Exception e) {
				errorMessage(getString(R.string.error_CannotChangeStyle));
			}
			return true;

		case R.id.StyleBook2:
			try {
				{
					DialogFragment newFragment = new ChangeCSSMenu();
					newFragment.show(getFragmentManager(), "");
					bookSelector = 1;
				}
			} catch (Exception e) {
				errorMessage(getString(R.string.error_CannotChangeStyle));
			}
			return true;

			/*
			 * case R.id.SyncScroll: syncScrollActivated = !syncScrollActivated;
			 * return true;
			 */

		case R.id.audio:
			if (navigator.exactlyOneBookOpen() == true)
				if (!navigator.extractAudio(0))
					errorMessage(getString(R.string.no_audio));
			return true;
		case R.id.firstAudio:
			if (!navigator.extractAudio(0))
				errorMessage(getString(R.string.no_audio));
			return true;
		case R.id.secondAudio:
			if (!navigator.extractAudio(1))
				errorMessage(getString(R.string.no_audio));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// ----

	// ---- Panels Manager
	public void addPanel(SplitPanel p) {
		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(R.id.MainLayout, p, p.getTag());
		fragmentTransaction.commit();

		panelCount++;
	}

	public void attachPanel(SplitPanel p) {
		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.attach(p);
		fragmentTransaction.commit();

		panelCount++;
	}

	public void detachPanel(SplitPanel p) {
		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.detach(p);
		fragmentTransaction.commit();

		panelCount--;
	}

	public void removePanelWithoutClosing(SplitPanel p) {
		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.remove(p);
		fragmentTransaction.commit();

		panelCount--;
	}

	public void removePanel(SplitPanel p) {
		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.remove(p);
		fragmentTransaction.commit();

		panelCount--;
		if (panelCount <= 0)
			finish();
	}

	// ----

	// ---- Language Selection
	public void chooseLanguage(int book) {

		String[] languages;
		languages = navigator.getLanguagesBook(book);
		if (languages.length == 2)
			refreshLanguages(book, 0, 1);
		else if (languages.length > 0) {
			Bundle bundle = new Bundle();
			bundle.putInt(getString(R.string.tome), book);
			bundle.putStringArray(getString(R.string.lang), languages);

			LanguageChooser langChooser = new LanguageChooser();
			langChooser.setArguments(bundle);
			langChooser.show(getFragmentManager(), "");
		} else {
			errorMessage(getString(R.string.error_noOtherLanguages));
		}
	}

	public void refreshLanguages(int book, int first, int second) {
		navigator.parallelText(book, first, second);
	}

	// ----

	// ---- Change Style
	public void setCSS() {
		navigator.changeCSS(bookSelector, settings);
	}

	public void setBackColor(String my_backColor) {
		settings[1] = my_backColor;
	}

	public void setColor(String my_color) {
		settings[0] = my_color;
	}

	public void setFontType(String my_fontFamily) {
		settings[2] = my_fontFamily;
	}

	public void setFontSize(String my_fontSize) {
		settings[3] = my_fontSize;
	}

	public void setLineHeight(String my_lineHeight) {
		if (my_lineHeight != null)
			settings[4] = my_lineHeight;
	}

	public void setAlign(String my_Align) {
		settings[5] = my_Align;
	}

	public void setMarginLeft(String mLeft) {
		settings[6] = mLeft;
	}

	public void setMarginRight(String mRight) {
		settings[7] = mRight;
	}

	// ----

	// change the views size, changing the weight
	protected void changeViewsSize(float weight) {
		navigator.changeViewsSize(weight);
	}

	public int getHeight() {
		LinearLayout main = (LinearLayout) findViewById(R.id.MainLayout);
		return main.getMeasuredHeight();
	}

	public int getWidth() {
		LinearLayout main = (LinearLayout) findViewById(R.id.MainLayout);
		return main.getWidth();
	}

	// Save/Load State
	protected void saveState(Editor editor) {
		navigator.saveState(editor);
	}

	protected void loadState(SharedPreferences preferences) {
		if (!navigator.loadState(preferences))
			errorMessage(getString(R.string.error_cannotLoadState));
	}

	public void errorMessage(String message) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
