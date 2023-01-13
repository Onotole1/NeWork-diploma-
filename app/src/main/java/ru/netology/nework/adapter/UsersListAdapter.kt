package ru.netology.nework.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import ru.netology.nework.R
import ru.netology.nework.dto.User
import ru.netology.nework.util.loadCircleCrop

class UsersListAdapter constructor(
    private val users: List<User>,
    private val context: Context,
) : BaseAdapter() {
    override fun getCount() = users.size

    override fun getItem(item: Int): User = users[item]

    override fun getItemId(item: Int): Long = item.toLong()

    override fun getView(item: Int, view: View?, viewGroup: ViewGroup?): View {

        val listView = when (view == null) {
            true -> LayoutInflater.from(context).inflate(R.layout.fragment_check_users, viewGroup, false)
            else -> view
        }

        val user = users[item]
        val name = listView.findViewById<TextView>(R.id.name)
        val avatar = listView.findViewById<ImageView>(R.id.avatar)

        name.text = user.name
        user.avatar?.let { avatar.loadCircleCrop(it, R.drawable.ic_avatar) }


        return listView
    }
}