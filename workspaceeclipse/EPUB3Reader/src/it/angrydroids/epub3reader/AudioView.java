package it.angrydroids.epub3reader;

import java.io.File;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

// Panel specialized to show a list of audio files and play the desired one
// The audio files are stored in an array of array.
// The first index indicates the different audio files,
// the second one indicates the different extensions for the same audio file
public class AudioView extends SplitPanel {
	String[][] audio;
	ListView list;
	private MediaPlayer player;
	private Button rew;
	private Button playpause;
	private String actuallyPlaying;
	private SeekBar progressBar;
	private Runnable update;
	private Handler progressHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		actuallyPlaying = null;
		View v = inflater.inflate(R.layout.activity_audio_view, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		list = (ListView) getView().findViewById(R.id.audioListView);
		rew = (Button) getView().findViewById(R.id.RewindButton);
		playpause = (Button) getView().findViewById(R.id.PlayPauseButton);
		progressBar = (SeekBar) getView().findViewById(R.id.progressBar);
		progressHandler = new Handler();

		rew.setEnabled(false);
		playpause.setEnabled(false);
		playpause.setText(">");

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View itemView,
					int position, long itemId) {
				int i = 0;
				boolean err = true;

				if (player == null)
					player = new MediaPlayer();

				// Try to play every format of the selected audio
				while (i < audio[position].length && err)
					try {
						player.reset();
						player.setDataSource(audio[position][i]);
						player.prepare();
						player.start();
						progressBar.setMax(player.getDuration());
						rew.setEnabled(true);
						playpause.setEnabled(true);
						playpause.setText("| |");
						actuallyPlaying = audio[position][i];
						err = false;
					} catch (Exception e) {
						actuallyPlaying = null;
					}
				if (err) {
					playpause.setEnabled(false);
					((MainActivity) getActivity())
							.errorMessage(getString(R.string.error_openaudiofile));
				}
			}
		});

		// Play or Pause Button
		playpause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player.isPlaying()) {
					player.pause();
					playpause.setText(">");
				} else {
					player.start();
					playpause.setText("| |");
					update.run();
				}
			}
		});

		// Rewind Button
		rew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player != null) {
					player.seekTo(0);
					player.start();
				}
			}
		});

		progressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (player != null)
					player.seekTo(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}
		});

		update = new Runnable() {

			@Override
			public void run() {
				if (player != null)
					progressBar.setProgress(player.getCurrentPosition());
				progressHandler.postDelayed(this, 1000);
			}
		};
		progressHandler.postDelayed(update, 1000);

		setAudioList(audio);
	}

	// Load the list of audio files
	public void setAudioList(String[][] audio) {
		this.audio = audio;
		if (audio.length > 0 && created) {
			String[] songs = new String[audio.length];
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();

			for (int i = 0; i < audio.length; i++) {
				// Get Title
				retriever.setDataSource(audio[i][0].replace("file:///", ""));
				String title = retriever
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				if (title == null)
					title = (new File(audio[i][0])).getName();

				// Get Duration
				String duration = retriever
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				if (duration != null)
					duration = (String) DateFormat.format("mm:ss",
							Integer.parseInt(duration));
				else
					duration = "";

				songs[i] = (i + 1) + "\t-\t" + title + "\t" + duration;
			}

			ArrayAdapter<String> songList = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_list_item_1, songs);
			list.setAdapter(songList);
		}
	}

	@Override
	protected void closeView() {
		if (player != null) {
			player.stop();
			player.release();
		}
		progressHandler.removeCallbacks(update);
		super.closeView();
	}

	@Override
	public void saveState(Editor editor) {
		progressHandler.removeCallbacks(update);
		super.saveState(editor);

		if (player != null) {
			editor.putBoolean(index + "isPlaying", player.isPlaying());
			editor.putInt(index + "current", player.getCurrentPosition());
			editor.putString(index + "actualSong", actuallyPlaying);
			player.stop();
			player.release();
		}
	}

	@Override
	public void loadState(SharedPreferences preferences) {
		super.loadState(preferences);
		actuallyPlaying = preferences.getString(index + "actualSong", null);
		player = new MediaPlayer();

		if (actuallyPlaying != null) {
			try {
				playpause.setEnabled(true);
				player.reset();
				player.setDataSource(actuallyPlaying);
				player.prepare();
				if (preferences.getBoolean(index + "isPlaying", false)) {
					player.start();
					playpause.setText("| |");
				} else
					playpause.setText(">");
				player.seekTo(preferences.getInt(index + "current", 0));
			} catch (Exception e) {
			}
		}
	}

}
