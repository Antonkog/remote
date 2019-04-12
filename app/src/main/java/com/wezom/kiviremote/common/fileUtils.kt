package com.wezom.kiviremote.common

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.support.annotation.RequiresApi
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
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
