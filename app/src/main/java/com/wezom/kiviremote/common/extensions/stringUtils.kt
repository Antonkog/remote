@file:JvmName("StringUtils")

package com.wezom.kiviremote.common.extensions

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

fun String.getModelName(): String = "KIVI " + this.substringBefore(" [").substringAfterLast(" ")

fun String.remove032Space(): String = this.replace("\\032", " ", true).replace("\\03", "", true)

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

