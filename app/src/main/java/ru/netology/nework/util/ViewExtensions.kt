package ru.netology.nework.util

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nework.R
import ru.netology.nework.api.ApiService
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

fun ImageView.load(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .transform(*transforms)
        .into(this)

fun ImageView.loadCircleCrop(url: String, placeholderId: Int) =
    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .transform(CircleCrop())
        .placeholder(placeholderId)
        .into(this)

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
@SuppressLint("NewApi")
fun listToString(list: List<String?>): String {
    return list.stream()
        .map { n -> java.lang.String.valueOf(n) }
        .collect(Collectors.joining(", ", "", ""))
}
