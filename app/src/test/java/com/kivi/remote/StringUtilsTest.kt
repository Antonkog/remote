package com.kivi.remote

import com.kivi.remote.common.extensions.formatDuration
import com.kivi.remote.common.extensions.removeMasks
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
        assertEquals("KIVI TV", "KIVI TV [49790](KIVI_TV)".removeMasks())
        assertEquals("Домашний кинотеатр", "Домашний кинотеатр [49790](KIVI_TV)".removeMasks())
        assertEquals("Sleeping room", "Sleeping room [49790](KIVI_TV)".removeMasks())
        assertEquals("MyHome01", "MyHome01 [49790](KIVI_TV)".removeMasks())
        assertEquals("Телевизор в большой комнате", "Телевизор в большой комнате [49790](KIVI_TV)".removeMasks())
        assertEquals("KIVI 50UK32G", "50UK32G [2c73](KIVI_TV)".removeMasks())
        assertEquals("KIVI 49UP30g", "49UP30g [ewgge](KIVI_TV)".removeMasks())
        assertEquals("KIVI 24fk30g", "24fk30g [ewgge](KIVI_TV)".removeMasks())
        assertEquals("KIVI 24HK30G_-Ver01", "24HK30G_-Ver01 [ewgge](KIVI_TV)".removeMasks())
        assertEquals("KIVI 24HK30G_-Ver01 new name", "24HK30G_-Ver01 new name [ewgge](KIVI_TV)".removeMasks())
        assertEquals("KIVI 49UP30g", "49UP30g [ewgge](KIVI_TV)".removeMasks())
        assertEquals("Прихожая", "Прихожая [49790](KIVI_TV)".removeMasks())
        assertEquals("InnoTest 42SFXHAS", "InnoTest 42SFXHAS [49790](KIVI_TV)".removeMasks())
        assertEquals("MStar Semiconductor Inc. 42SFXHAS", "MStar Semiconductor Inc. 42SFXHAS [49790](KIVI_TV)".removeMasks())
    }
}