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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class EpubReaderMain extends Activity {

	EpubNavigator navigator;

	protected static WebView view1;
	protected static WebView view2;
	protected static RelativeLayout layoutView1;
	protected static RelativeLayout layoutView2;
	protected static LinearLayout layout;

	protected static Button iconCloseView1;
	protected static Button iconCloseView2;
	protected int screenWidth;
	protected int screenHeight;

	protected ViewStateEnum stateView1;
	protected ViewStateEnum stateView2;
	protected BookEnum bookSelector;
	protected float swipeOriginX, swipeOriginY;
	protected static float firstViewSize = (float) 0.5;
	protected boolean syncScrollActivated = true;
	protected static String color = "#000000"; // work in progress

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_epub_reader_main);

		// ----- get activity elements
		view1 = (WebView) findViewById(R.id.firstViewport);
		view2 = (WebView) findViewById(R.id.secondViewport);
		layout = (LinearLayout) findViewById(R.id.mainLayout);

		iconCloseView1 = (Button) findViewById(R.id.topRightFirstView);
		iconCloseView2 = (Button) findViewById(R.id.topRightSecondView);

		layoutView1 = (RelativeLayout) findViewById(R.id.firstViewportLayout);
		layoutView2 = (RelativeLayout) findViewById(R.id.secondViewportLayout);
		// -----

		// ----- get activity screen size
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		// -----

		navigator = new EpubNavigator(getBaseContext());

		// enable JavaScript for cool things to happen!
		view1.getSettings().setJavaScriptEnabled(true);
		view2.getSettings().setJavaScriptEnabled(true);

		// when the app starts, only one view is shown
		updateView1(ViewStateEnum.books);
		updateView2(ViewStateEnum.invisible);

		// ----- SWIPE PAGE
		view1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (stateView1 == ViewStateEnum.books) {
					swipePage(v, event, BookEnum.first);
				}

				if (syncScrollActivated == true
						&& stateView2 != ViewStateEnum.invisible) {
					// syncScroll(v, view2, event); work in progress
				}

				WebView view = (WebView) v;
				return view.onTouchEvent(event);
			}
		});

		view2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (stateView2 == ViewStateEnum.books) {
					swipePage(v, event, BookEnum.second);
				}
				WebView view = (WebView) v;
				return view.onTouchEvent(event);
			}
		});
		// -----

		// ----- VIEW CLOSING
		iconCloseView1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewStateEnum newState = navigator.closeView1();
				ViewStateEnum oldState = stateView1;
				updateView1(newState);
				if (oldState == ViewStateEnum.books) {
					updateView2(ViewStateEnum.invisible);
				}
			}
		});

		iconCloseView2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateView2(navigator.closeView2());
			}
		});
		// -----

		// ----- NOTE & LINK
		view1.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (stateView1 == ViewStateEnum.books
						|| stateView1 == ViewStateEnum.notes) {
					Message msg = new Message();
					msg.setTarget(new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							String url = msg.getData().getString(
									getString(R.string.url));
							if (url != null)
								if (stateView1 == ViewStateEnum.books
										|| stateView1 == ViewStateEnum.notes) {
									updateView2(ViewStateEnum.notes);
									navigator.setView2(url);
								} else {
									navigator.setView1(url);
								}
						}
					});
					view1.requestFocusNodeHref(msg);
				}
				return false;
			}
		});

		view1.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				try {
					updateView1(navigator.setView1(url));
				} catch (Exception e) {
					errorMessage(getString(R.string.error_LoadPage));
				}
				return true;
			}
		});

		view2.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Message msg = new Message();
				msg.setTarget(new Handler() {
					@Override
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
						String url = msg.getData().getString(
								getString(R.string.url));
						if (url != null) {
							try {
								if (stateView2 == ViewStateEnum.books
										|| stateView2 == ViewStateEnum.notes) {
									updateView1(ViewStateEnum.notes);
									navigator.setView1(url);
								} else {
									navigator.setView2(url);
								}
							} catch (Exception e) {
								errorMessage(getString(R.string.error_LoadPage));
							}
						}

					}
				});
				view2.requestFocusNodeHref(msg);
				return false;
			}
		});

		view2.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				try {
					updateView2(navigator.setView2(url));
				} catch (Exception e) {
					errorMessage(getString(R.string.error_LoadPage));
				}
				return true;
			}
		});
		// -----

		// ----- LOAD STATE
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		if (preferences.getBoolean(getString(R.string.bookOpen), false)) {
			loadState(preferences);
		} else {
			bookSelector = BookEnum.first;
			Intent goToChooser = new Intent(this, FileChooser.class);
			startActivityForResult(goToChooser, 0);
		}
		// -----
	}

	// load the selected book
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			String path = data.getStringExtra(getString(R.string.bpath));

			if (bookSelector == BookEnum.first) {
				if (navigator.openBook1(path)) {
					updateView1(ViewStateEnum.books);
				} else {
					errorMessage(getString(R.string.error_LoadBook));
				}
			} else if (navigator.openBook2(path)) {
				updateView2(ViewStateEnum.books);
			} else {
				errorMessage(getString(R.string.error_LoadBook));
			}
		} else if (!navigator.isAtLeastOneBookOpen()) {
			finish();
		}
	}

	// UI Controller
	protected void updateView1(ViewStateEnum state) {
		stateView1 = state;
		if (stateView1 == ViewStateEnum.invisible) {
			stateView1 = ViewStateEnum.books;
			finish();
		}
	}

	protected void updateView2(ViewStateEnum state) {
		stateView2 = state;
		switch (stateView2) {
		case books:
		case notes:
			showSecondView();
			break;
		case invisible:
			hideSecondView();
			break;
		}
	}

	protected void hideSecondView() {
		layoutView2.setVisibility(RelativeLayout.GONE);
	}

	protected void showSecondView() {
		layoutView2.setVisibility(RelativeLayout.VISIBLE);
		iconCloseView2.setVisibility(RelativeLayout.VISIBLE);
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.epub_reader_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (navigator.isParallelTextOn() == false
				&& navigator.isExactlyOneBookOpen() == false) {

			menu.findItem(R.id.meta1).setVisible(true);

			menu.findItem(R.id.meta2).setVisible(true);

			menu.findItem(R.id.toc1).setVisible(true);

			menu.findItem(R.id.toc2).setVisible(true);

			menu.findItem(R.id.FirstFront).setVisible(true);

			menu.findItem(R.id.SecondFront).setVisible(true);
		}

		if (navigator.isExactlyOneBookOpen() == false) {

			menu.findItem(R.id.Synchronize).setVisible(true);

			menu.findItem(R.id.Align).setVisible(true);
		}

		if (navigator.isExactlyOneBookOpen() == true
				|| navigator.isParallelTextOn() == true) {

			menu.findItem(R.id.meta1).setVisible(false);

			menu.findItem(R.id.meta2).setVisible(false);

			menu.findItem(R.id.toc1).setVisible(false);

			menu.findItem(R.id.toc2).setVisible(false);

			menu.findItem(R.id.FirstFront).setVisible(false);

			menu.findItem(R.id.SecondFront).setVisible(false);

		}
		if (navigator.isExactlyOneBookOpen() == true) {
			menu.findItem(R.id.Synchronize).setVisible(false);

			menu.findItem(R.id.Align).setVisible(false);

		}

		// if there is only one view, option "changeSizes" is not visualized
		if (stateView2.name().equalsIgnoreCase(getString(R.string.invisible)))
			menu.findItem(R.id.changeSize).setVisible(false);
		else
			menu.findItem(R.id.changeSize).setVisible(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.FirstEPUB:
			bookSelector = BookEnum.first;
			Intent goToChooser1 = new Intent(this, FileChooser.class);
			goToChooser1.putExtra(getString(R.string.second),
					getString(R.string.time));
			startActivityForResult(goToChooser1, 0);
			return true;

		case R.id.SecondEPUB:
			bookSelector = BookEnum.second;
			Intent goToChooser2 = new Intent(this, FileChooser.class);
			goToChooser2.putExtra(getString(R.string.second),
					getString(R.string.time));
			startActivityForResult(goToChooser2, 0);
			// invalidateOptionsMenu();
			return true;

		case R.id.Front:
			if (navigator.isExactlyOneBookOpen() == true
					|| navigator.isParallelTextOn() == true)
				chooseLanguage(BookEnum.first);
			return true;

		case R.id.FirstFront:
			chooseLanguage(BookEnum.first);
			return true;

		case R.id.SecondFront:
			if (navigator.isExactlyOneBookOpen() == false)
				chooseLanguage(BookEnum.second);
			else
				errorMessage(getString(R.string.error_onlyOneBookOpen));
			return true;

		case R.id.PconS:
			try {
				boolean yes = navigator.synchronizeView1WithView2();
				if (!yes) {
					errorMessage(getString(R.string.error_onlyOneBookOpen));
				}
			} catch (Exception e) {
				errorMessage(getString(R.string.error_cannotSynchronize));
			}
			return true;

		case R.id.SconP:
			try {
				boolean ok = navigator.synchronizeView2WithView1();
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
			if (navigator.isExactlyOneBookOpen() == true
					|| navigator.isParallelTextOn() == true) {
				navigator.displayMetadata(BookEnum.first);
				updateView1(ViewStateEnum.notes);
			} else {
			}
			return true;

		case R.id.meta1:
			if (navigator.displayMetadata(BookEnum.first))
				updateView1(ViewStateEnum.notes);
			else
				errorMessage(getString(R.string.error_metadataNotFound));
			return true;

		case R.id.meta2:
			if (navigator.displayMetadata(BookEnum.second))
				updateView2(ViewStateEnum.notes);
			else
				errorMessage(getString(R.string.error_metadataNotFound));
			return true;

		case R.id.tableOfContents:
			if (navigator.isExactlyOneBookOpen() == true
					|| navigator.isParallelTextOn() == true) {
				navigator.displayTOC(BookEnum.first);
				updateView1(ViewStateEnum.notes);
			} else {
			}
			return true;

		case R.id.toc1:
			if (navigator.displayTOC(BookEnum.first))
				updateView1(ViewStateEnum.notes);
			else
				errorMessage(getString(R.string.error_tocNotFound));
			return true;
		case R.id.toc2:
			if (navigator.displayTOC(BookEnum.second))
				updateView1(ViewStateEnum.notes);
			else
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
				DialogFragment newFragment = new ChangeCSSMenu();
				newFragment.show(getFragmentManager(), "");
			} catch (Exception e) {
				errorMessage(getString(R.string.error_CannotChangeStyle));
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Change page
	protected void swipePage(View v, MotionEvent event, BookEnum which) {
		int action = MotionEventCompat.getActionMasked(event);

		switch (action) {
		case (MotionEvent.ACTION_DOWN):
			swipeOriginX = event.getX();
			swipeOriginY = event.getY();
			break;

		case (MotionEvent.ACTION_UP):
			int quarterWidth = (int) (screenWidth * 0.25);
			float diffX = swipeOriginX - event.getX();
			float diffY = swipeOriginY - event.getY();
			float absDiffX = Math.abs(diffX);
			float absDiffY = Math.abs(diffY);

			if ((diffX > quarterWidth) && (absDiffX > absDiffY)) {
				try {
					navigator.goToNextChapter(which);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_cannotTurnPage));
				}
			} else if ((diffX < -quarterWidth) && (absDiffX > absDiffY)) {
				try {
					navigator.goToPrevChapter(which);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_cannotTurnPage));
				}
			}
			break;
		}

	}

	// TODO work in progress, navigator?
	/*
	 * protected void syncScroll(View v1, View v2, MotionEvent event) { int
	 * action = MotionEventCompat.getActionMasked(event);
	 * 
	 * switch (action) { case (MotionEvent.ACTION_DOWN): swipeOriginY =
	 * event.getY(); swipeOriginX = event.getX(); break;
	 * 
	 * case (MotionEvent.ACTION_UP): float endY = event.getY(); float endX =
	 * event.getX(); float diffY = swipeOriginY - endY; float diffX =
	 * swipeOriginX - endX; if (Math.abs(diffY) < Math.abs(diffX) ||
	 * Math.abs(diffY) > 300) // solo // per // piccoli // scroll { }
	 * 
	 * else { v2.scrollBy(0, (int) diffY); }
	 * 
	 * break; } }
	 */

	// Language Selection
	public void chooseLanguage(BookEnum which) {

		String[] languages;
		if (which == BookEnum.first) {
			languages = navigator.getLanguagesBook1();
		} else {
			languages = navigator.getLanguagesBook2();
		}
		if (languages.length > 0) {
			Bundle bundle = new Bundle();
			bundle.putString(getString(R.string.tome), which.toString());
			bundle.putStringArray(getString(R.string.lang), languages);

			LanguageChooser langChooser = new LanguageChooser();
			langChooser.setArguments(bundle);
			langChooser.show(getFragmentManager(), "");
		} else {
			errorMessage(getString(R.string.error_noOtherLanguages));
		}
	}

	public void refreshLanguages(BookEnum which, int first, int second,
			int numberOfLanguages) {
		if (which == BookEnum.second) {
			int tmp = first;
			first = second;
			second = tmp;
		}
		navigator.parallelText(which, first, second);
		if ((which == BookEnum.first) || (numberOfLanguages > 1)) {
			updateView1(ViewStateEnum.books);
		}
		if ((which == BookEnum.second) || (numberOfLanguages > 1)) {
			updateView2(ViewStateEnum.books);
		}
	}

	// change the views size, changing the weight
	protected void changeViewsSize(float weight1) {

		firstViewSize = weight1;

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			weight1 = weight1 + (float) 0.01;

			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
					0, LayoutParams.MATCH_PARENT, weight1);
			layoutView1.setLayoutParams(params1);

			LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
					0, LayoutParams.MATCH_PARENT, (1 - weight1));
			layoutView2.setLayoutParams(params2);
		} else {
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, weight1);
			layoutView1.setLayoutParams(params1);

			LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, (1 - weight1));
			layoutView2.setLayoutParams(params2);
		}
	}

	// Save/Load State
	protected void saveState(Editor editor) {
		editor.putString(getString(R.string.firstState), stateView1.name());
		editor.putString(getString(R.string.secondState), stateView2.name());

		editor.putFloat(getString(R.string.firstViewSize), getFirstViewWeight());

		navigator.saveState(editor);
	}

	protected void loadState(SharedPreferences preferences) {
		updateView1(ViewStateEnum.valueOf(preferences.getString(
				getString(R.string.firstState), ViewStateEnum.books.name())));
		updateView2(ViewStateEnum
				.valueOf(preferences.getString(getString(R.string.secondState),
						ViewStateEnum.invisible.name())));

		float viewSize = preferences.getFloat(
				getString(R.string.firstViewSize), getFirstViewWeight());
		changeViewsSize(viewSize);

		if (!navigator.loadState(preferences))
			errorMessage(getString(R.string.error_cannotLoadState));
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Editor editor = preferences.edit();
		saveState(editor);
		editor.commit();
	}

	public static WebView getView1() {
		return view1;
	}

	public static WebView getView2() {
		return view2;
	}

	public static String getColor() { // work in progress
		return color;
	}

	public static void setColor(String my_color) { // work in progress
		color = my_color;
	}

	public static float getFirstViewWeight() {
		return firstViewSize;
	}

	public void errorMessage(String message) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
	}

}
