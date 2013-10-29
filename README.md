# EPUB3Reader Android App

## Abstract 

* Version: 1.1
* Date: 2013-10-29
* Developers: V. Giacometti, M. Giuriato, B. Petrantuono
* Mentors: C. Fantozzi, A. Pettarin
* License: The MIT License (MIT), see the included `LICENSE.md` file
* Contact: see section `Contact Information` below

The goal of this project consists in exploring new ways of interacting with rich, structured eBooks in [EPUB format](http://idpf.org/epub/).

The rationale behind this choice is simple: 1) EPUB is an open format for eBooks and it is the _de facto_ standard for non-Amazon eBooks; and 2) the current reading systems (eReaders, desktop programs, apps) are not suitable for reading structured, complex eBooks, as they are almost always designed with linear-reading books in mind.

Hence, the main goal of the project is proposing efficient ways of interacting with structured and complex content like:

* books with many internal (e.g., footnotes/endnotes) and external links (e.g., Web resources), and
* parallel texts (e.g., original text and its translation into another language).

In both cases, the app allows the user to split the viewport, creating two separate viewing areas, mimicking the familiar interaction with paper books, where, at the same time, the user can read 1) a footnote and the main text, 2) multiple passages from the same book, 3) passages from different books, and 4) the original text with its translation in another language, on facing pages.

In the case of links, a single tap will follow them in the same window panel, while a long tap will split the viewport into two window panels, opening the link target in the second one. This mechanism makes footnotes (internal links) or other supplemental materials (internal or external links) visible at the same time with the referring text passage, exactly like footnotes in paper books.

If the XHTML files inside an EPUB comply with the naming convention described below, the app is capable of automatically showing facing pages in two languages, turning to the previous/next chapter in both panels simultaneously.

Finally, the user can split the viewport for reading two completely unrelated eBooks.


### Screenshots

[See SCREENSHOTS.md](SCREENSHOTS.md)


### Download, Installation, and Privacy/Security Notes

From this repository, you can download:

* a pre-compiled APK from the `bin/` directory
* the Eclipse project directory, ready to be imported in your Eclipse workspace, from the `workspaceeclipse/` directory
* some sample EPUB 3 eBooks containing parallel texts, from the `ebooks/` directory
* some screenshots, from the `screenshots/` directory

To install the APK on your smartphone or tablet, you need:

* Android 4.0 or higher (it _might_ work on previous versions, YMMV)
* Enable the _Unknown sources_ option in the _Security Settings_

The installation is simple: just copy the APK to your sdcard and open it with the default package manager.
If you download the APK file directly onto your tablet/smartphone, please be sure to select _Raw_ or _View raw_, otherwise you will download the GitHub Web page (with `apk` extension), and the installation will give you an error.

The installer will ask you to grant the following permissions:

* Storage (needed to read your EPUB files)
* Network communications (needed to access/display Web resources referenced by your EPUB files)
* Development tools (needed for debug/logging purposes)

_Important Note_: JavaScript is _enabled_ by default because it allows lot of interesting things in EPUB 3 eBooks. However, JavaScript might also be exploited by an attacker to damage your device. Hence, you should not load EPUB files obtained from untrusted sources. The permission to access the network is required because we want to support browsing remote resources linked from within EPUB books. We value _your_ privacy as much as _ours_: you are absolutely welcome to recompile the APK from the source code, in case you want to disable some of the aforementioned features.


### Usage Tips

When you open the app, you will be presented with a list of EPUB files found on your device. Tap on one to open it.

If you want to open a second book, just click on `Open Book 2` in the menu.

To turn chapter, just swipe left or right.

If your eBook contains parallel texts (see section below), you can activate that function by selecting `Enable Parallel Texts` in the menu and choosing between `From Book 1` or `From Book 2`. The screen will be divided into two window panels, and they will be synchronized: when you turn chapter in one of the two panels, the other will be updated as well.

To close one of the panels, just tap the small semi-transparent square in the upper right corner.

A single tap on a link will follow that link. A long tap will open its target in the other window panel (useful when you have footnotes or link to remote Web sites).


### Structure of the Code of the App

The workflow of the app is simple: when the user selects an EPUB file from the local storage, the app decompresses it into a temporary directory, it parses its OPF manifest using the [epublib library](http://www.siegmann.nl/epublib/), and it displays the first element of the spine in a WebView (i.e., a WebKit instance).

When the user swipes to the left or to the right, the previous or the next element of the spine is loaded.

When the user closes a book or quits the app, the temporary directory `epubtemp/` is deleted. In case of a forced termination, you can safely delete this directory.

Special interactions, like long taps and menu commands to open a second window panel, are caught and dealt with separately.

Observe that WebKit itself takes care of a lot of features (like opening remote URLs, loading audio/video embedded objects, running JavaScript code embedded in the eBook, etc.).


### Naming Convention for Parallel Texts

To enable the parallel text function, the eBook producer must signal the correspondence between XHTML contents to the reading system.

We implemented the following simple rule: just name two corresponding parts `filename.XX.xhtml` and `filename.YY.xhtml`, where `XX` and `YY` are two valid [ISO 639-1 language codes](http://www.iso.org/iso/home/standards/language_codes.htm). For example, `chapter1.en.xhtml` and `chapter1.it.xhtml` indicate the English and the Italian version of the first chapter of the book. If an XHTML page does not have a language code, it is considered "common" to all the languages present in the EPUB eBook, and rendered according to the order defined in the spine. This might be useful for frontmatter (e.g., a title page) or backmatter materials (e.g., a bibliography), which might be common to more than a single language.

For example, an EPUB file with the following spine:

```
title.xhtml
toc.en.xhtml
chapter1.en.xhtml
chapter2.en.xhtml
chapter3.en.xhtml
end.en.xhtml
toc.it.xhtml
chapter1.it.xhtml
chapter2.it.xhtml
chapter3.it.xhtml
end.it.xhtml
toc.de.xhtml
chapter1.de.xhtml
chapter2.de.xhtml
chapter3.de.xhtml
end.de.xhtml
appendix1.xhtml
appendix2.xhtml
```

will show a common title page (`title.xhtml`), then the TOC (`toc.??.xhtml`), three chapters (`chapter?.??.xhtml`), and an end page (`end.??.xhtml`) in English, Italian, and German, and then two common appendices (`appendix1.xhtml` and `appendix2.xhtml`). As the above example shows, more than two languages might be contained in the same EPUB eBook: since the apps currently supports only two panels, it will ask the user for selecting two languages out of those contained in the EPUB file.

You can find some working examples (i.e., EPUB files) in the `ebooks/` directory.

We chose a naming convention for ease in implementing it, but other (possibly, more powerful) markup-based conventions can be devised. Observe that by adopting a naming convention (within the EPUB specification) we ensure backward compatibility with other EPUB reading systems not supporting special rendering for parallel texts.


### Limitations and Future Work

This app should be considered a proof-of-concept, not a production-grade application. In fact, several basic functions that the average user expects from an eBook reading system are missing: library management, TOC navigation, customization of typographical settings. We plan to implement these features gradually, in the next development iterations.

Additionally, the whole source code requires a deep refactoring. An important aspect which is currently implemented naively is the asset extraction from the EPUB container: especially when dealing with large EPUB 3 files containing lots of audio or video files, unzipping the whole EPUB file before displaying it might be unacceptably slow. We might want to selectively unzip the assets in the EPUB container depending on the actual XHTML file displayed in the WebView.

Currently, the app splits the viewport in two panels only, each taking 50% of the available viewport. We plan to extend its capability by implementing more complex layouts, with more panels and user-adjustable panel size.

With respect to the parallel text feature, currently the app supports it at XHTML file-level (usually corresponding to a chapter). In theory, it can be done at a finer level by recognizing matching IDs for XHTML elements (e.g., corresponding `<p>` or `<span>` elements). Moreover, we might want to implement algorithms for automatically recognizing the book structure/elements (i.e., without an explicit correspondence mapping, either implicit through a naming convention, or explicit through markup), so that even pre-existing EPUB eBooks with parallel texts might benefit from the "split viewport"/"facing pages" feature of this app.

Since the beginning of this project, the discussion around the [E0 format](http://epubzero.blogspot.com/) began. Time permitting, we will support both EPUB and E0 in a future release of the app.

Finally, several other advanced functions might be added to enhance the reading experience of (structured) eBooks, including: import/export annotations, smart dictionary/lexicon/pronunciation lookups, recognition of citations, self-updating eBooks from remote data sources, on-the-fly eBook editing, book and reading statistics, regex/XQuery/etc. searches, and so on.


### Contact Information

Feedback about this ongoing project is much appreciated.

You can send your comments by email to [Dr. Alberto Pettarin](http://www.albertopettarin.it/) (ReadBeyond, Padova, Italy) or [Prof. Carlo Fantozzi](http://www.dei.unipd.it/~fantozzi/) (University of Padova, Padova, Italy), adding `EPUB3Reader` to the subject, thank you!

