package ru.netology.nework.util


import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nework.R

const val PAGE_SIZE = 8
const val ENABLE_PLACE_HOLDERS = false

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


