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

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class EpubNavigator {

	//----- NEW NAVIGATOR
	
	// Fields
	private int nBooks;					// maximum number of books in the same time
	private EpubManipulator[] books;	// array of opened books
	private Fragment[] views;
	private String[] viewedPages;
	private SplitPanel activity;
	private static Context context;
	
	public EpubNavigator(int numberOfBooks, SplitPanel a) {
		nBooks = numberOfBooks;
		books = new EpubManipulator[nBooks];
		views = new Fragment[nBooks];
		viewedPages = new String[nBooks];
		activity = a;
		context = a.getBaseContext();
	}
	
	public boolean openBook(String path, int index)
	{
		try {
			if(books[index] != null)
				books[index].destroy();

			books[index] = new EpubManipulator(path, index+"", context);
			
			if(views[index]==null)							// if the fragment doesn't exists
				views[index] = activity.addBookView(index);	// create it
			else											// or
				activity.attachFragment(views[index]);		// re-attach it in the main activity
			
			((BookView)views[index]).index = index;
			viewedPages[index] = books[index].getSpineElementPath(0);
			setView(books[index].getSpineElementPath(0), index);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void setView(String page, int index) {

		viewedPages[index] = page;
		if ((books[index] != null) && (books[index].goToPage(page))) {
			// book mode
			((BookView)views[index]).state = ViewStateEnum.books;
		} else {
			// note or external link mode
			((BookView)views[index]).state = ViewStateEnum.notes;
		}
		
		loadPageIntoView(page, index);
	}
	
	// re-load every page on its own view 
	public void loadPages()
	{
		for(int i = 0; i < views.length; i++)
			if(views[i]!=null)
				((BookView) views[i]).view.loadUrl(viewedPages[i]);
		
	}
	
	public void loadPageIntoView(String pathOfPage, int index) {
		ViewStateEnum state;
		
		if ((books[index] != null)
				&& ((pathOfPage.equals(books[index].getCurrentPageURL())) || (books[index]
						.getPageIndex(pathOfPage) >= 0)))
			state = ViewStateEnum.books;
		else
			state = ViewStateEnum.notes;
		
		
		if(views[index]==null)
			views[index] = activity.addBookView(index);
		else
		{
			activity.attachFragment(views[index]);
			((BookView) views[index]).view.loadUrl(pathOfPage);
		}
	
		((BookView) views[index]).state = state;
	}
	
	// if synchronized reading is active, change chapter in each books
	public void goToNextChapter(int book) throws Exception {
		setView(books[book].goToNextChapter(),book);
		
		if (synchronizedReadingActive)
			for(int i = 1; i < nBooks; i++)
				if(books[(book+i)%nBooks]!= null)
					setView(books[(book+i)%nBooks].goToNextChapter(),(book+i)%nBooks);
	}

	// if synchronized reading is active, change chapter in each books
	public void goToPrevChapter(int book) throws Exception {
		setView(books[book].goToPreviousChapter(),book);
		
		if (synchronizedReadingActive)
			for(int i = 1; i < nBooks; i++)
				if(books[(book+i)%nBooks]!= null)
					setView(books[(book+i)%nBooks].goToPreviousChapter(),(book+i)%nBooks);
	}
	
	public boolean atLeastOneBookOpen()
	{
		if(books[0]!=null)
			return true;
		return false;
	}
	
	public boolean exactlyOneBookOpen()
	{
		for(int i = 1; i < books.length;i++)
			if(books[i]!=null)
				return false;
		if(books[0]!=null)
			return true;
		return false;
	}
	//----- END NEW NAVIGATOR
	
	// TODO: generalize
	private EpubManipulator book1;
	private EpubManipulator book2;

	// TODO: better logic
	private boolean atLeastOneBookOpen;
	private boolean exactlyOneBookOpen;
	private boolean synchronizedReadingActive;
	private boolean parallelText = false;
	private String pageOnView1;
	private String pageOnView2;

	public EpubNavigator(Context theContext) {
		atLeastOneBookOpen = false;
		exactlyOneBookOpen = true;
		synchronizedReadingActive = false;
		pageOnView1 = "";
		pageOnView2 = "";
		if (context == null) {
			context = theContext;
		}
	}

	public boolean openBook1(String path) {
		try {
			// if a book is already open, deletes it
			if (book1 != null)
				book1.destroy();
			parallelText = false;
			book1 = new EpubManipulator(path, "1", context);
			setView1(book1.getSpineElementPath(0));
			atLeastOneBookOpen = true;
			book1.createTocFile();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean openBook2(String path) {
		try {
			if (book2 != null)
				book2.destroy();
			parallelText = false;
			book2 = new EpubManipulator(path, "2", context);
			setView2(book2.getSpineElementPath(0));
			book2.createTocFile();
			exactlyOneBookOpen = false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean parallelText(BookEnum which, int firstLanguage,
			int secondLanguage) {
		boolean ok = true;
		if (firstLanguage != -1) {
			try {
				if (which != BookEnum.first) {
					openBook1(book2.getFileName());
					book1.goToPage(book2.getCurrentSpineElementIndex());
				}
				book1.setLanguage(firstLanguage);
				setView1(book1.getCurrentPageURL());
			} catch (Exception e) {
				ok = false;
			}
		}
		if (secondLanguage != -1) {
			try {
				if (which != BookEnum.second) {
					openBook2(book1.getFileName());
					book2.goToPage(book1.getCurrentSpineElementIndex());
				}
				book2.setLanguage(secondLanguage);
				setView2(book2.getCurrentPageURL());
			} catch (Exception e) {
				ok = false;
			}
		}
		if (ok && firstLanguage != -1 && secondLanguage != -1)
			setSynchronizedReadingActive(true);

		parallelText = true;
		return ok;
	}

	public String[] getLanguagesBook1() {
		return book1.getLanguages();
	}

	public String[] getLanguagesBook2() {
		return book2.getLanguages();
	}

	// if synchronized reading is active, change chapter in both books
	public void goToNextChapter(BookEnum which) throws Exception {
		if ((synchronizedReadingActive) || (which == BookEnum.first))
			setView1(book1.goToNextChapter());
		if ((synchronizedReadingActive) || (which == BookEnum.second))
			setView2(book2.goToNextChapter());
	}

	// if synchronized reading is active, change chapter in both books
	public void goToPrevChapter(BookEnum which) throws Exception {
		if ((synchronizedReadingActive) || (which == BookEnum.first))
			setView1(book1.goToPreviousChapter());
		if ((synchronizedReadingActive) || (which == BookEnum.second))
			setView2(book2.goToPreviousChapter());
	}

	public ViewStateEnum loadPageIntoView1(String pathOfPage) {
		EpubReaderMain.getView1().loadUrl(pathOfPage);
		if ((book1 != null)
				&& ((pathOfPage.equals(book1.getCurrentPageURL())) || (book1
						.getPageIndex(pathOfPage) >= 0)))
			return ViewStateEnum.books;
		else
			return ViewStateEnum.notes;
	}

	public ViewStateEnum loadPageIntoView2(String pathOfPage) {
		EpubReaderMain.getView2().loadUrl(pathOfPage);
		if ((book2 != null)
				&& ((pathOfPage.equals(book1.getCurrentPageURL())) || (book2
						.getPageIndex(pathOfPage) >= 0)))
			return ViewStateEnum.books;
		else
			return ViewStateEnum.notes;
	}

	public ViewStateEnum closeView1() {

		// book mode?
		if ((book1.getPageIndex(pageOnView1) >= 0)
				|| (pageOnView1.equals(book1.getCurrentPageURL()))) {

			// book mode: delete it
			try {
				book1.destroy();
			} catch (Exception e) {
			}

			if ((exactlyOneBookOpen) || (book2 == null)) {
				// no second book open
				atLeastOneBookOpen = false; // There is no longer any open book
				return ViewStateEnum.invisible; // and the first view must be
												// closed
			} else {
				// second book open

				// the former book2 now becomes book1
				book1 = book2;
				book2 = null;
				pageOnView1 = pageOnView2;
				pageOnView2 = "";
				exactlyOneBookOpen = true;
				setSynchronizedReadingActive(false);
				return loadPageIntoView1(pageOnView1);
			}
		} else {
			// note mode: go back to book mode
			pageOnView1 = book1.getCurrentPageURL();
			loadPageIntoView1(book1.getCurrentPageURL());
			return ViewStateEnum.books;
		}
	}

	public ViewStateEnum closeView2() {

		// book mode?
		if ((book2 == null) || (book2.getPageIndex(pageOnView2) >= 0)
				|| (pageOnView2.equals(book2.getCurrentPageURL()))) {
			// book mode: delete it
			try {
				book2.destroy();
			} catch (Exception e) {
			}
			exactlyOneBookOpen = true;
			return ViewStateEnum.invisible;
		} else {
			// note mode: go back to book mode
			pageOnView2 = book2.getCurrentPageURL();
			loadPageIntoView2(book2.getCurrentPageURL());
			return ViewStateEnum.books;
		}
	}

	public void setSynchronizedReadingActive(boolean value) {
		synchronizedReadingActive = value;
	}

	public boolean flipSynchronizedReadingActive() {
		if (exactlyOneBookOpen)
			return false;
		synchronizedReadingActive = !synchronizedReadingActive;
		return true;
	}

	public boolean synchronizeView2WithView1() throws Exception {
		if (!exactlyOneBookOpen) {
			setView2(book2.goToPage(book1.getCurrentSpineElementIndex()));
			return true;
		} else
			return false;

	}

	public boolean synchronizeView1WithView2() throws Exception {
		if (!exactlyOneBookOpen) {
			setView1(book1.goToPage(book2.getCurrentSpineElementIndex()));
			return true;
		} else
			return false;
	}

	public ViewStateEnum setView1(String page) {
		ViewStateEnum res;
		pageOnView1 = page;
		if ((book1 != null) && (book1.goToPage(page))) {
			// book mode
			res = ViewStateEnum.books;
		} else {
			// note or external link mode
			res = ViewStateEnum.notes;
		}
		loadPageIntoView1(page);
		return res;
	}

	public ViewStateEnum setView2(String page) {
		ViewStateEnum res;
		pageOnView2 = page;

		if ((book2 != null) && (book2.goToPage(page))) {
			// book mode
			res = ViewStateEnum.books;
		} else {
			// note or external link mode
			res = ViewStateEnum.notes;
		}
		loadPageIntoView2(page);
		return res;
	}

	// display book metadata
	// returns true if metadata are available, false otherwise
	public boolean displayMetadata(BookEnum which) {
		boolean res = true;

		if (which == BookEnum.first)
			if (book1 != null) {
				pageOnView1 = getS(R.string.metadata);
				EpubReaderMain.getView1().loadData(book1.metadata(),
						getS(R.string.textOrHTML), null);
			} else
				res = false;
		else if (book2 != null) {
			pageOnView2 = getS(R.string.metadata);
			EpubReaderMain.getView2().loadData(book2.metadata(),
					getS(R.string.textOrHTML), null);
		} else
			res = false;

		return res;
	}

	// return true if TOC is available, false otherwise
	public boolean displayTOC(BookEnum which) {
		boolean res = true;

		if (which == BookEnum.first)
			if (book1 != null) {
				pageOnView1 = getS(R.string.Table_of_Contents);
				EpubReaderMain.getView1().loadUrl(book1.tableOfContents());
			} else
				res = false;
		else if (book2 != null) {
			pageOnView2 = getS(R.string.Table_of_Contents);
			EpubReaderMain.getView2().loadUrl(book2.tableOfContents());
		} else
			res = false;

		return res;
	}

	public void saveState(Editor editor) {

		editor.putBoolean(getS(R.string.bookOpen), atLeastOneBookOpen);
		editor.putBoolean(getS(R.string.onlyOne), exactlyOneBookOpen);
		editor.putBoolean(getS(R.string.sync), synchronizedReadingActive);
		editor.putBoolean(getS(R.string.parallelTextBool), parallelText);

		if (atLeastOneBookOpen) {
			if (book1 != null) {

				// book1 exists: save its state and close it
				editor.putString(getS(R.string.page1), pageOnView1);
				editor.putInt(getS(R.string.CurrentPageBook1),
						book1.getCurrentSpineElementIndex());
				editor.putInt(getS(R.string.LanguageBook1),
						book1.getCurrentLanguage());
				editor.putString(getS(R.string.nameEpub1),
						book1.getDecompressedFolder());
				editor.putString(getS(R.string.pathBook1), book1.getFileName());
				try {
					book1.closeStream();
				} catch (IOException e) {
					Log.e(getS(R.string.error_CannotCloseStream),
							getS(R.string.Book1_Stream));
					e.printStackTrace();
				}

				editor.putString(getS(R.string.page2), pageOnView2);
				if ((!exactlyOneBookOpen) && (book2 != null)) {

					// book2 exists: save its state and close it
					editor.putInt(getS(R.string.CurrentPageBook2),
							book2.getCurrentSpineElementIndex());
					editor.putInt(getS(R.string.LanguageBook2),
							book2.getCurrentLanguage());
					editor.putString(getS(R.string.nameEpub2),
							book2.getDecompressedFolder());
					editor.putString(getS(R.string.pathBook2),
							book2.getFileName());
					try {
						book2.closeStream();
					} catch (IOException e) {
						Log.e(getS(R.string.error_CannotCloseStream),
								getS(R.string.Book2_Stream));
						e.printStackTrace();
					}
				}
			}
		}
	}

	public boolean loadState(SharedPreferences preferences) {
		boolean ok = true;
		atLeastOneBookOpen = preferences.getBoolean(getS(R.string.bookOpen),
				false);
		exactlyOneBookOpen = preferences.getBoolean(getS(R.string.onlyOne),
				true);
		synchronizedReadingActive = preferences.getBoolean(getS(R.string.sync),
				false);
		parallelText = preferences.getBoolean(getS(R.string.parallelTextBool),
				false);

		if (atLeastOneBookOpen) {

			// load the first book
			pageOnView1 = preferences.getString(getS(R.string.page1), "");
			int pageIndex = preferences.getInt(getS(R.string.CurrentPageBook1),
					0);
			int langIndex = preferences.getInt(getS(R.string.LanguageBook1), 0);
			String folder = preferences.getString(getS(R.string.nameEpub1), "");
			String file = preferences.getString(getS(R.string.pathBook1), "");

			// try loading a book already extracted
			try {
				book1 = new EpubManipulator(file, folder, pageIndex, langIndex,
						context);
				book1.goToPage(pageIndex);
			} catch (Exception e1) {

				// error: retry this way
				try {
					book1 = new EpubManipulator(file, "1", context);
					book1.goToPage(pageIndex);
				} catch (Exception e2) {
					ok = false;
				}

			}

			// Show the first view's actual page
			loadPageIntoView1(pageOnView1);
			if (pageOnView1 == getS(R.string.metadata)) // if they were
														// metadata, reload them
				displayMetadata(BookEnum.first);

			if (pageOnView1 == getS(R.string.Table_of_Contents)) // if it was
																	// table of
				// content, reload it
				displayTOC(BookEnum.first);

			// If there is a second book, try to reload it
			pageOnView2 = preferences.getString(getS(R.string.page2), "");
			if (!exactlyOneBookOpen) {
				pageIndex = preferences.getInt(getS(R.string.CurrentPageBook2),
						0);
				langIndex = preferences.getInt(getS(R.string.LanguageBook2), 0);
				folder = preferences.getString(getS(R.string.nameEpub2), "");
				file = preferences.getString(getS(R.string.pathBook2), "");
				try {
					book2 = new EpubManipulator(file, folder, pageIndex,
							langIndex, context);
					book2.goToPage(pageIndex);
				} catch (Exception e3) {
					try {
						book2 = new EpubManipulator(file, "2", context);
						book2.goToPage(pageIndex);
					} catch (Exception e4) {
						ok = false;
					}
				}
			}

			loadPageIntoView2(pageOnView2);
			if (pageOnView2 == getS(R.string.metadata))
				displayMetadata(BookEnum.second);

			if (pageOnView2 == getS(R.string.Table_of_Contents))
				displayTOC(BookEnum.second);
		}
		return ok;
	}

	public boolean isExactlyOneBookOpen() {
		return exactlyOneBookOpen;
	}

	public String getS(int id) {
		return context.getResources().getString(id);
	}

	public boolean isParallelTextOn() {
		return parallelText;
	}

	public boolean isSynchronized() {
		return synchronizedReadingActive;
	}

	public boolean isAtLeastOneBookOpen() {
		return atLeastOneBookOpen;
	}

}
