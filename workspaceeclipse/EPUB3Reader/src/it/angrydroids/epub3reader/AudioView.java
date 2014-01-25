package it.angrydroids.epub3reader;

import java.io.File;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

// Panel specialized to show a list of audio files and play the desired one
// The audio files are stored in an array of array.
// The first index indicates the different audio files,
// the second one indicates the different extensions for the same audio file
public class AudioView extends SplitPanel {
	String[][] audio;
	ListView list;
	private MediaPlayer player;
	private Button play;
	private Button pause;
	private String actuallyPlaying;

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
		play = (Button) getView().findViewById(R.id.PlayButton);
		pause = (Button) getView().findViewById(R.id.PauseButton);

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
						actuallyPlaying = audio[position][i];
						err = false;
					} catch (Exception e) {
						actuallyPlaying = null;
					}
				if (err)
					((MainActivity) getActivity())
							.errorMessage("Impossibile aprire il file multimediale");
			}
		});

		// Play Button
		play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				player.start();
			}
		});

		// Pause Button
		pause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				player.pause();
			}
		});

		setAudioList(audio);
	}

	// Load the list of audio files
	public void setAudioList(String[][] audio) {
		this.audio = audio;
		if (audio.length > 0 && created) {
			String[] songs = new String[audio.length];
			for (int i = 0; i < audio.length; i++)
				songs[i] = (new File(audio[i][0])).getName();

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
		super.closeView();
	}

	@Override
	public void saveState(Editor editor) {
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
				player.reset();
				player.setDataSource(actuallyPlaying);
				player.prepare();
				if (preferences.getBoolean(index + "isPlaying", false))
					player.start();
				player.seekTo(preferences.getInt(index + "current", 0));
			} catch (Exception e) {
			}
		}
	}

}
