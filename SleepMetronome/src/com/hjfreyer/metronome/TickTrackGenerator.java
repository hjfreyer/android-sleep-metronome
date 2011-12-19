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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class TickTrackGenerator {
  static final String TAG = "TickTrackGenerator";
  private static final int SAMPLE_RATE = 22050;

  private final double startHz;
  private final double endHz;
  private final double durationSecs;

  private final AudioTrack track;
  private final short[] click;
  private final int clickLen;
  
  public TickTrackGenerator(double startHz, double endHz, double durationSecs,
		short[] click, int clickLen) {
	this.startHz = startHz;
	this.endHz = endHz;
	this.durationSecs = durationSecs;
	this.click = click;
	this.clickLen = clickLen;
   
    int bufferSize =
      AudioTrack.getMinBufferSize(SAMPLE_RATE,
                                  AudioFormat.CHANNEL_OUT_MONO,
                                  AudioFormat.ENCODING_PCM_16BIT);
    track = new AudioTrack(AudioManager.STREAM_MUSIC,
                           SAMPLE_RATE,
                           AudioFormat.CHANNEL_OUT_MONO,
                           AudioFormat.ENCODING_PCM_16BIT,
                           bufferSize,
                           AudioTrack.MODE_STREAM);
  }

  public void play() {
	track.play();
    short[] deadSecond = new short[track.getSampleRate()];

    int totalFrames = 0;
    double elapsed = 0;
 
    while (elapsed < durationSecs) {
      double period = 1.0 / frequency(elapsed);
      elapsed += period;

      int periodFrames = (int)(period * track.getSampleRate());
      totalFrames += periodFrames;
      
      int clickFramesToWrite = Math.min(clickLen, periodFrames);
      track.write(click, 0, clickFramesToWrite);
      periodFrames -= clickFramesToWrite;

      while (periodFrames > 0) {
        int toWrite = Math.min(periodFrames, deadSecond.length);
        track.write(deadSecond, 0, toWrite);
        periodFrames -= toWrite;
      }
    }
  }
  
  public void stop() {
	  track.stop();
  }

  public double frequency(double t) {
    return (startHz * endHz * durationSecs) /
        (t * (startHz - endHz) + endHz * durationSecs);
  }
}