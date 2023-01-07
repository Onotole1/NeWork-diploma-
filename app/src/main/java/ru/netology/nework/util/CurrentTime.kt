package ru.netology.nework.hiltModules

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.enumeration.SeparatorTimeType
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

const val HOUR_PER_DAY = 24
const val MINUTE_PER_HOUR = 60
const val SEC_PER_MINUTE = 60
const val MS_PER_SEC = 1000

@Singleton
class CurrentTime @Inject constructor() {

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
    fun formatDate(value: Long?): String? {
        return if (value == null) null else DateFormat.format("yyyy-MM-dd", Date(value * 1000))
            .toString()
    }
    @SuppressLint("NewApi")
    fun formatDate(value: String): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.ROOT)
            .withZone(ZoneId.systemDefault())

        return formatter.format(Instant.parse(value))
    }
    fun uploadingAvatar(view: ImageView, avatar: String?) {
        Glide.with(view)
            .load(avatar)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar)
            .timeout(10_000)
            .into(view)
    }
    fun uploadingMedia(view: ImageView, url: String?) {
        Glide.with(view)
            .load(url)
            .timeout(10_000)
            .into(view)
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
}