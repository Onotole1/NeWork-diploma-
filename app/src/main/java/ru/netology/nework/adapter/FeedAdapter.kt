package ru.netology.nework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardAdBinding
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.databinding.CardTextItemSeparatorBinding
import ru.netology.nework.dto.*
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.util.*
import ru.netology.nework.viewmodel.UserViewModel


interface OnPostInteractionListener {
    fun onLikeListener(post: Post) {}
    fun onShareListener(post: Post) {}
    fun onRemoveListener(post: Post) {}
    fun onEditListener(post: Post) {}
    fun onFeedListener(feed: FeedItem) {}
    fun onHideListener(post: Post) {}
    fun onPlaceWorkListener(post: Post){}
    fun onMentorsListener(post: Post) {}
    fun onFullscreenAttachment(attachmentUrl: String) {}
    fun onLikeOwnerListener(post: Post) {}
    fun onPlayVideo(post: Post)
    fun onPlayAudio(post: Post)
    fun onMap(post: Post)
  //  fun onAuth()
}

class FeedAdapter(
    private val listener: OnPostInteractionListener,
//    private val appAuth: AppAuth,
    private val userViewModel: UserViewModel,
    private val lifecycleOwner: LifecycleOwner
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            is TextItemSeparator -> R.layout.card_text_item_separator
            else -> error("unknow item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding,
                    listener,
                    //appAuth,
                    userViewModel,
                    lifecycleOwner)
            }
            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding, listener)
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

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is TextItemSeparator -> ((holder as? TextItemViewHolder)?.bind(item))
            else -> error("unknow item type")
        }
        if (holder is PostViewHolder) {
            payloads.forEach {
                if (it is Payload) {
                    holder.bind(it)
                }
            }

            onBindViewHolder(holder, position)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is TextItemSeparator -> ((holder as? TextItemViewHolder)?.bind(item))
            else -> error("unknow item type")
        }
    }
}

class TextItemViewHolder(
    private val binding: CardTextItemSeparatorBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(textItemSeparator: TextItemSeparator) {
        binding.text.text = textItemSeparator.text
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
    private val listener: OnPostInteractionListener,

    ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        uploadingMedia(binding.imageAd, ad.name)
        binding.imageAd.setOnClickListener {
            listener.onFeedListener(ad)
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val listener: OnPostInteractionListener,
  //  private val appAuth: AppAuth,
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

    fun bind(post: Post) {
        binding.apply {

            author.text = post.author
            published.text = post.published
            content.text = post.content

            if (post.coordinates != null) {
                coordinates.isVisible = true
                coordinates.setOnClickListener { listener.onMap(post)}
            }

            menu.isVisible  = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.object_options)
                    menu.setGroupVisible(R.id.my_object_menu, post.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.object_remove -> {
                                listener.onRemoveListener(post)
                                true
                            }
                            R.id.object_edit -> {
                                listener.onEditListener(post)
                                true
                            }
                            R.id.object_hide -> {
                                listener.onHideListener(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            if (post.authorAvatar != null) {
                uploadingAvatar(avatar, post.authorAvatar)
            }

            imageAttachment.visibility =
                if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE)
                    View.VISIBLE else View.GONE
            audioPlay.visibility =
                if (post.attachment != null && post.attachment.type == AttachmentType.AUDIO)
                    View.VISIBLE else View.GONE
            videoPlay.visibility =
                if (post.attachment != null && post.attachment.type == AttachmentType.VIDEO)
                    View.VISIBLE else View.GONE

            imageAttachment.setOnClickListener {
                    post.attachment?.let { listener.onFullscreenAttachment(it.uri) }
            }
            audioPlay.setOnClickListener {
                listener.onPlayAudio(post)
            }

            videoPlay.setOnClickListener {
                listener.onPlayVideo(post)
            }

            val usersAdapter = UsersAdapter(object : UserOnInteractionListener {
                override fun onSingleUser(user: User) {}
            })

            val mentorslist = post.mentorsNames ?: emptyList()
            mentors.isVisible = mentorslist.isNotEmpty()
            mentorsEdit.isVisible = mentorslist.isNotEmpty()
            if (mentorslist.isNotEmpty()) {
                listener.onMentorsListener(post)
                mentorsEdit.adapter = usersAdapter
                userViewModel.data.observe(lifecycleOwner) { users ->
                    val mentors = users.users.filter { it.id in post.mentionIds }
                    usersAdapter.submitList(mentors)
                }

            }

            val likersList = post.likeOwnerIds

            like.isChecked = post.likedByMe
            likers.text = numbersToString(post.likeOwnerIds.size)
            when (likersList.size){
                0 -> likers.isVisible = false
                1 -> {
                    likers.isVisible = false
                    first.isVisible = true
                    val firsts = likersList.first()
                    val firstsAvatar = userViewModel.getUserAvatar(firsts)
                    uploadingAvatar(first,firstsAvatar)
                }
                2 -> {
                    likers.isVisible = false
                    first.isVisible = true
                    val firsts = likersList.first()
                    val firstsAvatar = userViewModel.getUserAvatar(firsts)
                    uploadingAvatar(first,firstsAvatar)
                    second.isVisible = true
                    val seconds = likersList.first()
                    val secondsAvatar = userViewModel.getUserAvatar(seconds)
                    uploadingAvatar(second,secondsAvatar)
                }
                else -> {   likers.isVisible = true
                            first.isVisible = false
                            second.isVisible = false}
             }

            first.setOnClickListener {
                listener.onLikeOwnerListener(post)
            }
            second.setOnClickListener {
                listener.onLikeOwnerListener(post)
            }

            like.setOnClickListener {
           //   if (appAuth.authStateFlow.value.id == 0L) {
             //       listener.onAuth()
            //        return@setOnClickListener
              //  }
                listener.onLikeListener(post)
            }
            share.setOnClickListener {
                listener.onShareListener(post)
            }
            thisPost.setOnClickListener { listener.onFeedListener(post) }

            val jobslist = post.jobs ?: emptyList()
            placeWork.setOnClickListener {
                listener.onPlaceWorkListener(post)
            }
            placeWork.text = if (jobslist.isNotEmpty()) {
                jobslist.first()
            } else {
                content.context.getString(
                    R.string.work
                )
            }

            if (post.link!=null) {
                link.isVisible = true
                link.text = post.link
            }else { link.isVisible = false}
            }
        }
    }


class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: FeedItem, newItem: FeedItem): Any {
        return if (oldItem::class != newItem::class) {
        } else if (oldItem is Post && newItem is Post) {
            Payload(
                liked = newItem.likedByMe.takeIf { oldItem.likedByMe != it },
                content = newItem.content.takeIf { oldItem.content != it },
            )
        } else {

        }
    }
}

