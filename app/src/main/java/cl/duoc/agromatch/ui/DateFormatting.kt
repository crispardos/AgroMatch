package cl.duoc.agromatch.ui

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val MILLIS_PER_DAY = 86_400_000L
private val utcTimeZone = TimeZone.getTimeZone("UTC")

fun formatEpochDay(epochDay: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL")).apply {
        timeZone = utcTimeZone
    }.format(Date(epochDay * MILLIS_PER_DAY))

fun formatIsoDate(isoDate: String?): String? {
    val epochDay = parseIsoDateToEpochDay(isoDate) ?: return null
    return formatEpochDay(epochDay)
}

fun parseIsoDateToEpochDay(isoDate: String?): Long? {
    if (isoDate == null) return null
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
        timeZone = utcTimeZone
    }
    val position = ParsePosition(0)
    val parsed = parser.parse(isoDate, position)?.takeIf { position.index == isoDate.length } ?: return null
    return parsed.time / MILLIS_PER_DAY
}

fun epochDayFromPickerDate(year: Int, month: Int, dayOfMonth: Int): Long {
    val calendar = Calendar.getInstance(utcTimeZone).apply {
        clear()
        set(year, month, dayOfMonth)
    }
    return calendar.timeInMillis / MILLIS_PER_DAY
}

fun isoDateFromPickerDate(year: Int, month: Int, dayOfMonth: Int): String =
    String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
