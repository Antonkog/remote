package com.wezom.kiviremote

import com.wezom.kiviremote.common.extensions.formatDuration
import com.wezom.kiviremote.common.extensions.removeMasks
import junit.framework.Assert.assertEquals
import org.junit.Test


internal class StringUtilsUnitTest {
    @Test
    fun durationFormatTest() {
        // Seconds
        assertEquals("10:09:05", "10:9:5".formatDuration())
        assertEquals("10:09:55", "10:9:55".formatDuration())

        // Minutes
        assertEquals("00:01:55", "0:1:55".formatDuration())
        assertEquals("00:10:55", "0:10:55".formatDuration())

        // Hours
        assertEquals("01:09:55", "1:9:55".formatDuration())
        assertEquals("10:09:55", "10:9:55".formatDuration())
    }

    @Test
    fun deviceFriendlyNameTest() {
        assertEquals("KIVI 42SFXHAS", "LGE 42SFXHAS [49790](KIVI_TV)".removeMasks())
        assertEquals("KIVI 42SFXHAS", "Samsung 42SFXHAS [49790](KIVI_TV)".removeMasks())
        assertEquals("KIVI 42SFXHAS", "InnoTest 42SFXHAS [49790](KIVI_TV)".removeMasks())
        assertEquals("KIVI 42SFXHAS", "MStar Semiconductor Inc. 42SFXHAS [49790](KIVI_TV)".removeMasks())
        assertEquals("KIVI 42SFXHAS", "MStar\\032semiconductor\\032Inc.\\03242SFXHAS\\032[49790](KIVI_TV)".removeMasks())
    }
}