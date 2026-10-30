package cl.duoc.agromatch.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateFormattingTest {
    @Test
    fun isoDateFormatsAsLocalCalendarDate() {
        val epochDay = parseIsoDateToEpochDay("2026-10-29")!!

        assertEquals("29/10/2026", formatEpochDay(epochDay))
        assertEquals("29/10/2026", formatIsoDate("2026-10-29"))
    }

    @Test
    fun invalidIsoDateIsNotFormatted() {
        assertNull(parseIsoDateToEpochDay("2026-02-30"))
        assertNull(formatIsoDate("not-a-date"))
    }
}
