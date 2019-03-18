/*
 * Copyright (C) 2008 ZXing authors
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
package edu.sfsu.cs.orange.ocr

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.ListPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import edu.sfsu.cs.orange.ocr.language.LanguageCodeHelper
import edu.sfsu.cs.orange.ocr.language.TranslatorBing
import edu.sfsu.cs.orange.ocr.language.TranslatorGoogle

/**
 * Class to handle preferences that are saved across sessions of the app. Shows
 * a hierarchy of preferences to the user, organized into sections. These
 * preferences are displayed in the options menu that is shown when the user
 * presses the MENU button.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
class PreferencesActivity : PreferenceActivity(), OnSharedPreferenceChangeListener {

    private var listPreferenceSourceLanguage: ListPreference? = null
    private var listPreferenceTargetLanguage: ListPreference? = null
    private var listPreferenceTranslator: ListPreference? = null
    private var listPreferenceOcrEngineMode: ListPreference? = null
    private var editTextPreferenceCharacterBlacklist: EditTextPreference? = null
    private var editTextPreferenceCharacterWhitelist: EditTextPreference? = null
    private var listPreferencePageSegmentationMode: ListPreference? = null

    /**
     * Set the default preference values.
     *
     * @param Bundle
     * savedInstanceState the current Activity's state, as passed by
     * Android
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        listPreferenceSourceLanguage = preferenceScreen.findPreference(KEY_SOURCE_LANGUAGE_PREFERENCE) as ListPreference
        listPreferenceTargetLanguage = preferenceScreen.findPreference(KEY_TARGET_LANGUAGE_PREFERENCE) as ListPreference
        listPreferenceTranslator = preferenceScreen.findPreference(KEY_TRANSLATOR) as ListPreference
        listPreferenceOcrEngineMode = preferenceScreen.findPreference(KEY_OCR_ENGINE_MODE) as ListPreference
        editTextPreferenceCharacterBlacklist = preferenceScreen.findPreference(KEY_CHARACTER_BLACKLIST) as EditTextPreference
        editTextPreferenceCharacterWhitelist = preferenceScreen.findPreference(KEY_CHARACTER_WHITELIST) as EditTextPreference
        listPreferencePageSegmentationMode = preferenceScreen.findPreference(KEY_PAGE_SEGMENTATION_MODE) as ListPreference

        // Create the entries/entryvalues for the translation target language list.
        initTranslationTargetList()

    }

    /**
     * Interface definition for a callback to be invoked when a shared
     * preference is changed. Sets summary text for the app's preferences. Summary text values show the
     * current settings for the values.
     *
     * @param sharedPreferences
     * the Android.content.SharedPreferences that received the change
     * @param key
     * the key of the preference that was changed, added, or removed
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {
        // Update preference summary values to show current preferences
        if (key == KEY_TRANSLATOR) {
            listPreferenceTranslator!!.summary = sharedPreferences.getString(key, CaptureActivity.DEFAULT_TRANSLATOR)
        } else if (key == KEY_SOURCE_LANGUAGE_PREFERENCE) {

            // Set the summary text for the source language name
            listPreferenceSourceLanguage!!.summary = LanguageCodeHelper.getOcrLanguageName(baseContext, sharedPreferences.getString(key, CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE))

            // Retrieve the character blacklist/whitelist for the new language
            val blacklist = OcrCharacterHelper.getBlacklist(sharedPreferences, listPreferenceSourceLanguage!!.value)
            val whitelist = OcrCharacterHelper.getWhitelist(sharedPreferences, listPreferenceSourceLanguage!!.value)

            // Save the character blacklist/whitelist to preferences
            sharedPreferences.edit().putString(KEY_CHARACTER_BLACKLIST, blacklist).commit()
            sharedPreferences.edit().putString(KEY_CHARACTER_WHITELIST, whitelist).commit()

            // Set the blacklist/whitelist summary text
            editTextPreferenceCharacterBlacklist!!.summary = blacklist
            editTextPreferenceCharacterWhitelist!!.summary = whitelist

        } else if (key == KEY_TARGET_LANGUAGE_PREFERENCE) {
            listPreferenceTargetLanguage!!.summary = LanguageCodeHelper.getTranslationLanguageName(this, sharedPreferences.getString(key, CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE))
        } else if (key == KEY_PAGE_SEGMENTATION_MODE) {
            listPreferencePageSegmentationMode!!.summary = sharedPreferences.getString(key, CaptureActivity.DEFAULT_PAGE_SEGMENTATION_MODE)
        } else if (key == KEY_OCR_ENGINE_MODE) {
            listPreferenceOcrEngineMode!!.summary = sharedPreferences.getString(key, CaptureActivity.DEFAULT_OCR_ENGINE_MODE)
        } else if (key == KEY_CHARACTER_BLACKLIST) {

            // Save a separate, language-specific character blacklist for this language
            OcrCharacterHelper.setBlacklist(sharedPreferences,
                    listPreferenceSourceLanguage!!.value,
                    sharedPreferences.getString(key, OcrCharacterHelper.getDefaultBlacklist(listPreferenceSourceLanguage!!.value)))

            // Set the summary text
            editTextPreferenceCharacterBlacklist!!.summary = sharedPreferences.getString(key, OcrCharacterHelper.getDefaultBlacklist(listPreferenceSourceLanguage!!.value))

        } else if (key == KEY_CHARACTER_WHITELIST) {

            // Save a separate, language-specific character blacklist for this language
            OcrCharacterHelper.setWhitelist(sharedPreferences,
                    listPreferenceSourceLanguage!!.value,
                    sharedPreferences.getString(key, OcrCharacterHelper.getDefaultWhitelist(listPreferenceSourceLanguage!!.value)))

            // Set the summary text
            editTextPreferenceCharacterWhitelist!!.summary = sharedPreferences.getString(key, OcrCharacterHelper.getDefaultWhitelist(listPreferenceSourceLanguage!!.value))

        }

        // Update the languages available for translation based on the current translator selected.
        if (key == KEY_TRANSLATOR) {
            initTranslationTargetList()
        }

    }

    /**
     * Sets the list of available languages and the current target language for translation. Called
     * when the key for the current translator is changed.
     */
    internal fun initTranslationTargetList() {
        // Set the preference for the target language code, in case we've just switched from Google
        // to Bing, or Bing to Google.
        val currentLanguageCode = sharedPreferences!!.getString(KEY_TARGET_LANGUAGE_PREFERENCE,
                CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE)

        // Get the name of our language
        val currentLanguage = LanguageCodeHelper.getTranslationLanguageName(baseContext,
                currentLanguageCode)
        val translators = resources.getStringArray(R.array.translators)
        val translator = sharedPreferences!!.getString(KEY_TRANSLATOR, CaptureActivity.DEFAULT_TRANSLATOR)
        var newLanguageCode = ""
        if (translator == translators[0]) { // Bing
            // Update the list of available languages for the currently-chosen translation API.
            listPreferenceTargetLanguage!!.setEntries(R.array.translationtargetlanguagenames_microsoft)
            listPreferenceTargetLanguage!!.setEntryValues(R.array.translationtargetiso6391_microsoft)

            // Get the corresponding code for our language name
            newLanguageCode = TranslatorBing.toLanguage(currentLanguage)
        } else if (translator == translators[1]) { // Google
            // Update the list of available languages for the currently-chosen translation API.
            listPreferenceTargetLanguage!!.setEntries(R.array.translationtargetlanguagenames_google)
            listPreferenceTargetLanguage!!.setEntryValues(R.array.translationtargetiso6391_google)

            // Get the corresponding code for our language name
            newLanguageCode = TranslatorGoogle.toLanguage(currentLanguage)
        }

        // Store the code as the target language preference
        val newLanguageName = LanguageCodeHelper.getTranslationLanguageName(baseContext,
                newLanguageCode)
        listPreferenceTargetLanguage!!.value = newLanguageName // Set the radio button in the list
        sharedPreferences!!.edit().putString(PreferencesActivity.KEY_TARGET_LANGUAGE_PREFERENCE,
                newLanguageCode).commit()
        listPreferenceTargetLanguage!!.summary = newLanguageName
    }

    /**
     * Sets up initial preference summary text
     * values and registers the OnSharedPreferenceChangeListener.
     */
    override fun onResume() {
        super.onResume()
        // Set up the initial summary values
        listPreferenceTranslator!!.summary = sharedPreferences!!.getString(KEY_TRANSLATOR, CaptureActivity.DEFAULT_TRANSLATOR)
        listPreferenceSourceLanguage!!.summary = LanguageCodeHelper.getOcrLanguageName(baseContext, sharedPreferences!!.getString(KEY_SOURCE_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE))
        listPreferenceTargetLanguage!!.summary = LanguageCodeHelper.getTranslationLanguageName(baseContext, sharedPreferences!!.getString(KEY_TARGET_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE))
        listPreferencePageSegmentationMode!!.summary = sharedPreferences!!.getString(KEY_PAGE_SEGMENTATION_MODE, CaptureActivity.DEFAULT_PAGE_SEGMENTATION_MODE)
        listPreferenceOcrEngineMode!!.summary = sharedPreferences!!.getString(KEY_OCR_ENGINE_MODE, CaptureActivity.DEFAULT_OCR_ENGINE_MODE)
        editTextPreferenceCharacterBlacklist!!.summary = sharedPreferences!!.getString(KEY_CHARACTER_BLACKLIST, OcrCharacterHelper.getDefaultBlacklist(listPreferenceSourceLanguage!!.value))
        editTextPreferenceCharacterWhitelist!!.summary = sharedPreferences!!.getString(KEY_CHARACTER_WHITELIST, OcrCharacterHelper.getDefaultWhitelist(listPreferenceSourceLanguage!!.value))

        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Called when Activity is about to lose focus. Unregisters the
     * OnSharedPreferenceChangeListener.
     */
    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {

        // Preference keys not carried over from ZXing project
        val KEY_SOURCE_LANGUAGE_PREFERENCE = "sourceLanguageCodeOcrPref"
        val KEY_TARGET_LANGUAGE_PREFERENCE = "targetLanguageCodeTranslationPref"
        val KEY_TOGGLE_TRANSLATION = "preference_translation_toggle_translation"
        val KEY_CONTINUOUS_PREVIEW = "preference_capture_continuous"
        val KEY_PAGE_SEGMENTATION_MODE = "preference_page_segmentation_mode"
        val KEY_OCR_ENGINE_MODE = "preference_ocr_engine_mode"
        val KEY_CHARACTER_BLACKLIST = "preference_character_blacklist"
        val KEY_CHARACTER_WHITELIST = "preference_character_whitelist"
        val KEY_TOGGLE_LIGHT = "preference_toggle_light"
        val KEY_TRANSLATOR = "preference_translator"

        // Preference keys carried over from ZXing project
        val KEY_AUTO_FOCUS = "preferences_auto_focus"
        val KEY_DISABLE_CONTINUOUS_FOCUS = "preferences_disable_continuous_focus"
        val KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown"
        val KEY_NOT_OUR_RESULTS_SHOWN = "preferences_not_our_results_shown"
        val KEY_REVERSE_IMAGE = "preferences_reverse_image"
        val KEY_PLAY_BEEP = "preferences_play_beep"
        val KEY_VIBRATE = "preferences_vibrate"

        val TRANSLATOR_BING = "Bing Translator"
        val TRANSLATOR_GOOGLE = "Google Translate"

        private var sharedPreferences: SharedPreferences? = null
    }
}