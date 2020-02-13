@file:JvmName("StringUtils")

package com.kivi.remote.common.extensions

import java.util.concurrent.TimeUnit
import java.util.regex.Pattern




//private val mStar = "MStar Semiconductor, Inc."
//private val lge = "LGE"
//fun String.replaceLGE(): String {
//    if (this.startsWith(lge) && this.indexOfFirst { it == '[' } != -1)
//        return this.replace(lge, "KIVI").substring(0, this.indexOfFirst { it == '[' })
//    if (this.indexOfFirst { it == '[' } != -1)
//        return this.substring(0, this.indexOfFirst { it == '[' })
//    return this
//}
//
//fun String.replaceMStar(): String {
//    if (this.startsWith(mStar))
//        return this.replace(mStar, "KIVI")
//    return this
//}

fun String.addKiviPrefix(): String {
    val KIVI_PREFIX = "KIVI"
    val re1 = "(\\d+)"
    val re2 = "((?:[a-z][a-z]*[0-9]+[a-z0-9]*))"

    val p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    val m = p.matcher(this)
    if (m.find()) {
        return if (contains(KIVI_PREFIX, true))
            this
        else KIVI_PREFIX + " " + this
    }
    return this
}

fun String.getIviPreviewDuration(): String {
    this.toLongOrNull()?.let {
       return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(it),
                TimeUnit.MILLISECONDS.toMinutes(it) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(it)),
                TimeUnit.MILLISECONDS.toSeconds(it) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(it)))

//        return SimpleDateFormat("hh:mm:ss").format(Date(it).toLocaleString())
    } ?: return ""
}

fun String.getModelName(): String = this.substringBefore(" [").addKiviPrefix()

fun String.remove032Space(): String = this.replace("\\032", " ", true).replace("\\03", "", true)

fun String.getTvUniqueId(): String = this.substringAfter(" [").substringBefore("]")

fun String.removeMasks(): String = this.remove032Space().getModelName()

fun String.formatDuration(): String {
    var endResult: String = this

    if (this.substringBeforeLast(":").substringAfter(":").length < 2) {
        endResult = this.substringBefore(":") + ":" + "0" + this.substringAfter(":")
    }

    if (this.substringBefore(":").length < 2)
        endResult = "0" + endResult

    if (this.substringAfterLast(":").length < 2)
        endResult = endResult.substringBeforeLast(":") + ":" + "0" + endResult.substringAfterLast(":")

    return endResult
}

fun String.substringBeforeLastKt(delimiter: String): String = substringBeforeLast(delimiter)

fun String.substringAfterLastKt(delimiter: String): String = substringAfterLast(delimiter)

//fun String.isEqualPath(path: String) = substring

