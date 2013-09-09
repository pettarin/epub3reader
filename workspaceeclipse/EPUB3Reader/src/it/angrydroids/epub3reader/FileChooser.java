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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileChooser extends Activity {

	static List<File> epubs;
	static List<String> names;
	ArrayAdapter<String> adapter;
	static File selected;
	boolean firstTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_chooser_layout);

		if ((epubs == null) || (epubs.size() == 0)) {
			epubs = epubList(Environment.getExternalStorageDirectory());
		}

		ListView list = (ListView) findViewById(R.id.fileListView);
		names = fileNames(epubs);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, names);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View itemView,
					int position, long itemId) {
				selected = epubs.get(position);
				Intent resultIntent = new Intent();
				// TODO: hardcoded string
				resultIntent.putExtra("bpath", selected.getAbsolutePath());
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});

		list.setAdapter(adapter);
	}

	// TODO: hardcoded string
	private List<String> fileNames(List<File> files) {
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < files.size(); i++) {
			res.add(files.get(i).getName().replace(".epub", ""));
			/*
			 * NOTE: future
			res.add(files.get(i).getName().replace(".epub", "").replace(".e0", ""));
			*/
		}
		return res;
	}

	// TODO: hardcoded string
	// TODO: check with mimetype, not with filename extension
	private List<File> epubList(File dir) {
		List<File> res = new ArrayList<File>();
		if (dir.isDirectory()) {
			File[] f = dir.listFiles();
			if (f != null) {
				for (int i = 0; i < f.length; i++) {
					if (f[i].isDirectory()) {
						res.addAll(epubList(f[i]));
					} else {
						String lowerCasedName = f[i].getName().toLowerCase();
						if (lowerCasedName.endsWith(".epub")) {
							res.add(f[i]);
						}

						/*
						 * NOTE: future
						if ((lowerCasedName.endsWith(".epub"))
								|| (lowerCasedName.endsWith(".e0"))) {
							res.add(f[i]);
						}
						*/
					}
				}
			}
		}
		return res;
	}

	private void refreshList() {
		epubs = epubList(Environment.getExternalStorageDirectory());
		names.clear();
		names.addAll(fileNames(epubs));
		this.adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.file_chooser, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.update:
			refreshList();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
