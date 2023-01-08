package ru.netology.nework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.*
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Payload
import ru.netology.nework.util.*
import ru.netology.nework.viewmodel.UserViewModel

interface EventListener {
    fun onLikeListener(event: Event) {}
    fun onShareListener(event: Event) {}
    fun onRemoveListener(event: Event) {}
    fun onEditListener(event: Event) {}
    fun onFeedListener(event: Event) {}
    fun onHideListener(event: Event) {}
    fun onFullscreenAttachment(attachmentUrl: String) {}
    fun onMap(event: Event) {}
    fun onAuth()
}

class EventAdapter(
    private val listener: EventListener,
    private val appAuth: AppAuth,
    private val userViewModel: UserViewModel,
    private val lifecycleOwner: LifecycleOwner
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
        return EventViewHolder(binding, listener, appAuth, userViewModel, lifecycleOwner)

    }
}

class EventViewHolder(
        private val binding: CardEventBinding,
        private val listener: EventListener,
        private val appAuth: AppAuth,
        private val userViewModel: UserViewModel,
        private val lifecycleOwner: LifecycleOwner
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
                content.text = event.content
                published.text = event.published
                attachment.visibility = View.GONE
                type.text = event.type.toString()

                if (event.link!=null) {
                    link.isVisible = true
                    link.text = event.link
                }else { link.isVisible = false}

                if (event.coordinates != null) {
                    navigate.isVisible = true
                    navigate.setOnClickListener { listener.onMap(event)}
                }

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
                if (event.attachment != null) {
                    uploadingMedia(attachment, event.attachment.url)
                    attachment.isVisible = true
                    attachment.setOnClickListener {
                        listener.onFullscreenAttachment(event.attachment.url)
                    }
                } else {
                    attachment.isVisible = false
                }

                like.isChecked = event.likedByMe
                val likersList = event.likeOwnerIds ?: emptyList()

                val likersAdapter = UsersAdapter(object : UserOnInteractionListener {
                    override fun onRemove(id: Long) = Unit
                })
                likers.adapter = likersAdapter


                when (likersList.size) {
                    0 -> likers.isVisible = false
                    1,2,3 -> {
                        likers.isVisible = true
                        userViewModel.data.observe(lifecycleOwner) { users ->
                            val mentors = users.users.filter { it.id in event.likeOwnerIds }
                            likersAdapter.submitList(mentors)
                        }
                    }
                    else -> {
                        likers.isVisible = false
                        like.text = numbersToString(likersList.size)
                    }
                }


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

                val speakerslist = event.speakerIds ?: emptyList()
                listSpeakers.isVisible = speakerslist.isNotEmpty()
                speakersHeader.isVisible = speakerslist.isNotEmpty()

                val participantlist = event.participantsIds ?: emptyList()
                listParticipants.isVisible = participantlist.isNotEmpty()
                participantsHeader.isVisible = participantlist.isNotEmpty()

                val avatarUrl = event.authorAvatar ?: ""
                author.avatar.loadCircleCrop(avatarUrl, R.drawable.ic_avatar)

                val speakersAdapter = UsersAdapter(object : UserOnInteractionListener {
                    override fun onRemove(id: Long) = Unit
                })
                listSpeakers.adapter = speakersAdapter

                val participantsAdapter = UsersAdapter(object : UserOnInteractionListener {
                    override fun onRemove(id: Long) = Unit
                })
                listParticipants.adapter = participantsAdapter

                userViewModel.data.observe(lifecycleOwner) { users ->
                    val speakers = users.users.filter { it.id in event.speakerIds }
                    speakersAdapter.submitList(speakers)

                    val participants = users.users.filter { it.id in event.participantsIds }
                    participantsAdapter.submitList(participants)
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

