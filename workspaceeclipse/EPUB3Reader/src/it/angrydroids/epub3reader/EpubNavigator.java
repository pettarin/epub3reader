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

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class EpubNavigator {

	private int nBooks;
	private EpubManipulator[] books;
	private SplitPanel[] views;
	private boolean[] extractAudio;
	private boolean synchronizedReadingActive;
	private boolean parallelText = false;
	private MainActivity activity;
	private static Context context;

	public EpubNavigator(int numberOfBooks, MainActivity a) {
		nBooks = numberOfBooks;
		books = new EpubManipulator[nBooks];
		views = new SplitPanel[nBooks];
		extractAudio = new boolean[nBooks];
		activity = a;
		context = a.getBaseContext();
	}

	public boolean openBook(String path, int index) {
		try {
			if (books[index] != null)
				books[index].destroy();

			books[index] = new EpubManipulator(path, index + "", context);
			changePanel(new BookView(), index);
			setBookPage(books[index].getSpineElementPath(0), index);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void setBookPage(String page, int index) {

		if (books[index] != null) {
			books[index].goToPage(page);
			if (extractAudio[index]) {
				if (views[(index + 1) % nBooks] instanceof AudioView)
					((AudioView) views[(index + 1) % nBooks])
							.setAudioList(books[index].getAudio());
				else
					extractAudio(index);
			}
		}

		loadPageIntoView(page, index);
	}

	// set the page in the next panel
	public void setNote(String page, int index) {
		loadPageIntoView(page, (index + 1) % nBooks);
	}

	public void loadPageIntoView(String pathOfPage, int index) {
		ViewStateEnum state = ViewStateEnum.notes;

		if (books[index] != null)
			if ((pathOfPage.equals(books[index].getCurrentPageURL()))
					|| (books[index].getPageIndex(pathOfPage) >= 0))
				state = ViewStateEnum.books;

		if (books[index] == null)
			state = ViewStateEnum.notes;

		if (views[index] == null || !(views[index] instanceof BookView))
			changePanel(new BookView(), index);

		((BookView) views[index]).state = state;
		((BookView) views[index]).loadPage(pathOfPage);
	}

	// if synchronized reading is active, change chapter in each books
	public void goToNextChapter(int book) throws Exception {
		setBookPage(books[book].goToNextChapter(), book);

		if (synchronizedReadingActive)
			for (int i = 1; i < nBooks; i++)
				if (books[(book + i) % nBooks] != null)
					setBookPage(books[(book + i) % nBooks].goToNextChapter(),
							(book + i) % nBooks);
	}

	// if synchronized reading is active, change chapter in each books
	public void goToPrevChapter(int book) throws Exception {
		setBookPage(books[book].goToPreviousChapter(), book);

		if (synchronizedReadingActive)
			for (int i = 1; i < nBooks; i++)
				if (books[(book + i) % nBooks] != null)
					setBookPage(
							books[(book + i) % nBooks].goToPreviousChapter(),
							(book + i) % nBooks);
	}

	public void closeView(int index) {
		if (views[index] instanceof AudioView) {
			((AudioView) views[index]).stop();
			extractAudio[index > 0 ? index - 1 : nBooks - 1] = false;
		}
		if (extractAudio[index]
				&& views[(index + 1) % nBooks] instanceof AudioView) {
			closeView((index + 1) % nBooks);
			extractAudio[index] = false;
		}

		// case: note or another panel over a book
		if (books[index] != null
				&& (!(views[index] instanceof BookView) || (((BookView) views[index]).state != ViewStateEnum.books))) {
			BookView v = new BookView();
			changePanel(v, index);
			v.loadPage(books[index].getCurrentPageURL());
		} else // all other cases
		{
			if (books[index] != null)
				try {
					books[index].destroy();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			activity.removePanel(views[index]);

			while (index < nBooks - 1) {
				books[index] = books[index + 1]; // shift left all books
				if (books[index] != null) // updating their folder
					books[index].changeDirName(index + ""); // according to the
															// index

				views[index] = views[index + 1]; // shift left every panel
				if (views[index] != null) {
					views[index].setKey(index); // update the panel key
					if (views[index] instanceof BookView
							&& ((BookView) views[index]).state == ViewStateEnum.books)
						((BookView) views[index]).loadPage(books[index]
								.getCurrentPageURL()); // reload the book page
				}
				index++;
			}
			books[nBooks - 1] = null; // last book and last view
			views[nBooks - 1] = null; // don't exist anymore
		}
	}

	public String[] getLanguagesBook(int index) {
		return books[index].getLanguages();
	}

	public boolean parallelText(int book, int firstLanguage, int secondLanguage) {
		boolean ok = true;

		if (firstLanguage != -1) {
			try {
				if (secondLanguage != -1) {
					openBook(books[book].getFileName(), (book + 1) % 2);
					books[(book + 1) % 2].goToPage(books[book]
							.getCurrentSpineElementIndex());
					books[(book + 1) % 2].setLanguage(secondLanguage);
					setBookPage(books[(book + 1) % 2].getCurrentPageURL(),
							(book + 1) % 2);
				}
				books[book].setLanguage(firstLanguage);
				setBookPage(books[book].getCurrentPageURL(), book);
			} catch (Exception e) {
				ok = false;
			}

			if (ok && firstLanguage != -1 && secondLanguage != -1)
				setSynchronizedReadingActive(true);

			parallelText = true;
		}
		return ok;
	}

	public void setSynchronizedReadingActive(boolean value) {
		synchronizedReadingActive = value;
	}

	public boolean flipSynchronizedReadingActive() {
		if (exactlyOneBookOpen())
			return false;
		synchronizedReadingActive = !synchronizedReadingActive;
		return true;
	}

	public boolean synchronizeView(int from, int to) throws Exception {
		if (!exactlyOneBookOpen()) {
			setBookPage(books[to].goToPage(books[from]
					.getCurrentSpineElementIndex()), to);
			return true;
		} else
			return false;
	}

	// display book metadata
	// returns true if metadata are available, false otherwise
	public boolean displayMetadata(int book) {
		boolean res = true;

		if (books[book] != null) {
			DataView dv = new DataView();
			dv.loadData(books[book].metadata());
			changePanel(dv, book);
		} else
			res = false;

		return res;
	}

	// return true if TOC is available, false otherwise
	public boolean displayTOC(int book) {
		boolean res = true;

		if (books[book] != null)
			setBookPage(books[book].tableOfContents(), book);
		else
			res = false;
		return res;
	}

	public void changeCSS(int book, String[] settings) {
		books[book].addCSS(settings);
		loadPageIntoView(books[book].getCurrentPageURL(), book);
	}

	public boolean extractAudio(int book) {
		if (books[book].getAudio().length > 0) {
			extractAudio[book] = true;
			AudioView a = new AudioView();
			a.setAudioList(books[book].getAudio());
			changePanel(a, (book + 1) % nBooks);
			return true;
		}
		return false;
	}

	public void changeViewsSize(float weight) {
		if (views[0] != null && views[1] != null) {
			views[0].changeWeight(1 - weight);
			views[1].changeWeight(weight);
		}
	}

	public boolean isParallelTextOn() {
		return parallelText;
	}

	public boolean isSynchronized() {
		return synchronizedReadingActive;
	}

	public boolean atLeastOneBookOpen() {
		for (int i = 0; i < nBooks; i++)
			if (books[i] != null)
				return true;
		return false;
	}

	public boolean exactlyOneBookOpen() {
		int i = 0;
		// find the first not null book
		while (i < nBooks && books[i] == null)
			i++;

		if (i == nBooks) // if every book is null
			return false; // there's no opened book and return false

		i++;

		while (i < nBooks && books[i] == null)
			i++; // find another not null book

		if (i == nBooks) // if there's no other not null book
			return true; // there's exactly one opened book
		else
			// otherwise
			return false; // there's more than one opened book
	}

	// change the panel in position "index" with the new panel p
	public void changePanel(SplitPanel p, int index) {
		if (views[index] != null) {
			activity.removePanelWithoutClosing(views[index]);
			p.changeWeight(views[index].getWeight());
		}

		if (p.isAdded())
			activity.removePanelWithoutClosing(p);

		views[index] = p;
		activity.addPanel(p);
		p.setKey(index);

		for (int i = index + 1; i < views.length; i++)
			if (views[i] != null) {
				activity.detachPanel(views[i]);
				activity.attachPanel(views[i]);
			}
	}

	// TODO: update when a new SplitPanel's inherited class is created
	private SplitPanel newPanelByClassName(String className) {
		if (className.equals(BookView.class.getName()))
			return new BookView();
		if (className.equals(DataView.class.getName()))
			return new DataView();
		if (className.equals(AudioView.class.getName()))
			return new AudioView();
		return null;
	}

	public void saveState(Editor editor) {

		editor.putBoolean(getS(R.string.sync), synchronizedReadingActive);
		editor.putBoolean(getS(R.string.parallelTextBool), parallelText);

		// Save Books
		for (int i = 0; i < nBooks; i++)
			if (books[i] != null) {
				editor.putInt(getS(R.string.CurrentPageBook) + i,
						books[i].getCurrentSpineElementIndex());
				editor.putInt(getS(R.string.LanguageBook) + i,
						books[i].getCurrentLanguage());
				editor.putString(getS(R.string.nameEpub) + i,
						books[i].getDecompressedFolder());
				editor.putString(getS(R.string.pathBook) + i,
						books[i].getFileName());
				editor.putBoolean(getS(R.string.exAudio) + i, extractAudio[i]);
				try {
					books[i].closeStream();
				} catch (IOException e) {
					Log.e(getS(R.string.error_CannotCloseStream),
							getS(R.string.Book_Stream) + (i + 1));
					e.printStackTrace();
				}
			} else {
				editor.putInt(getS(R.string.CurrentPageBook) + i, 0);
				editor.putInt(getS(R.string.LanguageBook) + i, 0);
				editor.putString(getS(R.string.nameEpub) + i, null);
				editor.putString(getS(R.string.pathBook) + i, null);
			}

		// Save views
		for (int i = 0; i < nBooks; i++)
			if (views[i] != null) {
				editor.putString(getS(R.string.ViewType) + i, views[i]
						.getClass().getName());
				views[i].saveState(editor);
				activity.removePanelWithoutClosing(views[i]);
			} else
				editor.putString(getS(R.string.ViewType) + i, "");
	}

	public boolean loadState(SharedPreferences preferences) {
		boolean ok = true;
		synchronizedReadingActive = preferences.getBoolean(getS(R.string.sync),
				false);
		parallelText = preferences.getBoolean(getS(R.string.parallelTextBool),
				false);

		int current, lang;
		String name, path;
		for (int i = 0; i < nBooks; i++) {
			current = preferences.getInt(getS(R.string.CurrentPageBook) + i, 0);
			lang = preferences.getInt(getS(R.string.LanguageBook) + i, 0);
			name = preferences.getString(getS(R.string.nameEpub) + i, null);
			path = preferences.getString(getS(R.string.pathBook) + i, null);
			extractAudio[i] = preferences.getBoolean(
					getS(R.string.exAudio) + i, false);
			// try loading a book already extracted
			if (path != null) {
				try {
					books[i] = new EpubManipulator(path, name, current, lang,
							context);
					books[i].goToPage(current);
				} catch (Exception e1) {

					// exception: retry this way
					try {
						books[i] = new EpubManipulator(path, i + "", context);
						books[i].goToPage(current);
					} catch (Exception e2) {
						ok = false;
					} catch (Error e3) {
						ok = false;
					}
				} catch (Error e) {
					// error: retry this way
					try {
						books[i] = new EpubManipulator(path, i + "", context);
						books[i].goToPage(current);
					} catch (Exception e2) {
						ok = false;
					} catch (Error e3) {
						ok = false;
					}
				}
			} else
				books[i] = null;
		}

		return ok;
	}

	public void loadViews(SharedPreferences preferences) {
		for (int i = 0; i < nBooks; i++) {
			views[i] = newPanelByClassName(preferences.getString(
					getS(R.string.ViewType) + i, ""));
			if (views[i] != null) {
				activity.addPanel(views[i]);
				views[i].setKey(i);
				if (views[i] instanceof AudioView)
					((AudioView) views[i]).setAudioList(books[i > 0 ? i - 1
							: nBooks - 1].getAudio());
				views[i].loadState(preferences);
			}
		}
	}

	public String getS(int id) {
		return context.getResources().getString(id);
	}
}
