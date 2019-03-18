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
package edu.sfsu.cs.orange.ocr

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.xeustechnologies.jtar.TarEntry
import org.xeustechnologies.jtar.TarInputStream

import com.googlecode.tesseract.android.TessBaseAPI

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log

/**
 * Installs the language data required for OCR, and initializes the OCR engine using a background
 * thread.
 */
internal class OcrInitAsyncTask
/**
 * AsyncTask to asynchronously download data and initialize Tesseract.
 *
 * @param activity
 * The calling activity
 * @param baseApi
 * API to the OCR engine
 * @param dialog
 * Dialog box with thermometer progress indicator
 * @param indeterminateDialog
 * Dialog box with indeterminate progress indicator
 * @param languageCode
 * ISO 639-2 OCR language code
 * @param languageName
 * Name of the OCR language, for example, "English"
 * @param ocrEngineMode
 * Whether to use Tesseract, Cube, or both
 */
(private val activity: CaptureActivity, private val baseApi: TessBaseAPI, private val dialog: ProgressDialog,
 private val indeterminateDialog: ProgressDialog, private val languageCode: String, private var languageName: String?,
 private val ocrEngineMode: Int) : AsyncTask<String, String, Boolean>() {
    private val context: Context

    init {
        this.context = activity.baseContext
    }

    override fun onPreExecute() {
        super.onPreExecute()
        dialog.setTitle("Please wait")
        dialog.setMessage("Checking for data installation...")
        dialog.isIndeterminate = false
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog.setCancelable(false)
        dialog.show()
        activity.setButtonVisibility(false)
    }

    /**
     * In background thread, perform required setup, and request initialization of
     * the OCR engine.
     *
     * @param params
     * [0] Pathname for the directory for storing language data files to the SD card
     */
    override fun doInBackground(vararg params: String): Boolean {
        // Check whether we need Cube data or Tesseract data.
        // Example Cube data filename: "tesseract-ocr-3.01.eng.tar"
        // Example Tesseract data filename: "eng.traineddata"
        val destinationFilenameBase = "$languageCode.traineddata"
        var isCubeSupported = false
        for (s in CaptureActivity.CUBE_SUPPORTED_LANGUAGES) {
            if (s == languageCode) {
                isCubeSupported = true
            }
        }

        // Check for, and create if necessary, folder to hold model data
        val destinationDirBase = params[0] // The storage directory, minus the
        // "tessdata" subdirectory
        val tessdataDir = File(destinationDirBase + File.separator + "tessdata")
        if (!tessdataDir.exists() && !tessdataDir.mkdirs()) {
            Log.e(TAG, "Couldn't make directory $tessdataDir")
            return false
        }

        // Create a reference to the file to save the download in
        val downloadFile = File(tessdataDir, destinationFilenameBase)

        // Check if an incomplete download is present. If a *.download file is there, delete it and
        // any (possibly half-unzipped) Tesseract and Cube data files that may be there.
        val incomplete = File(tessdataDir, "$destinationFilenameBase.download")
        val tesseractTestFile = File(tessdataDir, "$languageCode.traineddata")
        if (incomplete.exists()) {
            incomplete.delete()
            if (tesseractTestFile.exists()) {
                tesseractTestFile.delete()
            }
            deleteCubeDataFiles(tessdataDir)
        }

        // Check whether all Cube data files have already been installed
        var isAllCubeDataInstalled = false
        if (isCubeSupported) {
            var isAFileMissing = false
            var dataFile: File
            for (s in CUBE_DATA_FILES) {
                dataFile = File(tessdataDir.toString() + File.separator + languageCode + s)
                if (!dataFile.exists()) {
                    isAFileMissing = true
                }
            }
            isAllCubeDataInstalled = !isAFileMissing
        }

        // If language data files are not present, install them
        var installSuccess = false
        if (!tesseractTestFile.exists() || isCubeSupported && !isAllCubeDataInstalled) {
            Log.d(TAG, "Language data for $languageCode not found in $tessdataDir")
            deleteCubeDataFiles(tessdataDir)

            // Check assets for language data to install. If not present, download from Internet
            try {
                Log.d(TAG, "Checking for language data (" + destinationFilenameBase
                        + ".zip) in application assets...")
                // Check for a file like "eng.traineddata.zip" or "tesseract-ocr-3.01.eng.tar.zip"
                installSuccess = installFromAssets("$destinationFilenameBase.zip", tessdataDir,
                        downloadFile)
            } catch (e: IOException) {
                Log.e(TAG, "IOException", e)
            } catch (e: Exception) {
                Log.e(TAG, "Got exception", e)
            }

            if (!installSuccess) {
                // File was not packaged in assets, so download it
                Log.d(TAG, "Downloading $destinationFilenameBase.gz...")
                try {
                    installSuccess = downloadFile(destinationFilenameBase, downloadFile)
                    if (!installSuccess) {
                        Log.e(TAG, "Download failed")
                        return false
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "IOException received in doInBackground. Is a network connection available?")
                    return false
                }

            }

            // If we have a tar file at this point because we downloaded v3.01+ data, untar it
            val extension = destinationFilenameBase.substring(
                    destinationFilenameBase.lastIndexOf('.'),
                    destinationFilenameBase.length)
            if (extension == ".tar") {
                try {
                    untar(File(tessdataDir.toString() + File.separator + destinationFilenameBase),
                            tessdataDir)
                    installSuccess = true
                } catch (e: IOException) {
                    Log.e(TAG, "Untar failed")
                    return false
                }

            }

        } else {
            Log.d(TAG, "Language data for " + languageCode + " already installed in "
                    + tessdataDir.toString())
            installSuccess = true
        }

        // If OSD data file is not present, download it
        val osdFile = File(tessdataDir, CaptureActivity.OSD_FILENAME_BASE)
        var osdInstallSuccess = false
        if (!osdFile.exists()) {
            // Check assets for language data to install. If not present, download from Internet
            languageName = "orientation and script detection"
            try {
                // Check for, and delete, partially-downloaded OSD files
                val badFiles = arrayOf(CaptureActivity.OSD_FILENAME + ".gz.download", CaptureActivity.OSD_FILENAME + ".gz", CaptureActivity.OSD_FILENAME)
                for (filename in badFiles) {
                    val file = File(tessdataDir, filename)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                Log.d(TAG, "Checking for OSD data (" + CaptureActivity.OSD_FILENAME_BASE
                        + ".zip) in application assets...")
                // Check for "osd.traineddata.zip"
                osdInstallSuccess = installFromAssets(CaptureActivity.OSD_FILENAME_BASE + ".zip",
                        tessdataDir, File(CaptureActivity.OSD_FILENAME))
            } catch (e: IOException) {
                Log.e(TAG, "IOException", e)
            } catch (e: Exception) {
                Log.e(TAG, "Got exception", e)
            }

            if (!osdInstallSuccess) {
                // File was not packaged in assets, so download it
                Log.d(TAG, "Downloading " + CaptureActivity.OSD_FILENAME + ".gz...")
                try {
                    osdInstallSuccess = downloadFile(CaptureActivity.OSD_FILENAME, File(tessdataDir,
                            CaptureActivity.OSD_FILENAME))
                    if (!osdInstallSuccess) {
                        Log.e(TAG, "Download failed")
                        return false
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "IOException received in doInBackground. Is a network connection available?")
                    return false
                }

            }

        } else {
            Log.d(TAG, "OSD file already present in $tessdataDir")
            osdInstallSuccess = true
        }

        // Dismiss the progress dialog box, revealing the indeterminate dialog box behind it
        try {
            dialog.dismiss()
        } catch (e: IllegalArgumentException) {
            // Catch "View not attached to window manager" error, and continue
        }

        // Initialize the OCR engine
        return if (baseApi.init(destinationDirBase + File.separator, languageCode, ocrEngineMode)) {
            installSuccess && osdInstallSuccess
        } else false
    }

    /**
     * Delete any existing data files for Cube that are present in the given directory. Files may be
     * partially uncompressed files left over from a failed install, or pre-v3.01 traineddata files.
     *
     * @param tessdataDir
     * Directory to delete the files from
     */
    private fun deleteCubeDataFiles(tessdataDir: File) {
        var badFile: File
        for (s in CUBE_DATA_FILES) {
            badFile = File(tessdataDir.toString() + File.separator + languageCode + s)
            if (badFile.exists()) {
                Log.d(TAG, "Deleting existing file $badFile")
                badFile.delete()
            }
            badFile = File(tessdataDir.toString() + File.separator + "tesseract-ocr-3.01."
                    + languageCode + ".tar")
            if (badFile.exists()) {
                Log.d(TAG, "Deleting existing file $badFile")
                badFile.delete()
            }
        }
    }

    /**
     * Download a file from the site specified by DOWNLOAD_BASE, and gunzip to the given destination.
     *
     * @param sourceFilenameBase
     * Name of file to download, minus the required ".gz" extension
     * @param destinationFile
     * Name of file to save the unzipped data to, including path
     * @return True if download and unzip are successful
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun downloadFile(sourceFilenameBase: String, destinationFile: File): Boolean {
        try {
            return downloadGzippedFileHttp(URL(CaptureActivity.DOWNLOAD_BASE + sourceFilenameBase +
                    ".gz"),
                    destinationFile)
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("Bad URL string.")
        }

    }

    /**
     * Download a gzipped file using an HttpURLConnection, and gunzip it to the given destination.
     *
     * @param url
     * URL to download from
     * @param destinationFile
     * File to save the download as, including path
     * @return True if response received, destinationFile opened, and unzip
     * successful
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun downloadGzippedFileHttp(url: URL, destinationFile: File): Boolean {
        // Send an HTTP GET request for the file
        Log.d(TAG, "Sending GET request to $url...")
        publishProgress("Downloading data for $languageName...", "0")
        var urlConnection: HttpURLConnection? = null
        urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.allowUserInteraction = false
        urlConnection.instanceFollowRedirects = true
        urlConnection.requestMethod = "GET"
        urlConnection.connect()
        if (urlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            Log.e(TAG, "Did not get HTTP_OK response.")
            Log.e(TAG, "Response code: " + urlConnection.responseCode)
            Log.e(TAG, "Response message: " + urlConnection.responseMessage.toString())
            return false
        }
        val fileSize = urlConnection.contentLength
        val inputStream = urlConnection.inputStream
        val tempFile = File("$destinationFile.gz.download")

        // Stream the file contents to a local file temporarily
        Log.d(TAG, "Streaming download to $destinationFile.gz.download...")
        val BUFFER = 8192
        var fileOutputStream: FileOutputStream? = null
        var percentComplete: Int?
        var percentCompleteLast = 0
        try {
            fileOutputStream = FileOutputStream(tempFile)
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Exception received when opening FileOutputStream.", e)
        }

        var downloaded = 0
        val buffer = ByteArray(BUFFER)
        var bufferLength = inputStream.read(buffer, 0, BUFFER)
        while (bufferLength > 0) {
            fileOutputStream!!.write(buffer, 0, bufferLength)
            downloaded += bufferLength
            percentComplete = (downloaded / fileSize.toFloat() * 100).toInt()
            if (percentComplete > percentCompleteLast) {
                publishProgress(
                        "Downloading data for $languageName...",
                        percentComplete.toString())
                percentCompleteLast = percentComplete
            }
            bufferLength = inputStream.read(buffer, 0, BUFFER)
        }
        fileOutputStream!!.close()
        urlConnection?.disconnect()

        // Uncompress the downloaded temporary file into place, and remove the temporary file
        try {
            Log.d(TAG, "Unzipping...")
            gunzip(tempFile,
                    File(tempFile.toString().replace(".gz.download", "")))
            return true
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not available for unzipping.")
        } catch (e: IOException) {
            Log.e(TAG, "Problem unzipping file.")
        }

        return false
    }

    /**
     * Unzips the given Gzipped file to the given destination, and deletes the
     * gzipped file.
     *
     * @param zippedFile
     * The gzipped file to be uncompressed
     * @param outFilePath
     * File to unzip to, including path
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Throws(FileNotFoundException::class, IOException::class)
    private fun gunzip(zippedFile: File, outFilePath: File) {
        val uncompressedFileSize = getGzipSizeUncompressed(zippedFile)
        var percentComplete: Int?
        var percentCompleteLast = 0
        var unzippedBytes = 0
        val progressMin = 0
        var progressMax = 100 - progressMin
        publishProgress("Uncompressing data for $languageName...",
                progressMin.toString())

        // If the file is a tar file, just show progress to 50%
        val extension = zippedFile.toString().substring(
                zippedFile.toString().length - 16)
        if (extension == ".tar.gz.download") {
            progressMax = 50
        }
        val gzipInputStream = GZIPInputStream(
                BufferedInputStream(FileInputStream(zippedFile)))
        val outputStream = FileOutputStream(outFilePath)
        val bufferedOutputStream = BufferedOutputStream(
                outputStream)

        val BUFFER = 8192
        val data = ByteArray(BUFFER)
        var len: Int = gzipInputStream.read(data, 0, BUFFER)
        while (len > 0) {
            bufferedOutputStream.write(data, 0, len)
            unzippedBytes += len
            percentComplete = (unzippedBytes / uncompressedFileSize.toFloat() * progressMax).toInt() + progressMin

            if (percentComplete > percentCompleteLast) {
                publishProgress("Uncompressing data for " + languageName
                        + "...", percentComplete.toString())
                percentCompleteLast = percentComplete
            }
            len = gzipInputStream.read(data, 0, BUFFER)
        }
        gzipInputStream.close()
        bufferedOutputStream.flush()
        bufferedOutputStream.close()

        if (zippedFile.exists()) {
            zippedFile.delete()
        }
    }

    /**
     * Returns the uncompressed size for a Gzipped file.
     *
     * @param file
     * Gzipped file to get the size for
     * @return Size when uncompressed, in bytes
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getGzipSizeUncompressed(zipFile: File): Int {
        val raf = RandomAccessFile(zipFile, "r")
        raf.seek(raf.length() - 4)
        val b4 = raf.read()
        val b3 = raf.read()
        val b2 = raf.read()
        val b1 = raf.read()
        raf.close()
        return b1 shl 24 or (b2 shl 16) + (b3 shl 8) + b4
    }

    /**
     * Untar the contents of a tar file into the given directory, ignoring the
     * relative pathname in the tar file, and delete the tar file.
     *
     * Uses jtar: http://code.google.com/p/jtar/
     *
     * @param tarFile
     * The tar file to be untarred
     * @param destinationDir
     * The directory to untar into
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun untar(tarFile: File, destinationDir: File) {
        Log.d(TAG, "Untarring...")
        val uncompressedSize = getTarSizeUncompressed(tarFile)
        var percentComplete: Int?
        var percentCompleteLast = 0
        var unzippedBytes = 0
        val progressMin = 50
        val progressMax = 100 - progressMin
        publishProgress("Uncompressing data for $languageName...",
                progressMin.toString())

        // Extract all the files
        val tarInputStream = TarInputStream(BufferedInputStream(
                FileInputStream(tarFile)))
        var entry: TarEntry = tarInputStream.nextEntry
        while (entry != null) {
            val BUFFER = 8192
            val data = ByteArray(BUFFER)
            val pathName = entry.name
            val fileName = pathName.substring(pathName.lastIndexOf('/'), pathName.length)
            val outputStream = FileOutputStream(destinationDir.toString() + fileName)
            val bufferedOutputStream = BufferedOutputStream(outputStream)

            Log.d(TAG, "Writing " + fileName.substring(1, fileName.length) + "...")
            var len: Int = tarInputStream.read(data, 0, BUFFER)
            while (len != -1) {
                bufferedOutputStream.write(data, 0, len)
                unzippedBytes += len
                percentComplete = (unzippedBytes / uncompressedSize.toFloat() * progressMax).toInt() + progressMin
                if (percentComplete > percentCompleteLast) {
                    publishProgress("Uncompressing data for $languageName...",
                            percentComplete.toString())
                    percentCompleteLast = percentComplete
                }
                len = tarInputStream.read(data, 0, BUFFER)
            }
            bufferedOutputStream.flush()
            bufferedOutputStream.close()
            entry = tarInputStream.nextEntry
        }
        tarInputStream.close()

        if (tarFile.exists()) {
            tarFile.delete()
        }
    }

    /**
     * Return the uncompressed size for a Tar file.
     *
     * @param tarFile
     * The Tarred file
     * @return Size when uncompressed, in bytes
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getTarSizeUncompressed(tarFile: File): Int {
        var size = 0
        val tis = TarInputStream(BufferedInputStream(
                FileInputStream(tarFile)))
        var entry: TarEntry = tis.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) size += entry.size.toInt()
            entry = tis.nextEntry
        }
        tis.close()
        return size
    }

    /**
     * Install a file from application assets to device external storage.
     *
     * @param sourceFilename
     * File in assets to install
     * @param modelRoot
     * Directory on SD card to install the file to
     * @param destinationFile
     * File name for destination, excluding path
     * @return True if installZipFromAssets returns true
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun installFromAssets(sourceFilename: String, modelRoot: File,
                                  destinationFile: File): Boolean {
        val extension = sourceFilename.substring(sourceFilename.lastIndexOf('.'),
                sourceFilename.length)
        try {
            return if (extension == ".zip") {
                installZipFromAssets(sourceFilename, modelRoot, destinationFile)
            } else {
                throw IllegalArgumentException("Extension " + extension
                        + " is unsupported.")
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "Language not packaged in application assets.")
        }

        return false
    }

    /**
     * Unzip the given Zip file, located in application assets, into the given
     * destination file.
     *
     * @param sourceFilename
     * Name of the file in assets
     * @param destinationDir
     * Directory to save the destination file in
     * @param destinationFile
     * File to unzip into, excluding path
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Throws(IOException::class, FileNotFoundException::class)
    private fun installZipFromAssets(sourceFilename: String,
                                     destinationDir: File, destinationFile: File): Boolean {
        var destinationFile = destinationFile
        // Attempt to open the zip archive
        publishProgress("Uncompressing data for $languageName...", "0")
        val inputStream = ZipInputStream(context.assets.open(sourceFilename))

        // Loop through all the files and folders in the zip archive (but there should just be one)
        var entry: ZipEntry? = inputStream.nextEntry
        while (entry != null) {
            destinationFile = File(destinationDir, entry.name)

            if (entry.isDirectory) {
                destinationFile.mkdirs()
            } else {
                // Note getSize() returns -1 when the zipfile does not have the size set
                val zippedFileSize = entry.size

                // Create a file output stream
                val outputStream = FileOutputStream(destinationFile)
                val BUFFER = 8192

                // Buffer the output to the file
                val bufferedOutputStream = BufferedOutputStream(outputStream, BUFFER)
                var unzippedSize = 0

                // Write the contents
                var percentComplete: Int = 0
                var percentCompleteLast: Int = 0
                val data = ByteArray(BUFFER)
                var count = inputStream.read(data, 0, BUFFER)
                while (count != -1) {
                    bufferedOutputStream.write(data, 0, count)
                    unzippedSize += count
                    percentComplete = (unzippedSize / zippedFileSize * 100).toInt()
                    if (percentComplete > percentCompleteLast) {
                        publishProgress("Uncompressing data for $languageName...",
                                percentComplete.toString(), "0")
                        percentCompleteLast = percentComplete
                    }
                    count = inputStream.read(data, 0, BUFFER)
                }
                bufferedOutputStream.close()
            }
            inputStream.closeEntry()
            entry = inputStream
                    .nextEntry
        }
        inputStream.close()
        return true
    }

    /**
     * Update the dialog box with the latest incremental progress.
     *
     * @param message
     * [0] Text to be displayed
     * @param message
     * [1] Numeric value for the progress
     */
    override fun onProgressUpdate(vararg message: String) {
        super.onProgressUpdate(*message)
        var percentComplete = 0

        percentComplete = Integer.parseInt(message[1])
        dialog.setMessage(message[0])
        dialog.progress = percentComplete
        dialog.show()
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)

        try {
            indeterminateDialog.dismiss()
        } catch (e: IllegalArgumentException) {
            // Catch "View not attached to window manager" error, and continue
        }

        if (result!!) {
            // Restart recognition
            activity.resumeOCR()
            activity.showLanguageName()
        } else {
            activity.showErrorMessage("Error", "Network is unreachable - cannot download language data. " + "Please enable network access and restart this app.")
        }
    }

    companion object {
        private val TAG = OcrInitAsyncTask::class.java.simpleName

        /** Suffixes of required data files for Cube.  */
        private val CUBE_DATA_FILES = arrayOf(".cube.bigrams", ".cube.fold", ".cube.lm", ".cube.nn", ".cube.params",
                //".cube.size", // This file is not available for Hindi
                ".cube.word-freq", ".tesseract_cube.nn", ".traineddata")
    }
}