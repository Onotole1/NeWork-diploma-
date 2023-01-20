package ru.netology.nework.ui.user


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.adapter.FeedAdapter
import ru.netology.nework.adapter.JobOnInteractionListener
import ru.netology.nework.adapter.JobsAdapter
import ru.netology.nework.adapter.OnPostInteractionListener
import ru.netology.nework.databinding.FragmentProfileBinding
import ru.netology.nework.dto.Event.Companion.emptyEvent
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Job.Companion.emptyJob
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.Post.Companion.emptyPost
import ru.netology.nework.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment: Fragment() {

    lateinit var binding: FragmentProfileBinding
    private val jobViewModel: JobViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()
    private var isVisibleGroupFab = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        val id = arguments?.getLong("id")
        val avatar = arguments?.getString("avatar")
        val name = arguments?.getString("name")

        (activity as AppCompatActivity).supportActionBar?.title = name

        userViewModel.user.observe(viewLifecycleOwner) {
            with(binding) {
                userName.text = name

                userAvatar.setOnClickListener {
                    val bundle = Bundle().apply {
                        putString("url", avatar)
                    }
                    findNavController().navigate(R.id.action_navigation_my_profile_to_imageFragment2, bundle)
                }

                Glide.with(userAvatar)
                    .load("$avatar")
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_avatar)
                    .into(userAvatar)
            }
        }

        val postsAdapter = FeedAdapter(object : OnPostInteractionListener {
            override fun onRemoveListener(post: Post) {
                if (authViewModel.authenticated) {
                postViewModel.removeById(post.id)
                } else {
                    Toast.makeText(
                        activity,
                        R.string.error_auth,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onEditListener(post: Post) {
                if (authViewModel.authenticated) {
                postViewModel.edit(post)
                val bundle = Bundle().apply { putString("content", post.content) }
                findNavController().navigate(R.id.action_navigation_my_profile_to_newPostFragment, bundle)
                } else {
                    Toast.makeText(
                        activity,
                        R.string.error_auth,
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                findNavController().navigate(R.id.action_navigation_my_profile_to_navigation_users)
            }

            override fun onLikeOwnerListener(post: Post) {
                userViewModel.getUsersIds(post.likeOwnerIds)
                findNavController().navigate(R.id.action_navigation_my_profile_to_navigation_users)
            }

            override fun onMap(post: Post) {
                val bundle = Bundle().apply {
                    post.coordinates?.lat?.let { putDouble("lat", it) }
                    post.coordinates?.long?.let { putDouble("lng", it) }
                }
                findNavController().navigate(R.id.mapFragment, bundle)
            }
            override fun onFullscreenAttachment(attachmentUrl: String) {
                val bundle = Bundle().apply {
                    putString("url", attachmentUrl)
                }
                findNavController().navigate(R.id.action_navigation_my_profile_to_imageFragment2, bundle)
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
            override fun onHideListener(post: Post) {
               postViewModel.hidePost(post)
            }

        },userViewModel, viewLifecycleOwner)
        val jobsAdapter = JobsAdapter(object : JobOnInteractionListener {
            override fun onRemoveListener(job: Job) {
                jobViewModel.removeById(job.id)
            }

            override fun onEditListener(job: Job) {
                if (authViewModel.authenticated) {
                    jobViewModel.edit(job)
                    val bundle = Bundle().apply {
                        job.name?.let { putString("name", job.name) }
                        job.position?.let { putString("position", job.position) }
                        job.start?.let { putLong("start", job.start) }
                        job.finish?.let { putLong("finish", job.finish) }
                        job.link?.let { putString("link", job.link) }}
                    findNavController().navigate(R.id.action_navigation_profile_to_newJobFragment, bundle)
                } else {
                    Toast.makeText(
                        activity,
                        R.string.error_auth,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onHideListener(job: Job) {
                jobViewModel.hideById(job.id)
            }

        })

        binding.listPost.adapter = postsAdapter
        binding.listJobs.adapter = jobsAdapter


        binding.buttonLogout.visibility = View.GONE

        authViewModel.data.observe(viewLifecycleOwner) {
            if (authViewModel.authenticated && id == it.id) {
                binding.fab.visibility = View.VISIBLE
            }
        }

        binding.fab.setOnClickListener {
            if (!isVisibleGroupFab) {
                binding.fab.setImageResource(R.drawable.ic_close)
                binding.groupFab.visibility = View.VISIBLE
            } else {
                binding.fab.setImageResource(R.drawable.ic_add_24)
                binding.groupFab.visibility = View.GONE
            }
            isVisibleGroupFab = !isVisibleGroupFab
        }

        binding.fabAddPost.setOnClickListener {
            postViewModel.edit(emptyPost)
            findNavController().navigate(R.id.action_navigation_profile_to_newPostFragment)
        }

        binding.fabAddEvent.setOnClickListener {
            eventViewModel.edit(emptyEvent)
            findNavController().navigate(R.id.action_navigation_my_profile_to_newEventFragment)
        }

        binding.fabAddJob.setOnClickListener {
            jobViewModel.edit(emptyJob)
            findNavController().navigate(R.id.action_navigation_profile_to_newJobFragment)
        }
        return binding.root
    }
}