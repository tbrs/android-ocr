/*
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
package tbrs.ocr.language

import android.content.Context
import android.util.Log

import tbrs.ocr.R

/**
 * Class for handling functions relating to converting between standard language
 * codes, and converting language codes to language names.
 */
class LanguageCodeHelper
/**
 * Private constructor to enforce noninstantiability
 */
private constructor() {

    init {
        throw AssertionError()
    }

    companion object {
        val TAG = "LanguageCodeHelper"

        /**
         * Map an ISO 639-3 language code to an ISO 639-1 language code.
         *
         * There is one entry here for each language recognized by the OCR engine.
         *
         * @param languageCode
         * ISO 639-3 language code
         * @return ISO 639-1 language code
         */
        fun mapLanguageCode(languageCode: String): String {
            return if (languageCode == "afr") { // Afrikaans
                "af"
            } else if (languageCode == "sqi") { // Albanian
                "sq"
            } else if (languageCode == "ara") { // Arabic
                "ar"
            } else if (languageCode == "aze") { // Azeri
                "az"
            } else if (languageCode == "eus") { // Basque
                "eu"
            } else if (languageCode == "bel") { // Belarusian
                "be"
            } else if (languageCode == "ben") { // Bengali
                "bn"
            } else if (languageCode == "bul") { // Bulgarian
                "bg"
            } else if (languageCode == "cat") { // Catalan
                "ca"
            } else if (languageCode == "chi_sim") { // Chinese (Simplified)
                "zh-CN"
            } else if (languageCode == "chi_tra") { // Chinese (Traditional)
                "zh-TW"
            } else if (languageCode == "hrv") { // Croatian
                "hr"
            } else if (languageCode == "ces") { // Czech
                "cs"
            } else if (languageCode == "dan") { // Danish
                "da"
            } else if (languageCode == "nld") { // Dutch
                "nl"
            } else if (languageCode == "eng") { // English
                "en"
            } else if (languageCode == "est") { // Estonian
                "et"
            } else if (languageCode == "fin") { // Finnish
                "fi"
            } else if (languageCode == "fra") { // French
                "fr"
            } else if (languageCode == "glg") { // Galician
                "gl"
            } else if (languageCode == "deu") { // German
                "de"
            } else if (languageCode == "ell") { // Greek
                "el"
            } else if (languageCode == "heb") { // Hebrew
                "he"
            } else if (languageCode == "hin") { // Hindi
                "hi"
            } else if (languageCode == "hun") { // Hungarian
                "hu"
            } else if (languageCode == "isl") { // Icelandic
                "is"
            } else if (languageCode == "ind") { // Indonesian
                "id"
            } else if (languageCode == "ita") { // Italian
                "it"
            } else if (languageCode == "jpn") { // Japanese
                "ja"
            } else if (languageCode == "kan") { // Kannada
                "kn"
            } else if (languageCode == "kor") { // Korean
                "ko"
            } else if (languageCode == "lav") { // Latvian
                "lv"
            } else if (languageCode == "lit") { // Lithuanian
                "lt"
            } else if (languageCode == "mkd") { // Macedonian
                "mk"
            } else if (languageCode == "msa") { // Malay
                "ms"
            } else if (languageCode == "mal") { // Malayalam
                "ml"
            } else if (languageCode == "mlt") { // Maltese
                "mt"
            } else if (languageCode == "nor") { // Norwegian
                "no"
            } else if (languageCode == "pol") { // Polish
                "pl"
            } else if (languageCode == "por") { // Portuguese
                "pt"
            } else if (languageCode == "ron") { // Romanian
                "ro"
            } else if (languageCode == "rus") { // Russian
                "ru"
            } else if (languageCode == "srp") { // Serbian (Latin) // TODO is google expecting Cyrillic?
                "sr"
            } else if (languageCode == "slk") { // Slovak
                "sk"
            } else if (languageCode == "slv") { // Slovenian
                "sl"
            } else if (languageCode == "spa") { // Spanish
                "es"
            } else if (languageCode == "swa") { // Swahili
                "sw"
            } else if (languageCode == "swe") { // Swedish
                "sv"
            } else if (languageCode == "tgl") { // Tagalog
                "tl"
            } else if (languageCode == "tam") { // Tamil
                "ta"
            } else if (languageCode == "tel") { // Telugu
                "te"
            } else if (languageCode == "tha") { // Thai
                "th"
            } else if (languageCode == "tur") { // Turkish
                "tr"
            } else if (languageCode == "ukr") { // Ukrainian
                "uk"
            } else if (languageCode == "vie") { // Vietnamese
                "vi"
            } else {
                ""
            }
        }

        /**
         * Map the given ISO 639-3 language code to a name of a language, for example,
         * "Spanish"
         *
         * @param context
         * interface to calling application environment. Needed to access
         * values from strings.xml.
         * @param languageCode
         * ISO 639-3 language code
         * @return language name
         */
        fun getOcrLanguageName(context: Context, languageCode: String): String {
            val res = context.resources
            val language6393 = res.getStringArray(R.array.iso6393)
            val languageNames = res.getStringArray(R.array.languagenames)
            var len: Int

            // Finds the given language code in the iso6393 array, and takes the name with the same index
            // from the languagenames array.
            len = 0
            while (len < language6393.size) {
                if (language6393[len] == languageCode) {
                    Log.d(TAG, "getOcrLanguageName: " + languageCode + "->"
                            + languageNames[len])
                    return languageNames[len]
                }
                len++
            }

            Log.d(TAG, "languageCode: Could not find language name for ISO 693-3: $languageCode")
            return languageCode
        }

        /**
         * Map the given ISO 639-1 language code to a name of a language, for example,
         * "Spanish"
         *
         * @param languageCode
         * ISO 639-1 language code
         * @return name of the language. For example, "English"
         */
        fun getTranslationLanguageName(context: Context, languageCode: String): String {
            val res = context.resources
            var language6391 = res.getStringArray(R.array.translationtargetiso6391_google)
            var languageNames = res.getStringArray(R.array.translationtargetlanguagenames_google)
            var len: Int

            // Finds the given language code in the translationtargetiso6391 array, and takes the name
            // with the same index from the translationtargetlanguagenames array.
            len = 0
            while (len < language6391.size) {
                if (language6391[len] == languageCode) {
                    Log.d(TAG, "getTranslationLanguageName: " + languageCode + "->" + languageNames[len])
                    return languageNames[len]
                }
                len++
            }

            // Now look in the Microsoft Translate API list. Currently this will only be needed for
            // Haitian Creole.
            language6391 = res.getStringArray(R.array.translationtargetiso6391_microsoft)
            languageNames = res.getStringArray(R.array.translationtargetlanguagenames_microsoft)
            len = 0
            while (len < language6391.size) {
                if (language6391[len] == languageCode) {
                    Log.d(TAG, "languageCode: " + languageCode + "->" + languageNames[len])
                    return languageNames[len]
                }
                len++
            }

            Log.d(TAG, "getTranslationLanguageName: Could not find language name for ISO 693-1: $languageCode")
            return ""
        }
    }

}
