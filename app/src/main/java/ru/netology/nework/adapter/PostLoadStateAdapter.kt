package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.LoadStateBinding


class PostLoadStateAdapter(
    private val retryListener: () -> Unit,
) : LoadStateAdapter<LoadStateViewHolder>() {

    interface OnInteractionListener {
        fun onRetry() {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder =
        LoadStateViewHolder(
            LoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            retryListener
        )

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
}

class LoadStateViewHolder(
    private val binding: LoadStateBinding,
    private val retryListener:() -> Unit,
) : RecyclerView.ViewHolder(binding.root)
{
    fun bind(loadState: LoadState) {
        binding.apply {
            progress.isVisible = loadState is LoadState.Loading
            retry.isVisible = loadState is LoadState.Error

            retry.setOnClickListener {
                retryListener()
            }
        }
    }
}
