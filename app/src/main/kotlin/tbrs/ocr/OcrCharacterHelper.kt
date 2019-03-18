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
package tbrs.ocr

import android.content.SharedPreferences

/**
 * Helper class to enable language-specific character blacklists/whitelists.
 */
object OcrCharacterHelper {
    val KEY_CHARACTER_BLACKLIST_AFRIKAANS = "preference_character_blacklist_afrikaans"
    val KEY_CHARACTER_BLACKLIST_ALBANIAN = "preference_character_blacklist_albanian"
    val KEY_CHARACTER_BLACKLIST_ARABIC = "preference_character_blacklist_arabic"
    val KEY_CHARACTER_BLACKLIST_AZERI = "preference_character_blacklist_azeri"
    val KEY_CHARACTER_BLACKLIST_BASQUE = "preference_character_blacklist_basque"
    val KEY_CHARACTER_BLACKLIST_BELARUSIAN = "preference_character_blacklist_belarusian"
    val KEY_CHARACTER_BLACKLIST_BENGALI = "preference_character_blacklist_bengali"
    val KEY_CHARACTER_BLACKLIST_BULGARIAN = "preference_character_blacklist_bulgarian"
    val KEY_CHARACTER_BLACKLIST_CATALAN = "preference_character_blacklist_catalan"
    val KEY_CHARACTER_BLACKLIST_CHINESE_SIMPLIFIED = "preference_character_blacklist_chinese_simplified"
    val KEY_CHARACTER_BLACKLIST_CHINESE_TRADITIONAL = "preference_character_blacklist_chinese_traditional"
    val KEY_CHARACTER_BLACKLIST_CROATIAN = "preference_character_blacklist_croatian"
    val KEY_CHARACTER_BLACKLIST_CZECH = "preference_character_blacklist_czech"
    val KEY_CHARACTER_BLACKLIST_DANISH = "preference_character_blacklist_danish"
    val KEY_CHARACTER_BLACKLIST_DUTCH = "preference_character_blacklist_dutch"
    val KEY_CHARACTER_BLACKLIST_ENGLISH = "preference_character_blacklist_english"
    val KEY_CHARACTER_BLACKLIST_ESTONIAN = "preference_character_blacklist_estonian"
    val KEY_CHARACTER_BLACKLIST_FINNISH = "preference_character_blacklist_finnish"
    val KEY_CHARACTER_BLACKLIST_FRENCH = "preference_character_blacklist_french"
    val KEY_CHARACTER_BLACKLIST_GALICIAN = "preference_character_blacklist_galician"
    val KEY_CHARACTER_BLACKLIST_GERMAN = "preference_character_blacklist_german"
    val KEY_CHARACTER_BLACKLIST_GREEK = "preference_character_blacklist_greek"
    val KEY_CHARACTER_BLACKLIST_HEBREW = "preference_character_blacklist_hebrew"
    val KEY_CHARACTER_BLACKLIST_HINDI = "preference_character_blacklist_hindi"
    val KEY_CHARACTER_BLACKLIST_HUNGARIAN = "preference_character_blacklist_hungarian"
    val KEY_CHARACTER_BLACKLIST_ICELANDIC = "preference_character_blacklist_icelandic"
    val KEY_CHARACTER_BLACKLIST_INDONESIAN = "preference_character_blacklist_indonesian"
    val KEY_CHARACTER_BLACKLIST_ITALIAN = "preference_character_blacklist_italian"
    val KEY_CHARACTER_BLACKLIST_JAPANESE = "preference_character_blacklist_japanese"
    val KEY_CHARACTER_BLACKLIST_KANNADA = "preference_character_blacklist_kannada"
    val KEY_CHARACTER_BLACKLIST_KOREAN = "preference_character_blacklist_korean"
    val KEY_CHARACTER_BLACKLIST_LATVIAN = "preference_character_blacklist_latvian"
    val KEY_CHARACTER_BLACKLIST_LITHUANIAN = "preference_character_blacklist_lithuanian"
    val KEY_CHARACTER_BLACKLIST_MACEDONIAN = "preference_character_blacklist_macedonian"
    val KEY_CHARACTER_BLACKLIST_MALAY = "preference_character_blacklist_malay"
    val KEY_CHARACTER_BLACKLIST_MALAYALAM = "preference_character_blacklist_malayalam"
    val KEY_CHARACTER_BLACKLIST_MALTESE = "preference_character_blacklist_maltese"
    val KEY_CHARACTER_BLACKLIST_NORWEGIAN = "preference_character_blacklist_norwegian"
    val KEY_CHARACTER_BLACKLIST_POLISH = "preference_character_blacklist_polish"
    val KEY_CHARACTER_BLACKLIST_PORTUGUESE = "preference_character_blacklist_portuguese"
    val KEY_CHARACTER_BLACKLIST_ROMANIAN = "preference_character_blacklist_romanian"
    val KEY_CHARACTER_BLACKLIST_RUSSIAN = "preference_character_blacklist_russian"
    val KEY_CHARACTER_BLACKLIST_SERBIAN = "preference_character_blacklist_serbian"
    val KEY_CHARACTER_BLACKLIST_SLOVAK = "preference_character_blacklist_slovak"
    val KEY_CHARACTER_BLACKLIST_SLOVENIAN = "preference_character_blacklist_slovenian"
    val KEY_CHARACTER_BLACKLIST_SPANISH = "preference_character_blacklist_spanish"
    val KEY_CHARACTER_BLACKLIST_SWAHILI = "preference_character_blacklist_swahili"
    val KEY_CHARACTER_BLACKLIST_SWEDISH = "preference_character_blacklist_swedish"
    val KEY_CHARACTER_BLACKLIST_TAGALOG = "preference_character_blacklist_tagalog"
    val KEY_CHARACTER_BLACKLIST_TAMIL = "preference_character_blacklist_tamil"
    val KEY_CHARACTER_BLACKLIST_TELUGU = "preference_character_blacklist_telugu"
    val KEY_CHARACTER_BLACKLIST_THAI = "preference_character_blacklist_thai"
    val KEY_CHARACTER_BLACKLIST_TURKISH = "preference_character_blacklist_turkish"
    val KEY_CHARACTER_BLACKLIST_UKRAINIAN = "preference_character_blacklist_ukrainian"
    val KEY_CHARACTER_BLACKLIST_VIETNAMESE = "preference_character_blacklist_vietnamese"

    val KEY_CHARACTER_WHITELIST_AFRIKAANS = "preference_character_whitelist_afrikaans"
    val KEY_CHARACTER_WHITELIST_ALBANIAN = "preference_character_whitelist_albanian"
    val KEY_CHARACTER_WHITELIST_ARABIC = "preference_character_whitelist_arabic"
    val KEY_CHARACTER_WHITELIST_AZERI = "preference_character_whitelist_azeri"
    val KEY_CHARACTER_WHITELIST_BASQUE = "preference_character_whitelist_basque"
    val KEY_CHARACTER_WHITELIST_BELARUSIAN = "preference_character_whitelist_belarusian"
    val KEY_CHARACTER_WHITELIST_BENGALI = "preference_character_whitelist_bengali"
    val KEY_CHARACTER_WHITELIST_BULGARIAN = "preference_character_whitelist_bulgarian"
    val KEY_CHARACTER_WHITELIST_CATALAN = "preference_character_whitelist_catalan"
    val KEY_CHARACTER_WHITELIST_CHINESE_SIMPLIFIED = "preference_character_whitelist_chinese_simplified"
    val KEY_CHARACTER_WHITELIST_CHINESE_TRADITIONAL = "preference_character_whitelist_chinese_traditional"
    val KEY_CHARACTER_WHITELIST_CROATIAN = "preference_character_whitelist_croatian"
    val KEY_CHARACTER_WHITELIST_CZECH = "preference_character_whitelist_czech"
    val KEY_CHARACTER_WHITELIST_DANISH = "preference_character_whitelist_danish"
    val KEY_CHARACTER_WHITELIST_DUTCH = "preference_character_whitelist_dutch"
    val KEY_CHARACTER_WHITELIST_ENGLISH = "preference_character_whitelist_english"
    val KEY_CHARACTER_WHITELIST_ESTONIAN = "preference_character_whitelist_estonian"
    val KEY_CHARACTER_WHITELIST_FINNISH = "preference_character_whitelist_finnish"
    val KEY_CHARACTER_WHITELIST_FRENCH = "preference_character_whitelist_french"
    val KEY_CHARACTER_WHITELIST_GALICIAN = "preference_character_whitelist_galician"
    val KEY_CHARACTER_WHITELIST_GERMAN = "preference_character_whitelist_german"
    val KEY_CHARACTER_WHITELIST_GREEK = "preference_character_whitelist_greek"
    val KEY_CHARACTER_WHITELIST_HEBREW = "preference_character_whitelist_hebrew"
    val KEY_CHARACTER_WHITELIST_HINDI = "preference_character_whitelist_hindi"
    val KEY_CHARACTER_WHITELIST_HUNGARIAN = "preference_character_whitelist_hungarian"
    val KEY_CHARACTER_WHITELIST_ICELANDIC = "preference_character_whitelist_icelandic"
    val KEY_CHARACTER_WHITELIST_INDONESIAN = "preference_character_whitelist_indonesian"
    val KEY_CHARACTER_WHITELIST_ITALIAN = "preference_character_whitelist_italian"
    val KEY_CHARACTER_WHITELIST_JAPANESE = "preference_character_whitelist_japanese"
    val KEY_CHARACTER_WHITELIST_KANNADA = "preference_character_whitelist_kannada"
    val KEY_CHARACTER_WHITELIST_KOREAN = "preference_character_whitelist_korean"
    val KEY_CHARACTER_WHITELIST_LATVIAN = "preference_character_whitelist_latvian"
    val KEY_CHARACTER_WHITELIST_LITHUANIAN = "preference_character_whitelist_lithuanian"
    val KEY_CHARACTER_WHITELIST_MACEDONIAN = "preference_character_whitelist_macedonian"
    val KEY_CHARACTER_WHITELIST_MALAY = "preference_character_whitelist_malay"
    val KEY_CHARACTER_WHITELIST_MALAYALAM = "preference_character_whitelist_malayalam"
    val KEY_CHARACTER_WHITELIST_MALTESE = "preference_character_whitelist_maltese"
    val KEY_CHARACTER_WHITELIST_NORWEGIAN = "preference_character_whitelist_norwegian"
    val KEY_CHARACTER_WHITELIST_POLISH = "preference_character_whitelist_polish"
    val KEY_CHARACTER_WHITELIST_PORTUGUESE = "preference_character_whitelist_portuguese"
    val KEY_CHARACTER_WHITELIST_ROMANIAN = "preference_character_whitelist_romanian"
    val KEY_CHARACTER_WHITELIST_RUSSIAN = "preference_character_whitelist_russian"
    val KEY_CHARACTER_WHITELIST_SERBIAN = "preference_character_whitelist_serbian"
    val KEY_CHARACTER_WHITELIST_SLOVAK = "preference_character_whitelist_slovak"
    val KEY_CHARACTER_WHITELIST_SLOVENIAN = "preference_character_whitelist_slovenian"
    val KEY_CHARACTER_WHITELIST_SPANISH = "preference_character_whitelist_spanish"
    val KEY_CHARACTER_WHITELIST_SWAHILI = "preference_character_whitelist_swahili"
    val KEY_CHARACTER_WHITELIST_SWEDISH = "preference_character_whitelist_swedish"
    val KEY_CHARACTER_WHITELIST_TAGALOG = "preference_character_whitelist_tagalog"
    val KEY_CHARACTER_WHITELIST_TAMIL = "preference_character_whitelist_tamil"
    val KEY_CHARACTER_WHITELIST_TELUGU = "preference_character_whitelist_telugu"
    val KEY_CHARACTER_WHITELIST_THAI = "preference_character_whitelist_thai"
    val KEY_CHARACTER_WHITELIST_TURKISH = "preference_character_whitelist_turkish"
    val KEY_CHARACTER_WHITELIST_UKRAINIAN = "preference_character_whitelist_ukrainian"
    val KEY_CHARACTER_WHITELIST_VIETNAMESE = "preference_character_whitelist_vietnamese"

    fun getDefaultBlacklist(languageCode: String): String {
        //final String DEFAULT_BLACKLIST = "`~|";

        return if (languageCode == "afr") {
            ""
        } // Afrikaans
        else if (languageCode == "sqi") {
            ""
        } // Albanian
        else if (languageCode == "ara") {
            ""
        } // Arabic
        else if (languageCode == "aze") {
            ""
        } // Azeri
        else if (languageCode == "eus") {
            ""
        } // Basque
        else if (languageCode == "bel") {
            ""
        } // Belarusian
        else if (languageCode == "ben") {
            ""
        } // Bengali
        else if (languageCode == "bul") {
            ""
        } // Bulgarian
        else if (languageCode == "cat") {
            ""
        } // Catalan
        else if (languageCode == "chi_sim") {
            ""
        } // Chinese (Simplified)
        else if (languageCode == "chi_tra") {
            ""
        } // Chinese (Traditional)
        else if (languageCode == "hrv") {
            ""
        } // Croatian
        else if (languageCode == "ces") {
            ""
        } // Czech
        else if (languageCode == "dan") {
            ""
        } // Danish
        else if (languageCode == "nld") {
            ""
        } // Dutch
        else if (languageCode == "eng") {
            ""
        } // English
        else if (languageCode == "est") {
            ""
        } // Estonian
        else if (languageCode == "fin") {
            ""
        } // Finnish
        else if (languageCode == "fra") {
            ""
        } // French
        else if (languageCode == "glg") {
            ""
        } // Galician
        else if (languageCode == "deu") {
            ""
        } // German
        else if (languageCode == "ell") {
            ""
        } // Greek
        else if (languageCode == "heb") {
            ""
        } // Hebrew
        else if (languageCode == "hin") {
            ""
        } // Hindi
        else if (languageCode == "hun") {
            ""
        } // Hungarian
        else if (languageCode == "isl") {
            ""
        } // Icelandic
        else if (languageCode == "ind") {
            ""
        } // Indonesian
        else if (languageCode == "ita") {
            ""
        } // Italian
        else if (languageCode == "jpn") {
            ""
        } // Japanese
        else if (languageCode == "kan") {
            ""
        } // Kannada
        else if (languageCode == "kor") {
            ""
        } // Korean
        else if (languageCode == "lav") {
            ""
        } // Latvian
        else if (languageCode == "lit") {
            ""
        } // Lithuanian
        else if (languageCode == "mkd") {
            ""
        } // Macedonian
        else if (languageCode == "msa") {
            ""
        } // Malay
        else if (languageCode == "mal") {
            ""
        } // Malayalam
        else if (languageCode == "mlt") {
            ""
        } // Maltese
        else if (languageCode == "nor") {
            ""
        } // Norwegian
        else if (languageCode == "pol") {
            ""
        } // Polish
        else if (languageCode == "por") {
            ""
        } // Portuguese
        else if (languageCode == "ron") {
            ""
        } // Romanian
        else if (languageCode == "rus") {
            ""
        } // Russian
        else if (languageCode == "srp") {
            ""
        } // Serbian (Latin)
        else if (languageCode == "slk") {
            ""
        } // Slovak
        else if (languageCode == "slv") {
            ""
        } // Slovenian
        else if (languageCode == "spa") {
            ""
        } // Spanish
        else if (languageCode == "swa") {
            ""
        } // Swahili
        else if (languageCode == "swe") {
            ""
        } // Swedish
        else if (languageCode == "tgl") {
            ""
        } // Tagalog
        else if (languageCode == "tam") {
            ""
        } // Tamil
        else if (languageCode == "tel") {
            ""
        } // Telugu
        else if (languageCode == "tha") {
            ""
        } // Thai
        else if (languageCode == "tur") {
            ""
        } // Turkish
        else if (languageCode == "ukr") {
            ""
        } // Ukrainian
        else if (languageCode == "vie") {
            ""
        } // Vietnamese
        else {
            throw IllegalArgumentException()
        }
    }

    fun getDefaultWhitelist(languageCode: String): String {
        return if (languageCode == "afr") {
            ""
        } // Afrikaans
        else if (languageCode == "sqi") {
            ""
        } // Albanian
        else if (languageCode == "ara") {
            ""
        } // Arabic
        else if (languageCode == "aze") {
            ""
        } // Azeri
        else if (languageCode == "eus") {
            ""
        } // Basque
        else if (languageCode == "bel") {
            ""
        } // Belarusian
        else if (languageCode == "ben") {
            ""
        } // Bengali
        else if (languageCode == "bul") {
            ""
        } // Bulgarian
        else if (languageCode == "cat") {
            ""
        } // Catalan
        else if (languageCode == "chi_sim") {
            ""
        } // Chinese (Simplified)
        else if (languageCode == "chi_tra") {
            ""
        } // Chinese (Traditional)
        else if (languageCode == "hrv") {
            ""
        } // Croatian
        else if (languageCode == "ces") {
            ""
        } // Czech
        else if (languageCode == "dan") {
            ""
        } // Danish
        else if (languageCode == "nld") {
            ""
        } // Dutch
        else if (languageCode == "eng") {
            "!?@#$%&*()<>_-+=/.,:;'\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        } // English
        else if (languageCode == "est") {
            ""
        } // Estonian
        else if (languageCode == "fin") {
            ""
        } // Finnish
        else if (languageCode == "fra") {
            ""
        } // French
        else if (languageCode == "glg") {
            ""
        } // Galician
        else if (languageCode == "deu") {
            ""
        } // German
        else if (languageCode == "ell") {
            ""
        } // Greek
        else if (languageCode == "heb") {
            ""
        } // Hebrew
        else if (languageCode == "hin") {
            ""
        } // Hindi
        else if (languageCode == "hun") {
            ""
        } // Hungarian
        else if (languageCode == "isl") {
            ""
        } // Icelandic
        else if (languageCode == "ind") {
            ""
        } // Indonesian
        else if (languageCode == "ita") {
            ""
        } // Italian
        else if (languageCode == "jpn") {
            ""
        } // Japanese
        else if (languageCode == "kan") {
            ""
        } // Kannada
        else if (languageCode == "kor") {
            ""
        } // Korean
        else if (languageCode == "lav") {
            ""
        } // Latvian
        else if (languageCode == "lit") {
            ""
        } // Lithuanian
        else if (languageCode == "mkd") {
            ""
        } // Macedonian
        else if (languageCode == "msa") {
            ""
        } // Malay
        else if (languageCode == "mal") {
            ""
        } // Malayalam
        else if (languageCode == "mlt") {
            ""
        } // Maltese
        else if (languageCode == "nor") {
            ""
        } // Norwegian
        else if (languageCode == "pol") {
            ""
        } // Polish
        else if (languageCode == "por") {
            ""
        } // Portuguese
        else if (languageCode == "ron") {
            ""
        } // Romanian
        else if (languageCode == "rus") {
            ""
        } // Russian
        else if (languageCode == "srp") {
            ""
        } // Serbian (Latin)
        else if (languageCode == "slk") {
            ""
        } // Slovak
        else if (languageCode == "slv") {
            ""
        } // Slovenian
        else if (languageCode == "spa") {
            ""
        } // Spanish
        else if (languageCode == "swa") {
            ""
        } // Swahili
        else if (languageCode == "swe") {
            ""
        } // Swedish
        else if (languageCode == "tgl") {
            ""
        } // Tagalog
        else if (languageCode == "tam") {
            ""
        } // Tamil
        else if (languageCode == "tel") {
            ""
        } // Telugu
        else if (languageCode == "tha") {
            ""
        } // Thai
        else if (languageCode == "tur") {
            ""
        } // Turkish
        else if (languageCode == "ukr") {
            ""
        } // Ukrainian
        else if (languageCode == "vie") {
            ""
        } // Vietnamese
        else {
            throw IllegalArgumentException()
        }
    }

    fun getBlacklist(prefs: SharedPreferences, languageCode: String): String? {
        return if (languageCode == "afr") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_AFRIKAANS, getDefaultBlacklist(languageCode))
        } else if (languageCode == "sqi") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_ALBANIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "ara") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_ARABIC, getDefaultBlacklist(languageCode))
        } else if (languageCode == "aze") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_AZERI, getDefaultBlacklist(languageCode))
        } else if (languageCode == "eus") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_BASQUE, getDefaultBlacklist(languageCode))
        } else if (languageCode == "bel") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_BELARUSIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "ben") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_BENGALI, getDefaultBlacklist(languageCode))
        } else if (languageCode == "bul") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_BULGARIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "cat") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_CATALAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "chi_sim") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_CHINESE_SIMPLIFIED, getDefaultBlacklist(languageCode))
        } else if (languageCode == "chi_tra") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_CHINESE_TRADITIONAL, getDefaultBlacklist(languageCode))
        } else if (languageCode == "hrv") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_CROATIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "ces") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_CZECH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "dan") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_DANISH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "nld") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_DUTCH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "eng") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_ENGLISH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "est") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_ESTONIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "fin") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_FINNISH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "fra") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_FRENCH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "glg") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_GALICIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "deu") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_GERMAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "ell") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_GREEK, getDefaultBlacklist(languageCode))
        } else if (languageCode == "heb") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_HEBREW, getDefaultBlacklist(languageCode))
        } else if (languageCode == "hin") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_HINDI, getDefaultBlacklist(languageCode))
        } else if (languageCode == "hun") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_HUNGARIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "isl") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_ICELANDIC, getDefaultBlacklist(languageCode))
        } else if (languageCode == "ind") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_INDONESIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "ita") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_ITALIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "jpn") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_JAPANESE, getDefaultBlacklist(languageCode))
        } else if (languageCode == "kan") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_KANNADA, getDefaultBlacklist(languageCode))
        } else if (languageCode == "kor") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_KOREAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "lav") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_LATVIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "lit") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_LITHUANIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "mkd") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_MACEDONIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "msa") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_MALAY, getDefaultBlacklist(languageCode))
        } else if (languageCode == "mal") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_MALAYALAM, getDefaultBlacklist(languageCode))
        } else if (languageCode == "mlt") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_MALTESE, getDefaultBlacklist(languageCode))
        } else if (languageCode == "nor") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_NORWEGIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "pol") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_POLISH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "por") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_PORTUGUESE, getDefaultBlacklist(languageCode))
        } else if (languageCode == "ron") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_ROMANIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "rus") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_RUSSIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "srp") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_SERBIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "slk") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_SLOVAK, getDefaultBlacklist(languageCode))
        } else if (languageCode == "slv") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_SLOVENIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "spa") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_SPANISH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "swa") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_SWAHILI, getDefaultBlacklist(languageCode))
        } else if (languageCode == "swe") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_SWEDISH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "tgl") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_TAGALOG, getDefaultBlacklist(languageCode))
        } else if (languageCode == "tam") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_TAMIL, getDefaultBlacklist(languageCode))
        } else if (languageCode == "tel") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_TELUGU, getDefaultBlacklist(languageCode))
        } else if (languageCode == "tha") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_THAI, getDefaultBlacklist(languageCode))
        } else if (languageCode == "tur") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_TURKISH, getDefaultBlacklist(languageCode))
        } else if (languageCode == "ukr") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_UKRAINIAN, getDefaultBlacklist(languageCode))
        } else if (languageCode == "vie") {
            prefs.getString(KEY_CHARACTER_BLACKLIST_VIETNAMESE, getDefaultBlacklist(languageCode))
        } else {
            throw IllegalArgumentException()
        }
    }

    fun getWhitelist(prefs: SharedPreferences, languageCode: String): String? {
        return if (languageCode == "afr") {
            prefs.getString(KEY_CHARACTER_WHITELIST_AFRIKAANS, getDefaultWhitelist(languageCode))
        } else if (languageCode == "sqi") {
            prefs.getString(KEY_CHARACTER_WHITELIST_ALBANIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "ara") {
            prefs.getString(KEY_CHARACTER_WHITELIST_ARABIC, getDefaultWhitelist(languageCode))
        } else if (languageCode == "aze") {
            prefs.getString(KEY_CHARACTER_WHITELIST_AZERI, getDefaultWhitelist(languageCode))
        } else if (languageCode == "eus") {
            prefs.getString(KEY_CHARACTER_WHITELIST_BASQUE, getDefaultWhitelist(languageCode))
        } else if (languageCode == "bel") {
            prefs.getString(KEY_CHARACTER_WHITELIST_BELARUSIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "ben") {
            prefs.getString(KEY_CHARACTER_WHITELIST_BENGALI, getDefaultWhitelist(languageCode))
        } else if (languageCode == "bul") {
            prefs.getString(KEY_CHARACTER_WHITELIST_BULGARIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "cat") {
            prefs.getString(KEY_CHARACTER_WHITELIST_CATALAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "chi_sim") {
            prefs.getString(KEY_CHARACTER_WHITELIST_CHINESE_SIMPLIFIED, getDefaultWhitelist(languageCode))
        } else if (languageCode == "chi_tra") {
            prefs.getString(KEY_CHARACTER_WHITELIST_CHINESE_TRADITIONAL, getDefaultWhitelist(languageCode))
        } else if (languageCode == "hrv") {
            prefs.getString(KEY_CHARACTER_WHITELIST_CROATIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "ces") {
            prefs.getString(KEY_CHARACTER_WHITELIST_CZECH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "dan") {
            prefs.getString(KEY_CHARACTER_WHITELIST_DANISH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "nld") {
            prefs.getString(KEY_CHARACTER_WHITELIST_DUTCH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "eng") {
            prefs.getString(KEY_CHARACTER_WHITELIST_ENGLISH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "est") {
            prefs.getString(KEY_CHARACTER_WHITELIST_ESTONIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "fin") {
            prefs.getString(KEY_CHARACTER_WHITELIST_FINNISH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "fra") {
            prefs.getString(KEY_CHARACTER_WHITELIST_FRENCH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "glg") {
            prefs.getString(KEY_CHARACTER_WHITELIST_GALICIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "deu") {
            prefs.getString(KEY_CHARACTER_WHITELIST_GERMAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "ell") {
            prefs.getString(KEY_CHARACTER_WHITELIST_GREEK, getDefaultWhitelist(languageCode))
        } else if (languageCode == "heb") {
            prefs.getString(KEY_CHARACTER_WHITELIST_HEBREW, getDefaultWhitelist(languageCode))
        } else if (languageCode == "hin") {
            prefs.getString(KEY_CHARACTER_WHITELIST_HINDI, getDefaultWhitelist(languageCode))
        } else if (languageCode == "hun") {
            prefs.getString(KEY_CHARACTER_WHITELIST_HUNGARIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "isl") {
            prefs.getString(KEY_CHARACTER_WHITELIST_ICELANDIC, getDefaultWhitelist(languageCode))
        } else if (languageCode == "ind") {
            prefs.getString(KEY_CHARACTER_WHITELIST_INDONESIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "ita") {
            prefs.getString(KEY_CHARACTER_WHITELIST_ITALIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "jpn") {
            prefs.getString(KEY_CHARACTER_WHITELIST_JAPANESE, getDefaultWhitelist(languageCode))
        } else if (languageCode == "kan") {
            prefs.getString(KEY_CHARACTER_WHITELIST_KANNADA, getDefaultWhitelist(languageCode))
        } else if (languageCode == "kor") {
            prefs.getString(KEY_CHARACTER_WHITELIST_KOREAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "lav") {
            prefs.getString(KEY_CHARACTER_WHITELIST_LATVIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "lit") {
            prefs.getString(KEY_CHARACTER_WHITELIST_LITHUANIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "mkd") {
            prefs.getString(KEY_CHARACTER_WHITELIST_MACEDONIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "msa") {
            prefs.getString(KEY_CHARACTER_WHITELIST_MALAY, getDefaultWhitelist(languageCode))
        } else if (languageCode == "mal") {
            prefs.getString(KEY_CHARACTER_WHITELIST_MALAYALAM, getDefaultWhitelist(languageCode))
        } else if (languageCode == "mlt") {
            prefs.getString(KEY_CHARACTER_WHITELIST_MALTESE, getDefaultWhitelist(languageCode))
        } else if (languageCode == "nor") {
            prefs.getString(KEY_CHARACTER_WHITELIST_NORWEGIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "pol") {
            prefs.getString(KEY_CHARACTER_WHITELIST_POLISH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "por") {
            prefs.getString(KEY_CHARACTER_WHITELIST_PORTUGUESE, getDefaultWhitelist(languageCode))
        } else if (languageCode == "ron") {
            prefs.getString(KEY_CHARACTER_WHITELIST_ROMANIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "rus") {
            prefs.getString(KEY_CHARACTER_WHITELIST_RUSSIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "srp") {
            prefs.getString(KEY_CHARACTER_WHITELIST_SERBIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "slk") {
            prefs.getString(KEY_CHARACTER_WHITELIST_SLOVAK, getDefaultWhitelist(languageCode))
        } else if (languageCode == "slv") {
            prefs.getString(KEY_CHARACTER_WHITELIST_SLOVENIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "spa") {
            prefs.getString(KEY_CHARACTER_WHITELIST_SPANISH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "swa") {
            prefs.getString(KEY_CHARACTER_WHITELIST_SWAHILI, getDefaultWhitelist(languageCode))
        } else if (languageCode == "swe") {
            prefs.getString(KEY_CHARACTER_WHITELIST_SWEDISH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "tgl") {
            prefs.getString(KEY_CHARACTER_WHITELIST_TAGALOG, getDefaultWhitelist(languageCode))
        } else if (languageCode == "tam") {
            prefs.getString(KEY_CHARACTER_WHITELIST_TAMIL, getDefaultWhitelist(languageCode))
        } else if (languageCode == "tel") {
            prefs.getString(KEY_CHARACTER_WHITELIST_TELUGU, getDefaultWhitelist(languageCode))
        } else if (languageCode == "tha") {
            prefs.getString(KEY_CHARACTER_WHITELIST_THAI, getDefaultWhitelist(languageCode))
        } else if (languageCode == "tur") {
            prefs.getString(KEY_CHARACTER_WHITELIST_TURKISH, getDefaultWhitelist(languageCode))
        } else if (languageCode == "ukr") {
            prefs.getString(KEY_CHARACTER_WHITELIST_UKRAINIAN, getDefaultWhitelist(languageCode))
        } else if (languageCode == "vie") {
            prefs.getString(KEY_CHARACTER_WHITELIST_VIETNAMESE, getDefaultWhitelist(languageCode))
        } else {
            throw IllegalArgumentException()
        }
    }

    fun setBlacklist(prefs: SharedPreferences, languageCode: String, blacklist: String) {
        if (languageCode == "afr") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_AFRIKAANS, blacklist).commit()
        } else if (languageCode == "sqi") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_ALBANIAN, blacklist).commit()
        } else if (languageCode == "ara") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_ARABIC, blacklist).commit()
        } else if (languageCode == "aze") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_AZERI, blacklist).commit()
        } else if (languageCode == "eus") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_BASQUE, blacklist).commit()
        } else if (languageCode == "bel") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_BELARUSIAN, blacklist).commit()
        } else if (languageCode == "ben") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_BENGALI, blacklist).commit()
        } else if (languageCode == "bul") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_BULGARIAN, blacklist).commit()
        } else if (languageCode == "cat") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_CATALAN, blacklist).commit()
        } else if (languageCode == "chi_sim") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_CHINESE_SIMPLIFIED, blacklist).commit()
        } else if (languageCode == "chi_tra") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_CHINESE_TRADITIONAL, blacklist).commit()
        } else if (languageCode == "hrv") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_CROATIAN, blacklist).commit()
        } else if (languageCode == "ces") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_CZECH, blacklist).commit()
        } else if (languageCode == "dan") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_DANISH, blacklist).commit()
        } else if (languageCode == "nld") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_DUTCH, blacklist).commit()
        } else if (languageCode == "eng") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_ENGLISH, blacklist).commit()
        } else if (languageCode == "est") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_ESTONIAN, blacklist).commit()
        } else if (languageCode == "fin") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_FINNISH, blacklist).commit()
        } else if (languageCode == "fra") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_FRENCH, blacklist).commit()
        } else if (languageCode == "glg") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_GALICIAN, blacklist).commit()
        } else if (languageCode == "deu") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_GERMAN, blacklist).commit()
        } else if (languageCode == "ell") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_GREEK, blacklist).commit()
        } else if (languageCode == "heb") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_HEBREW, blacklist).commit()
        } else if (languageCode == "hin") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_HINDI, blacklist).commit()
        } else if (languageCode == "hun") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_HUNGARIAN, blacklist).commit()
        } else if (languageCode == "isl") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_ICELANDIC, blacklist).commit()
        } else if (languageCode == "ind") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_INDONESIAN, blacklist).commit()
        } else if (languageCode == "ita") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_ITALIAN, blacklist).commit()
        } else if (languageCode == "jpn") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_JAPANESE, blacklist).commit()
        } else if (languageCode == "kan") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_KANNADA, blacklist).commit()
        } else if (languageCode == "kor") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_KOREAN, blacklist).commit()
        } else if (languageCode == "lav") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_LATVIAN, blacklist).commit()
        } else if (languageCode == "lit") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_LITHUANIAN, blacklist).commit()
        } else if (languageCode == "mkd") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_MACEDONIAN, blacklist).commit()
        } else if (languageCode == "msa") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_MALAY, blacklist).commit()
        } else if (languageCode == "mal") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_MALAYALAM, blacklist).commit()
        } else if (languageCode == "mlt") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_MALTESE, blacklist).commit()
        } else if (languageCode == "nor") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_NORWEGIAN, blacklist).commit()
        } else if (languageCode == "pol") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_POLISH, blacklist).commit()
        } else if (languageCode == "por") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_PORTUGUESE, blacklist).commit()
        } else if (languageCode == "ron") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_ROMANIAN, blacklist).commit()
        } else if (languageCode == "rus") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_RUSSIAN, blacklist).commit()
        } else if (languageCode == "srp") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_SERBIAN, blacklist).commit()
        } else if (languageCode == "slk") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_SLOVAK, blacklist).commit()
        } else if (languageCode == "slv") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_SLOVENIAN, blacklist).commit()
        } else if (languageCode == "spa") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_SPANISH, blacklist).commit()
        } else if (languageCode == "swa") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_SWAHILI, blacklist).commit()
        } else if (languageCode == "swe") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_SWEDISH, blacklist).commit()
        } else if (languageCode == "tgl") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_TAGALOG, blacklist).commit()
        } else if (languageCode == "tam") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_TAMIL, blacklist).commit()
        } else if (languageCode == "tel") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_TELUGU, blacklist).commit()
        } else if (languageCode == "tha") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_THAI, blacklist).commit()
        } else if (languageCode == "tur") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_TURKISH, blacklist).commit()
        } else if (languageCode == "ukr") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_UKRAINIAN, blacklist).commit()
        } else if (languageCode == "vie") {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_VIETNAMESE, blacklist).commit()
        } else {
            throw IllegalArgumentException()
        }
    }

    fun setWhitelist(prefs: SharedPreferences, languageCode: String, whitelist: String) {
        if (languageCode == "afr") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_AFRIKAANS, whitelist).commit()
        } else if (languageCode == "sqi") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_ALBANIAN, whitelist).commit()
        } else if (languageCode == "ara") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_ARABIC, whitelist).commit()
        } else if (languageCode == "aze") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_AZERI, whitelist).commit()
        } else if (languageCode == "eus") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_BASQUE, whitelist).commit()
        } else if (languageCode == "bel") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_BELARUSIAN, whitelist).commit()
        } else if (languageCode == "ben") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_BENGALI, whitelist).commit()
        } else if (languageCode == "bul") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_BULGARIAN, whitelist).commit()
        } else if (languageCode == "cat") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_CATALAN, whitelist).commit()
        } else if (languageCode == "chi_sim") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_CHINESE_SIMPLIFIED, whitelist).commit()
        } else if (languageCode == "chi_tra") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_CHINESE_TRADITIONAL, whitelist).commit()
        } else if (languageCode == "hrv") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_CROATIAN, whitelist).commit()
        } else if (languageCode == "ces") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_CZECH, whitelist).commit()
        } else if (languageCode == "dan") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_DANISH, whitelist).commit()
        } else if (languageCode == "nld") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_DUTCH, whitelist).commit()
        } else if (languageCode == "eng") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_ENGLISH, whitelist).commit()
        } else if (languageCode == "est") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_ESTONIAN, whitelist).commit()
        } else if (languageCode == "fin") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_FINNISH, whitelist).commit()
        } else if (languageCode == "fra") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_FRENCH, whitelist).commit()
        } else if (languageCode == "glg") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_GALICIAN, whitelist).commit()
        } else if (languageCode == "deu") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_GERMAN, whitelist).commit()
        } else if (languageCode == "ell") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_GREEK, whitelist).commit()
        } else if (languageCode == "heb") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_HEBREW, whitelist).commit()
        } else if (languageCode == "hin") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_HINDI, whitelist).commit()
        } else if (languageCode == "hun") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_HUNGARIAN, whitelist).commit()
        } else if (languageCode == "isl") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_ICELANDIC, whitelist).commit()
        } else if (languageCode == "ind") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_INDONESIAN, whitelist).commit()
        } else if (languageCode == "ita") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_ITALIAN, whitelist).commit()
        } else if (languageCode == "jpn") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_JAPANESE, whitelist).commit()
        } else if (languageCode == "kan") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_KANNADA, whitelist).commit()
        } else if (languageCode == "kor") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_KOREAN, whitelist).commit()
        } else if (languageCode == "lav") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_LATVIAN, whitelist).commit()
        } else if (languageCode == "lit") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_LITHUANIAN, whitelist).commit()
        } else if (languageCode == "mkd") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_MACEDONIAN, whitelist).commit()
        } else if (languageCode == "msa") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_MALAY, whitelist).commit()
        } else if (languageCode == "mal") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_MALAYALAM, whitelist).commit()
        } else if (languageCode == "mlt") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_MALTESE, whitelist).commit()
        } else if (languageCode == "nor") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_NORWEGIAN, whitelist).commit()
        } else if (languageCode == "pol") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_POLISH, whitelist).commit()
        } else if (languageCode == "por") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_PORTUGUESE, whitelist).commit()
        } else if (languageCode == "ron") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_ROMANIAN, whitelist).commit()
        } else if (languageCode == "rus") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_RUSSIAN, whitelist).commit()
        } else if (languageCode == "srp") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_SERBIAN, whitelist).commit()
        } else if (languageCode == "slk") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_SLOVAK, whitelist).commit()
        } else if (languageCode == "slv") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_SLOVENIAN, whitelist).commit()
        } else if (languageCode == "spa") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_SPANISH, whitelist).commit()
        } else if (languageCode == "swa") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_SWAHILI, whitelist).commit()
        } else if (languageCode == "swe") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_SWEDISH, whitelist).commit()
        } else if (languageCode == "tgl") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_TAGALOG, whitelist).commit()
        } else if (languageCode == "tam") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_TAMIL, whitelist).commit()
        } else if (languageCode == "tel") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_TELUGU, whitelist).commit()
        } else if (languageCode == "tha") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_THAI, whitelist).commit()
        } else if (languageCode == "tur") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_TURKISH, whitelist).commit()
        } else if (languageCode == "ukr") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_UKRAINIAN, whitelist).commit()
        } else if (languageCode == "vie") {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_VIETNAMESE, whitelist).commit()
        } else {
            throw IllegalArgumentException()
        }
    }
}// Private constructor to enforce noninstantiability
