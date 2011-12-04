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

import android.media.AudioTrack;

public class TickTrackGenerator implements Runnable {
  static final String TAG = "TickTrackGenerator";

  private final double startPeriod;
  private final double endPeriod;
  private final double durationSecs;

  private final AudioTrack track;
  private final short[] click;
  private final int clickLen;

  public TickTrackGenerator(double startPeriod,
                            double endPeriod,
                            double durationSecs,
                            AudioTrack track,
                            short[] click,
                            int clickLen) {
    this.startPeriod = startPeriod;
    this.endPeriod = endPeriod;
    this.durationSecs = durationSecs;
    this.track = track;
    this.click = click;
    this.clickLen = clickLen;

    // In run() we set the notification marker at the end of the
    // stream. This does cleanup.
    track.setPlaybackPositionUpdateListener(
        new AudioTrack.OnPlaybackPositionUpdateListener() {
          public void onPeriodicNotification(AudioTrack track) { }

          public void onMarkerReached(AudioTrack track) {
      	    track.stop();
          }
        });
  }

  public void run() {
    short[] deadSecond = new short[track.getSampleRate()];

    int totalFrames = 0;
    double elapsed = 0;
    while (elapsed < durationSecs) {
      track.write(click, 0, clickLen);
      totalFrames += clickLen;

      double wait = waitTime(elapsed);
      elapsed += wait;

      long gap = ((long)(wait * track.getSampleRate())) - clickLen;

      while (gap > 0) {
        int toWrite = (int) (gap < deadSecond.length ? gap : deadSecond.length);
        track.write(deadSecond, 0, toWrite);
        totalFrames += toWrite;

        gap -= toWrite;
      }
    }
    track.setNotificationMarkerPosition(totalFrames);
  }

  public double waitTime(double t) {
    return (startPeriod * endPeriod * durationSecs) /
      (t * (startPeriod - endPeriod) + endPeriod * durationSecs);
  }
}