package it.angrydroids.epub3reader;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SplitPanel extends Activity {

	public EpubNavigator navigator;
	protected BookEnum bookSelector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_split_panel);
		navigator = new EpubNavigator(2, this);
		
		bookSelector = BookEnum.first;
		Intent goToChooser = new Intent(this, FileChooser.class);
		startActivityForResult(goToChooser, 0);
	}

	// load the selected book
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			
			if (resultCode == Activity.RESULT_OK) {
				String path = data.getStringExtra(getString(R.string.bpath));
				
				if (bookSelector == BookEnum.first)
					navigator.openBook(path,0);
				else
					navigator.openBook(path,1);
			}
					/*if (navigator.openBook(path,0)) {
						//navigator.setView("", 0);
						//updateView1(ViewStateEnum.books);
					} else {
						//errorMessage(getString(R.string.error_LoadBook));
					}
				} else if (navigator.openBook(path,1)) {
					//updateView2(ViewStateEnum.books);
				} else {
					//errorMessage(getString(R.string.error_LoadBook));
				}
			} /*else if (!navigator.atLeastOneBookOpen()) {
				finish();
			}*/
		}
	
		public Fragment addBookView(int index)
		{
			BookView bv = new BookView();
			addFragment(bv, index);
			return bv;
		}
		
		public void addFragment(Fragment f, int index)
		{
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.MainLayout, f, index+"");
			fragmentTransaction.commit();
		}
		
		public void attachFragment(Fragment f)
		{
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.attach(f);
			fragmentTransaction.commit();
		}
		
		public void detachFragment(Fragment f)
		{
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.detach(f);
			fragmentTransaction.commit();
		}
		
		public void removeFragment (Fragment f)
		{
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.remove(f);
			fragmentTransaction.commit();
		}

		// Menu
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.epub_reader_main, menu);
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

			default:
				return super.onOptionsItemSelected(item);
			}
		}

}
