package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.dto.User
import ru.netology.nework.util.loadCircleCrop

interface UserOnInteractionListener {
    fun onRemove(id: Long)
}

class UsersAdapter(
    private val listener: UserOnInteractionListener,
    ) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            CardUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return UserViewHolder(binding, listener)

    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        val user = getItem(position)
        holder.bind(user)
    }

}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val listener: UserOnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) {
            binding.apply {
                name.text = user.name
                val avatarUrl = user.avatar ?: ""
                avatar.loadCircleCrop(avatarUrl, R.drawable.ic_avatar)

                root.setOnLongClickListener {
                    listener.onRemove(user.id)
                    true
                }
            }
        }
}

class UserDiffCallback  : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        if (oldItem::class != newItem::class) return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

}