package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardAdBinding
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.databinding.CardTextItemSeparatorBinding
import ru.netology.nework.dto.*
import ru.netology.nework.viewmodel.UserViewModel


class WallAdapter(
        private val postlistener: OnPostInteractionListener,
        private val eventlistener: EventListener,
        private val userViewModel: UserViewModel,
        private val lifecycleOwner: LifecycleOwner
    ) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(WallDiffCallback()) {

        override fun getItemViewType(position: Int): Int =
            when (getItem(position)) {
                is Ad -> R.layout.card_ad
                is Post -> R.layout.card_post
                is Event -> R.layout.card_event
                is TextItemSeparator -> R.layout.card_text_item_separator
                else -> error("unknow item type")
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                R.layout.card_post -> {
                    val binding =
                        CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    PostViewHolder(binding,
                        postlistener,
                        userViewModel,
                        lifecycleOwner)
                }
                R.layout.card_ad -> {
                    val binding =
                        CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    AdViewHolder(binding, postlistener)
                }
                R.layout.card_event -> {
                    val binding =
                        CardEventBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    EventViewHolder(binding, eventlistener, userViewModel, lifecycleOwner)
                }
                R.layout.card_text_item_separator -> {
                    val binding =
                        CardTextItemSeparatorBinding.inflate(
                            LayoutInflater.from(parent.context),
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
                is Event -> (holder as? EventViewHolder)?.bind(item)
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


    class WallDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
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
            } else if (oldItem is Event && newItem is Event) {
                Payload(
                    liked = newItem.likedByMe.takeIf { oldItem.likedByMe != it },
                    content = newItem.content.takeIf { oldItem.content != it },
                )
            } else {

            }
        }
    }
