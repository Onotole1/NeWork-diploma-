package ru.netology.nework.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapter.EventAdapter
import ru.netology.nework.adapter.EventListener
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.MapViewModel
import ru.netology.nework.viewmodel.UserViewModel


@AndroidEntryPoint
class CardEventFragment : Fragment() {

    private val eventViewModel: EventViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val mapViewModel: MapViewModel by viewModels(
        ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CardEventBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = EventAdapter(object : EventListener {
            override fun onFeedListener(feed: FeedItem) {
                eventViewModel.getEventById(feed.id)
                findNavController().navigate(R.id.action_eventsFragment_to_carEventFragment)

            }

            override fun onRemoveListener(event: Event) {
                eventViewModel.removeById(event.id)
            }

            override fun onEditListener(event: Event) {
                eventViewModel.edit(event)
                val bundle = Bundle().apply {
                    putString("content", event.content)
                    putString("dateTime", event.datetime)
                    event.coordinates?.lat?.let { putDouble("lat", it) }
                    event.coordinates?.long?.let { putDouble("lng", it) }
                    putString("attachment", event.attachment?.uri)
                    putString("attachmentType", event.attachment?.type?.name.toString())
                }
                findNavController().navigate(R.id.newEventFragment, bundle)
            }

            override fun onLikeListener(event: Event) {
                if (authViewModel.authenticated) {
                    eventViewModel.likeById(event.id, event.likedByMe)
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
                    findNavController().navigate(R.id.action_cardEventFragment_to_navigation_users)
                }
            }

            override fun onOpenLikeOwners(event: Event) {
                userViewModel.getUsersIds(event.likeOwnerIds)
                if (event.likeOwnerIds.isEmpty()) {
                    Toast.makeText(context, R.string.empty_list, Toast.LENGTH_SHORT).show()
                } else {
                    findNavController().navigate(R.id.action_cardEventFragment_to_navigation_users)
                }
            }

            override fun onOpenParticipants(event: Event) {
                userViewModel.getUsersIds(event.participantsIds)
                if (event.participantsIds.isEmpty()) {
                    Toast.makeText(context, R.string.empty_list, Toast.LENGTH_SHORT).show()
                } else {
                    findNavController().navigate(R.id.action_cardEventFragment_to_navigation_users)
                }
            }

            override fun onMap(event: Event) {
                val bundle = Bundle().apply {
                    event.coordinates?.lat?.let { putDouble("lat", it) }
                    event.coordinates?.long?.let { putDouble("lng", it) }
                }
                findNavController().navigate(R.id.action_eventsFragment_to_mapFragment, bundle)
            }

            override fun onFullscreenAttachment(attachmentUrl: String) {
                val bundle = Bundle().apply {
                    putString("url", attachmentUrl)
                }
                findNavController().navigate(R.id.action_newEventFragment_to_imageFragment2, bundle)
            }
        }, userViewModel, viewLifecycleOwner)

        eventViewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            eventViewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        return binding.root
    }
    private fun authenticate() = findNavController().navigate(R.id.action_cardEventFragment_to_navigation_sign_in)

}