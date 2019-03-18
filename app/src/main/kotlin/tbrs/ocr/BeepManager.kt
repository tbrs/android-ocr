/*
 * Copyright (C) 2010 ZXing authors
 * Copyright 2011 Robert Theis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tbrs.ocr

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.preference.PreferenceManager
import android.util.Log
import tbrs.ocr.R

import java.io.IOException

/**
 * Manages beeps and vibrations for [CaptureActivity].
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
class BeepManager(private val activity: Activity) {
    private var mediaPlayer: MediaPlayer? = null
    private var playBeep: Boolean = false

    init {
        this.mediaPlayer = null
        updatePrefs()
    }

    fun updatePrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        playBeep = shouldBeep(prefs, activity)
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
            // so we now play on the music stream.
            activity.volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = buildMediaPlayer(activity)
        }
    }

    fun playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer!!.start()
        }
    }

    companion object {

        private val TAG = BeepManager::class.java.simpleName

        private val BEEP_VOLUME = 0.10f

        private fun shouldBeep(prefs: SharedPreferences, activity: Context): Boolean {
            var shouldPlayBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, CaptureActivity.DEFAULT_TOGGLE_BEEP)
            if (shouldPlayBeep) {
                // See if sound settings overrides this
                val audioService = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (audioService.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                    shouldPlayBeep = false
                }
            }
            return shouldPlayBeep
        }

        private fun buildMediaPlayer(activity: Context): MediaPlayer? {
            var mediaPlayer: MediaPlayer? = MediaPlayer()
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            // When the beep has finished playing, rewind to queue up another one.
            mediaPlayer.setOnCompletionListener { player -> player.seekTo(0) }

            val file = activity.resources.openRawResourceFd(R.raw.beep)
            try {
                mediaPlayer.setDataSource(file.fileDescriptor, file.startOffset, file.length)
                file.close()
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME)
                mediaPlayer.prepare()
            } catch (ioe: IOException) {
                Log.w(TAG, ioe)
                mediaPlayer = null
            }

            return mediaPlayer
        }
    }

}
