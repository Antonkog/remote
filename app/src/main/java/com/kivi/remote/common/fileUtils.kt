package com.kivi.remote.common

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.io.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.N)
fun getRoots(context: Context): List<Pair<String, File>> {
    val roots = arrayListOf<Pair<String, File>>()
    val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    val volumes = storageManager.storageVolumes
    for (volume in volumes) {
        val description = volume.getDescription(context)
        val path: File
        try {
            path = volume.javaClass.getMethod("getPathFile").invoke(volume) as File
        } catch (e: Throwable) {
            Timber.e(e)
            continue
        }

        Timber.d("Found volume $description ($path) in state ${volume.state}")
        if (Environment.MEDIA_MOUNTED == volume.state || Environment.MEDIA_MOUNTED_READ_ONLY == volume.state) {
            roots.add(Pair(description, path))
        }
    }
    return roots
}

fun String.appendSlash(): String =
        if (this.endsWith("/")) this else this + "/"

private val File.extension: String?
    get() {
        var path = absolutePath
        val filePos = path.lastIndexOf("/")
        if (filePos > 0) path = path.substring(filePos + 1)
        val dotPos = path.lastIndexOf(".")
        return if (dotPos >= 0) path.substring(dotPos + 1)
        else null
    }

private fun startsWithPath(first: File, second: File): Boolean =
        (first.absolutePath.appendSlash()).startsWith(second.absolutePath.appendSlash())


fun getLogFile(): File {
    return File(Environment.getExternalStorageDirectory().toString() + File.separator + Constants.LOG_FILE_PREFIX + Build.MODEL + Constants.LOG_FILE_EXTENSION)
}


fun appendLog(text: String) {
    Timber.i(text)
    System.out.println(text)
    val logFile = getLogFile()
    try {
        if (!logFile.exists()) {
            logFile.createNewFile()
        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        val buf = BufferedWriter(FileWriter(logFile, true))
        buf.append(text + " " + calendar.time.toString())
        buf.newLine()
        buf.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun extractLogToFile(context: Context): String {
    val manager: PackageManager = context.packageManager
    var info: PackageInfo? = null
    try {
        info = manager.getPackageInfo(context.packageName, 0)
    } catch (e2: PackageManager.NameNotFoundException) {
    }
    var model = Build.MODEL
    if (!model.startsWith(Build.MANUFACTURER)) model = Build.MANUFACTURER + " " + model
    // Make file name - file must be saved to external storage or it wont be readable by
// the email app.
    val path = Environment.getExternalStorageDirectory().toString() + "/" + "MyApp/"
    val fullName = path + "kivi_log"
    // Extract to file.
    val file = File(fullName)
    var reader: InputStreamReader? = null
    var writer: FileWriter? = null
    try { // For Android 4.0 and earlier, you will get all app's log output, so filter it to
// mostly limit it to your app's output.  In later versions, the filtering isn't needed.
        val cmd = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" else "logcat -d -v time"
        // get input stream
        val process = Runtime.getRuntime().exec(cmd)
        reader = InputStreamReader(process.inputStream)
        // write output stream
        writer = FileWriter(file)
        writer.write("Android version: " + Build.VERSION.SDK_INT + "\n")
        writer.write("Device: $model\n")
        writer.write("App version: " + (info?.versionCode ?: "(null)") + "\n")
        val buffer = CharArray(10000)
        do {
            val n = reader.read(buffer, 0, buffer.size)
            if (n == -1) break
            writer.write(buffer, 0, n)
        } while (true)
        reader.close()
        writer.close()
    } catch (e: IOException) {
        if (writer != null) try {
            writer.close()
        } catch (e1: IOException) {
        }
        if (reader != null) try {
            reader.close()
        } catch (e1: IOException) {
        }
        // You might want to write a failure message to the log here.
        return ""
    }
    return fullName
}
