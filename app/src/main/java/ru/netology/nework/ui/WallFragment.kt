package ru.netology.nework.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapter.*
import ru.netology.nework.databinding.FragmentWallBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.UserViewModel


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WallFragment : Fragment() {

    private val postViewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentWallBinding.inflate(
            inflater, container, false
        )

        val adapter = WallAdapter(
            object : OnPostInteractionListener {
            override fun onRemoveListener(post: Post) {
                postViewModel.removeById(post.id)
            }

            override fun onEditListener(post: Post) {
                postViewModel.edit(post)
                val bundle = Bundle().apply { putString("content", post.content) }
                bundle.putString("attachment", post.attachment?.uri)
                bundle.putString("attachmentType", post.attachment?.type?.name.toString())
                findNavController().navigate(R.id.action_navigation_main_to_newPostFragment, bundle)
            }

            override fun onLikeListener(post: Post) {
                if (authViewModel.authenticated) {
                   postViewModel.likeById(post.id, post.likedByMe)
                } else {
                    Toast.makeText(
                        activity,
                        R.string.error_auth,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onMentorsListener(post: Post) {
                userViewModel.getUsersIds(post.mentionIds)
                findNavController().navigate(R.id.action_navigation_main_to_navigation_users)
            }

            override fun onLikeOwnerListener(post: Post) {
                userViewModel.getUsersIds(post.likeOwnerIds)
                findNavController().navigate(R.id.action_navigation_main_to_navigation_users)
            }

            override fun onMap(post: Post) {
                val bundle = Bundle().apply {
                    post.coordinates?.latitude?.let { putDouble("lat", it) }
                    post.coordinates?.longitude?.let { putDouble("lng", it) }
                }
                findNavController().navigate(R.id.action_navigation_main_to_mapFragment, bundle)
            }
            override fun onFullscreenAttachment(attachmentUrl: String) {
                val bundle = Bundle().apply {
                    putString("url", attachmentUrl)
                }
                findNavController().navigate(R.id.action_navigation_main_to_imageFragment, bundle)
            }
            override fun onPlayAudio(post: Post) {
                try {
                    val uri = Uri.parse(post.attachment?.uri)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "audio/*")
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPlayVideo(post: Post) {
                try {
                    val uri = Uri.parse(post.attachment?.uri)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "video/*")
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        },
            object : EventListener {
            override fun onFeedListener(feed: FeedItem) {
                eventViewModel.getEventById(feed.id)
                findNavController().navigate(R.id.action_navigation_main_to_carEventFragment)

            }

            override fun onRemoveListener(event: Event) {
                eventViewModel.removeById(event.id)
            }

            override fun onEditListener(event: Event) {
                eventViewModel.edit(event)
                val bundle = Bundle().apply {
                    putString("content", event.content)
                    putString("dateTime", event.datetime)
                    event.coordinates?.latitude?.let { putDouble("lat", it) }
                    event.coordinates?.longitude?.let { putDouble("lng", it) }
                    putString("attachment", event.attachment?.uri)
                    putString("attachmentType", event.attachment?.type?.name.toString())
                }
                findNavController().navigate(R.id.newEventFragment, bundle)
            }

            override fun onLikeListener(event: Event) {
                if (authViewModel.authenticated) {
                    eventViewModel.likeById(event.id,event.likedByMe)
                } else {
                    Toast.makeText(
                        activity,
                        R.string.error_auth,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onJoin(event: Event) {
                if (authViewModel.authenticated) {
                    if (!event.participatedByMe) eventViewModel.joinById(event.id)
                    else eventViewModel.denyById(event.id)
                } else {
                    Toast.makeText(
                        activity,
                        R.string.error_auth,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onOpenSpeakers(event: Event) {
                userViewModel.getUsersIds(event.speakerIds)
                if (event.speakerIds.isEmpty()) {
                    Toast.makeText(context, R.string.empty_list, Toast.LENGTH_SHORT).show()
                } else {
                    findNavController().navigate(R.id.action_navigation_main_to_navigation_users)
                }
            }

            override fun onOpenLikeOwners(event: Event) {
                userViewModel.getUsersIds(event.likeOwnerIds)
                if (event.likeOwnerIds.isEmpty()) {
                    Toast.makeText(context, R.string.empty_list, Toast.LENGTH_SHORT).show()
                } else {
                    findNavController().navigate(R.id.action_navigation_main_to_navigation_users)
                }
            }

            override fun onOpenParticipants(event: Event) {
                userViewModel.getUsersIds(event.participantsIds)
                if (event.participantsIds.isEmpty()) {
                    Toast.makeText(context, R.string.empty_list, Toast.LENGTH_SHORT).show()
                } else {
                    findNavController().navigate(R.id.action_navigation_main_to_navigation_users)
                }
            }

            override fun onMap(event: Event) {
                val bundle = Bundle().apply {
                    event.coordinates?.latitude?.let { putDouble("lat", it) }
                    event.coordinates?.longitude?.let { putDouble("lng", it) }
                }
                findNavController().navigate(R.id.action_navigation_main_to_mapFragment, bundle)
            }

            override fun onFullscreenAttachment(attachmentUrl: String) {
                val bundle = Bundle().apply {
                    putString("url", attachmentUrl)
                }
                findNavController().navigate(R.id.action_navigation_main_to_imageFragment, bundle)
            }
        },userViewModel, viewLifecycleOwner)

        val id = parentFragment?.arguments?.getLong("id")

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadStateAdapter { adapter.retry() },
            footer = PostLoadStateAdapter { adapter.retry() },
        )
        postViewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show()
                }
            }
            binding.progress.isVisible = it.loading
        }

        lifecycleScope.launchWhenCreated {
            postViewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
            }
        }



        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

        return binding.root
    }
}