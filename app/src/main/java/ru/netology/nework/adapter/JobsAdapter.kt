package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.CardJobBinding
import ru.netology.nework.dto.Job


interface JobOnInteractionListener  {
    fun onEditListener(job: Job){}
    fun onRemoveListener(job: Job){}
    fun onHideListener(job: Job){}
    fun onAuth()
}
abstract class JobsAdapter (
    private val listener: JobOnInteractionListener,
    private val appAuth: AppAuth
    ) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding =
            CardJobBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return JobViewHolder(binding, listener, appAuth)
    }
    override fun onBindViewHolder(
        holder: JobViewHolder,
        position: Int
    ) {
        val job = getItem(position)
        holder.bind(job)
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val listener: JobOnInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {
        binding.apply {
            period.text = job.getString()
            nameOfCompany.text = job.name
            position.text = job.position
            if (job.link!=null) {
                link.isVisible = true
                link.text = job.link
            }else { link.isVisible = false}

            menu.visibility = if (job.ownedByMe) View.VISIBLE else View.INVISIBLE
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.object_options)
                    menu.setGroupVisible(R.id.my_object_menu, job.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.object_remove -> {
                                listener.onRemoveListener(job)
                                true
                            }
                            R.id.object_edit -> {
                                listener.onEditListener(job)
                                true
                            }
                            R.id.object_hide -> {
                                listener.onHideListener(job)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}


class JobDiffCallback: DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}


