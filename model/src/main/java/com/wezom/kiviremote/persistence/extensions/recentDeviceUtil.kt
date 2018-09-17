@file:JvmName("RecentDeviceUtil")

package com.wezom.kiviremote.persistence.extensions

import com.wezom.kiviremote.persistence.model.RecentDevice

private fun String.removeKiviMask(): String {
    val lgMask = "LGE"
    if (this.startsWith(lgMask) && this.indexOfFirst { it == '[' } != -1)
        return this.replace(lgMask, "KIVI").substring(0, this.indexOfFirst { it == '[' })
    if (this.indexOfFirst { it == '[' } != -1)
        return this.substring(0, this.indexOfFirst { it == '[' })
    return this
}

private fun String.removeMStarMask(): String {
    val mStarMask = "MStar Semiconductor, Inc."
    if (this.startsWith(mStarMask))
        return this.replace(mStarMask, "KIVI")
    return this
}

/**
 * Seriously, wtf is wrong with Pre-Lollipop Android?
 *
 * @return actual name without \032
 */
private fun String.remove032Space(): String = this.replace("\\032", " ", true).replace("\\03", "", true)

private fun String.removeMasks(): String = this.removeKiviMask().removeMStarMask().remove032Space()

val RecentDevice.actualNameWithout032 : String
    get() = actualName.remove032Space()

val RecentDevice.actualNameWithoutMasks : String
    get() = actualName.removeMasks()