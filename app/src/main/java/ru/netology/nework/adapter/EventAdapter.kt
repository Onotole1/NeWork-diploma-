package ru.netology.nework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.*
import ru.netology.nework.dto.*
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.util.*
import ru.netology.nework.viewmodel.UserViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

interface EventListener {
    fun onLikeListener(event: Event) {}
    fun onShareListener(event: Event) {}
    fun onRemoveListener(event: Event) {}
    fun onEditListener(event: Event) {}
    fun onFeedListener(feed: FeedItem) {}
    fun onHideListener(event: Event) {}
    fun onFullscreenAttachment(attachmentUrl: String) {}
    fun onMap(event: Event) {}
    fun onJoin(event: Event) {}
    fun onOpenParticipants(event: Event)
    fun onOpenLikeOwners(event: Event)
    fun onOpenSpeakers(event: Event)
}

class EventAdapter(
    private val listener: EventListener,
    private val userViewModel: UserViewModel,
    private val lifecycleOwner: LifecycleOwner
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(EventDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Event -> R.layout.card_event
            is TextItemSeparator -> R.layout.card_text_item_separator
            else -> error("unknow item type")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Event -> (holder as? EventViewHolder)?.bind(item)
            is TextItemSeparator -> ((holder as? TextItemViewHolder)?.bind(item))
            else -> error("unknow item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_event -> {
                val binding =
                    CardEventBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                EventViewHolder(binding, listener, userViewModel, lifecycleOwner)
                 }
            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    AdEventViewHolder(binding, listener)
                }
            R.layout.card_text_item_separator -> {
                val binding =
                    CardTextItemSeparatorBinding.inflate(LayoutInflater.from(parent.context),
                        parent,
                        false)
                TextItemViewHolder(binding)
            }
            else -> error("unknow item type $viewType")
        }
}
class AdEventViewHolder(
    private val binding: CardAdBinding,
    private val listener: EventListener,

    ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        uploadingMedia(binding.imageAd, ad.name)
        binding.imageAd.setOnClickListener {
            listener.onFeedListener(ad)
        }
    }
}
class EventViewHolder(
        private val binding: CardEventBinding,
        private val listener: EventListener,
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
                when (event.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        imageAttachment.visibility = View.VISIBLE
                        uploadingAvatar(imageAttachment,event.attachment.uri)
                        imageAttachment.setOnClickListener {
                            listener.onFullscreenAttachment(event.attachment.uri)
                        }
                    }
                    AttachmentType.AUDIO -> {
                        audioPlay.visibility = View.VISIBLE
                        audioPlay.setOnClickListener {
                            listener.onFullscreenAttachment(event.attachment.uri)
                        }
                    }
                    AttachmentType.VIDEO -> {
                        videoPlay.visibility = View.VISIBLE
                        videoPlay.setOnClickListener {
                            listener.onFullscreenAttachment(event.attachment.uri)
                        }
                    }
                    else -> {videoPlay.visibility = View.GONE
                    videoPlay.visibility = View.GONE
                    imageAttachment.visibility = View.GONE}
                }
                type.text = event.type.toString()

                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withZone( ZoneId.systemDefault() )

                val date = Instant.parse(event.datetime).epochSecond
                val now = Instant.now().epochSecond
                val diff = (date-now)
                val hours = (diff / 3600L)
                val days = (hours / 24L)
                datetime.text = formatter.format(Instant.parse(event.datetime))
                datetimego.text = "$days   "+(R.string.days)

                if (event.link!=null) {
                    link.isVisible = true
                    link.text = event.link
                }else { link.isVisible = false}

                if (event.coordinates != null) {
                    navigate.isVisible = true
                    navigate.setOnClickListener { listener.onMap(event)}
                }
                join.apply {
                    when (event.participatedByMe) {
                        true -> {
                            this.icon =
                                AppCompatResources.getDrawable(root.context, R.drawable.ic_close)
                            this.text = root.context.getString(R.string.join_button_text_2)
                        }
                        false -> {
                            this.icon =
                                AppCompatResources.getDrawable(root.context, R.drawable.ic_check)
                            this.text = root.context.getString(R.string.join_button_text)
                        }
                    }
                }
                join.setOnClickListener {
                    listener.onJoin(event)
                }
                menu.isVisible  = event.ownedByMe
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

                like.isChecked = event.likedByMe
                val likersList = event.likeOwnerIds ?: emptyList()

                val usersAdapter = UsersAdapter(object : UserOnInteractionListener {
                    override fun onSingleUser(user: User) {}
                })

                likers.adapter = usersAdapter

                when (likersList.size) {
                    0 -> likers.isVisible = false
                    1,2,3 -> {
                        likers.isVisible = true
                        userViewModel.data.observe(lifecycleOwner) { users ->
                            val mentors = users.users.filter { it.id in event.likeOwnerIds }
                            usersAdapter.submitList(mentors)
                        }
                    }
                    else -> {
                        likers.isVisible = false
                        like.text = numbersToString(likersList.size)
                    }
                }

                like.setOnClickListener {
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

                binding.listSpeakers.setOnClickListener {
                    listener.onOpenSpeakers(event)
                }

                binding.likers.setOnClickListener {
                    listener.onOpenLikeOwners(event)
                }

                binding.listParticipants.setOnClickListener {
                    listener.onOpenParticipants(event)
                }

                likers.adapter = usersAdapter

                listSpeakers.adapter = usersAdapter

                listParticipants.adapter = usersAdapter

                userViewModel.data.observe(lifecycleOwner) { users ->
                    val speakers = users.users.filter { it.id in event.speakerIds }
                    usersAdapter.submitList(speakers)

                    val participants = users.users.filter { it.id in event.participantsIds }
                    usersAdapter.submitList(participants)
                }
            }
        }
    }


class EventDiffCallback : DiffUtil.ItemCallback<FeedItem>() {

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: FeedItem, newItem: FeedItem): Any {
        return  if (oldItem::class != newItem::class) {
          } else if (oldItem is Event && newItem is Event) {
            Payload(
                liked = newItem.likedByMe.takeIf { oldItem.likedByMe != it },
                content = newItem.content.takeIf { oldItem.content != it },
            )
        } else {
        }
    }
}

