// Copyright 2011 Hunter Freyer (yt@hjfreyer.com)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.hjfreyer.metronome;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class MetronomeActivity extends Activity {

  private static final int CLICK_BUFFER_SIZE = 4096;

  static final String TAG = "SleepMetronome";

  private EditText durationEdit;
  private EditText startEdit;
  private EditText endEdit;

  private short[] click;
  private int clickLen;

  private TickTrackGenerator ticker;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    durationEdit = (EditText) findViewById(R.id.editDuration);
    startEdit = (EditText) findViewById(R.id.editStart);
    endEdit = (EditText) findViewById(R.id.editEnd);

    try {
      initClick();
    } catch (IOException e) {
      e.printStackTrace();
    }

    ticker = new TickTrackGenerator(click, clickLen);
  }

  @Override
  protected void onResume() {
    super.onResume();
    this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }

  public void toggleMetro(View v) throws IOException, InterruptedException {
    final ToggleButton toggle = (ToggleButton) v;
    if (!toggle.isChecked()) {
      ticker.stop();
      return;
    }

    final double duration =
      Double.parseDouble(durationEdit.getText().toString());
    final double startHz =
      Double.parseDouble(startEdit.getText().toString()) / 60;
    final double endHz = Double.parseDouble(endEdit.getText().toString()) / 60;

    AsyncTask<Void, Void, Void> tickTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          ticker.play(startHz, endHz, duration);
          return null;
        }

        @Override
        protected void onPostExecute(Void result) {
          toggle.setChecked(false);
        }
      };

    tickTask.execute();
  }

  public void initClick() throws IOException {
    InputStream raw = getResources().openRawResource(R.raw.click);

    click = new short[CLICK_BUFFER_SIZE];
    clickLen = 0;
    while (true) {
      int a = raw.read();
      if (a == -1) {
        break;
      }

      int b = raw.read();
      if (b == -1) {
        throw new EOFException("Found EOF half way through short.");
      }

      // Little endian byte pair to short.
      click[clickLen++] = (short)((b << 8) | (a & 0xff));
    }
  }
}