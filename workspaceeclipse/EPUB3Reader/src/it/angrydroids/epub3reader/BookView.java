package it.angrydroids.epub3reader;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class BookView extends Fragment {

	public WebView view;
	protected Button btnCloseView;
	public ViewStateEnum state;
	protected float swipeOriginX, swipeOriginY;
	protected int screenWidth;
	protected int screenHeight;
	protected EpubNavigator navigator;
	public int index;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		navigator = ((SplitPanel) getActivity()).navigator;
		View v = inflater.inflate(R.layout.activity_book_view, container, false);				
		return v;
	}

	@Override
    public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		state = ViewStateEnum.books;
		view = (WebView) getView().findViewById(R.id.Viewport);
		btnCloseView = (Button) getView().findViewById(R.id.CloseButton);
		
		// ----- get fragment screen size
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		// -----
		
		// enable JavaScript for cool things to happen!
		view.getSettings().setJavaScriptEnabled(true);

		// ----- SWIPE PAGE
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
	
				if (state == ViewStateEnum.books) {
					swipePage(v, event, index);
				}
				WebView view = (WebView) v;
				return view.onTouchEvent(event);
			}
		});
		
		view.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				try {
					navigator.setView(url, index);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_LoadPage));
				}
				return true;
			}
		});
		
		navigator.loadPages();
	}
	
	// Change page
	protected void swipePage(View v, MotionEvent event, int book) {
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
					navigator.goToNextChapter(book);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_cannotTurnPage));
				}
			} else if ((diffX < -quarterWidth) && (absDiffX > absDiffY)) {
				try {
					navigator.goToPrevChapter(book);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_cannotTurnPage));
				}
			}
			break;
		}

	}
	
	
	public void errorMessage(String message) {
		Context context = this.getActivity().getApplicationContext();
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
