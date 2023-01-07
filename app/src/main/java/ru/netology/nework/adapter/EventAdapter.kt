package ru.netology.nework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.BuildConfig.BASE_URL
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.*
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Payload
import ru.netology.nework.util.numbersToString

interface EventListener {
    fun onLikeListener(event: Event) {}
    fun onShareListener(event: Event) {}
    fun onRemoveListener(event: Event) {}
    fun onEditListener(event: Event) {}
    fun onFeedListener(event: Event) {}
    fun onHideListener(event: Event) {}
    fun onPlaceWorkListener(event: Event) {}
    fun onMentorsListener(event: Event) {}
    fun onFullscreenAttachment(attachmentUrl: String) {}
    fun onLikeOwnerListener(event: Event) {}
    fun onMap(event: Event) {}
    fun onAuth()
}

class EventAdapter(
    private val listener: EventListener,
    private val appAuth: AppAuth,
) : PagingDataAdapter<Event, RecyclerView.ViewHolder>(EventDiffCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val event = getItem(position)
        if (holder is EventViewHolder) {
            event?.let { holder.bind(it) }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding =
            CardEventBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return EventViewHolder(binding, listener, appAuth)

    }
}


    class EventViewHolder(
        private val binding: CardEventBinding,
        private val listener: EventListener,
        private val appAuth: AppAuth,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(payload: Payload) {
            payload.liked?.also { liked ->

                binding.like.setIconResource(
                    if (liked) R.drawable.ic_liked_24 else R.drawable.ic_like_24
                )
                if (liked) {
                    ObjectAnimator.ofPropertyValuesHolder(
                        binding.like,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F, 1.0F, 1.2F),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F, 1.0F, 1.2F)
                    ).start()
                } else {
                    ObjectAnimator.ofFloat(
                        binding.like,
                        View.ROTATION,
                        0F, 360F
                    ).start()
                }
            }

            payload.content?.let(binding.content::setText)
        }

        fun bind(event: Event) {
            binding.apply {

                author.text = event.author
                published.text = event.published.toString()
                content.text = event.content
                attachment.visibility = View.GONE

                menu.visibility = if (event.ownedByMe) View.VISIBLE else View.INVISIBLE
                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.object_options)
                        menu.setGroupVisible(R.id.my_object_menu, event.ownedByMe)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.object_remove -> {
                                    listener.onRemoveListener(event)
                                    true
                                }
                                R.id.object_edit -> {
                                    listener.onEditListener(event)
                                    true
                                }
                                R.id.object_hide -> {
                                    listener.onHideListener(event)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }

                val url = "${BASE_URL}/avatars/${event.authorAvatar}"
                if (event.authorAvatar != null) {
                    Glide.with(itemView)
                        .load(url)
                        .placeholder(R.drawable.ic_loading_24)
                        .error(R.drawable.ic_baseline_error_outline_24)
                        .timeout(10_000)
                        .circleCrop()
                        .into(avatar)
                    avatar.setOnClickListener {
                        listener.onFullscreenAttachment(url)
                    }
                }


                val urlAttachment = "${BASE_URL}/media/${event.attachment?.url}"
                if (event.attachment != null) {
                    Glide.with(attachment.context)
                        .load(urlAttachment)
                        .placeholder(R.drawable.ic_loading_24)
                        .error(R.drawable.ic_baseline_error_outline_24)
                        .timeout(10_000)
                        .into(attachment)
                    attachment.isVisible = true
                    attachment.setOnClickListener {
                        listener.onFullscreenAttachment(urlAttachment)
                    }
                } else {
                    attachment.isVisible = false
                }

                like.isChecked = event.likedByMe
                likeCnt.text = numbersToString(event.likeOwnerIds.size)


                like.setOnClickListener {
                    if (appAuth.authStateFlow.value.id == 0L) {
                        listener.onAuth()
                        return@setOnClickListener
                    }
                    listener.onLikeListener(event)
                }
                share.setOnClickListener {
                    listener.onShareListener(event)
                }
                thisEvent.setOnClickListener { listener.onFeedListener(event) }

                placeWork.setOnClickListener {
                    listener.onPlaceWorkListener(event)
                }

                mentors.setOnClickListener {
                    listener.onMentorsListener(event)
                }

                attachment.setOnClickListener {
                    event.attachment?.let { attach ->
                        listener.onFullscreenAttachment(attach.url)
                    }

                }
            }
        }
    }


class EventDiffCallback : DiffUtil.ItemCallback<Event>() {

    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Event, newItem: Event): Any {
        if (oldItem::class != newItem::class) {
            return false
        }else{
            return  Payload(
                liked = newItem.likedByMe.takeIf { oldItem.likedByMe != it },
                content = newItem.content.takeIf { oldItem.content != it },
            )
        }
        }
}

