package ru.netology.nework.util

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.widget.EditText
import ru.netology.nework.enumeration.SeparatorTimeType
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

const val HOUR_PER_DAY = 24
const val MINUTE_PER_HOUR = 60
const val SEC_PER_MINUTE = 60
const val MS_PER_SEC = 1000

object CurrentTimes {

    val currentTime: Long
        get() = Calendar.getInstance().time.time

    fun differentHourFromCurrent(time: Long): Long =
        (currentTime - time * MS_PER_SEC) / (MINUTE_PER_HOUR * SEC_PER_MINUTE * MS_PER_SEC)

    fun getDaySeparatorType(time: Long?): SeparatorTimeType {
        if (time == null) {
            return SeparatorTimeType.NULL
        }
        val dif = differentHourFromCurrent(time)
        return when {
            dif in 0..23L -> SeparatorTimeType.TODAY
            dif in 24L..47L -> SeparatorTimeType.YESTERDAY
            dif >= 48L -> SeparatorTimeType.MORE_OLD
            else -> SeparatorTimeType.NULL
        }
    }

    @SuppressLint("NewApi")
    fun formatDateS(value: String): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.ROOT)
            .withZone(ZoneId.systemDefault())

        return formatter.format(Instant.parse(value))
    }

    fun formatDateL(value: Long?): String? {
        return if (value == null) null else DateFormat.format("yyyy-MM-dd", Date(value * 1000))
            .toString()
    }

    fun showDateDialog(editText: EditText, context: Context) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = monthOfYear
            calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            editText.setText(
                SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

                    .format(calendar.time)
            )
        }
        DatePickerDialog(
            context, datePicker,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
            .show()
    }
    fun showTimeDialog(editText: EditText, context: Context) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            editText.setText(
                SimpleDateFormat("HH:mm", Locale.ROOT)
                    .format(calendar.time)
            )
        }
        TimePickerDialog(
            context, timePicker,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), true
        )
            .show()
    }

    private const val THOU = 1000;
    private const val MILL = 1000000;

    fun formatNum(value: Int): String {
        if (value < THOU) return value.toString();
        if (value < MILL) return makeDecimal(value, THOU, "k");
        return makeDecimal(value, MILL, "m");
    }

    private fun makeDecimal(value: Int, div: Int, suffix: String): String {
        val vl = value / (div / 10)
        val whole = vl / 10
        val tenths = vl % 10
        if ((tenths == 0) || (whole >= 10)) {
            return String.format("%d%s", whole, suffix)
        }
        return String.format("%d.%d%s", whole, tenths, suffix)
    }

    fun formatDate(date: String): String {
        val instant = Instant.parse(date)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        return DateTimeFormatter
            .ofPattern("yyyy-MM-dd hh:mm")
            .format(dateTime)
    }

    fun formatDate(millis: Long): String {
        val instant = Instant.ofEpochMilli(millis)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        return DateTimeFormatter
            .ofPattern("yyyy-MM-dd hh:mm")
            .format(dateTime)
    }

    fun formatJustDate(millis: Long): String {
        val instant = Instant.ofEpochMilli(millis)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        return DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .format(dateTime)
    }

    fun formatJustTime(millis: Long): String {
        val instant = Instant.ofEpochMilli(millis)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        return DateTimeFormatter
            .ofPattern("hh:mm")
            .format(dateTime)
    }

    fun dateToEpochSec(str: String?): Long? {
        return if (str.isNullOrBlank()) null else LocalDate.parse(str)
            .atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond()
    }
}