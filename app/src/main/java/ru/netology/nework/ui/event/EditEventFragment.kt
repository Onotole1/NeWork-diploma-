package ru.netology.nework.ui.event

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEditEventBinding
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Event.Companion.emptyEvent
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.LongArg
import ru.netology.nework.util.listToString
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.MapViewModel
import ru.netology.nework.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@AndroidEntryPoint
class EditEventFragment : Fragment() {
    companion object {
        var Bundle.longArg: Long? by LongArg
        const val DATE_KEY = "date"
        const val TIME_KEY = "time"
        const val EDIT_EVENT_CONTENT_KEY = "event_content"
    }

    private val eventViewModel: EventViewModel by viewModels(ownerProducer = ::requireParentFragment)
    private val userViewModel: UserViewModel by viewModels(ownerProducer = ::requireParentFragment)
    private val mapViewModel: MapViewModel by viewModels(ownerProducer = ::requireParentFragment)
    private val authViewModel: AuthViewModel by viewModels(ownerProducer = ::requireParentFragment)
    lateinit var binding: FragmentEditEventBinding
    private var shared: SharedPreferences? = null

    private var format: EventType = EventType.ONLINE

    private var latitude: Double? = null
    private var longitude: Double? = null
    var event = emptyEvent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        var date = LocalDate.of(1, 1, 1)


        binding = FragmentEditEventBinding.inflate(
            inflater,
            container,
            false
        )

        shared = activity?.getSharedPreferences("draft", Context.MODE_PRIVATE)

        binding.calendar.visibility = View.GONE

        binding.date.setOnClickListener {
            binding.calendar.visibility = View.VISIBLE
        }
        binding.calendar.setOnDateChangeListener { _, i, i2, i3 ->
            date = LocalDate.of(i,i2+1,i3)
            binding.calendar.visibility = View.GONE
            binding.date?.setText(formatter.format(date))
            binding.time.visibility = View.VISIBLE

        }
        binding.timeEdit.setOnClickListener {
            eventViewModel.datetime.value = date
                .atTime(binding.timer.hour,binding.timer.minute,58,1000000)
                .atOffset(ZoneOffset.UTC)
            binding.timer.visibility = View.GONE
        }
        if (eventViewModel.datetime.value != OffsetDateTime.MIN) {
            binding.date.setText(formatter.format(eventViewModel.datetime.value?.toLocalDate()))
        }

        binding.edit.setText(eventViewModel.edited.value?.content)

        binding.editLocation.setOnClickListener {
            findNavController().navigate(R.id.action_newEventFragment_to_mapFragment)
            latitude = arguments?.getDouble("lat")
            longitude = arguments?.getDouble("lng")
        }
        if(mapViewModel.coords.value != Coordinates(55.751999, 37.617734)){
            binding.editLocation.isChecked = true
        }
        binding.edit.setText(eventViewModel.edited.value?.content)

        eventViewModel.edited.observe(viewLifecycleOwner) {event->
            val nameSpeakers = mutableListOf<String>()

            event.speakerIds.map { id ->
                userViewModel.getUserName(id)?.let { nameSpeakers.add(it) }
            }
            val nameSpeakersString = listToString(nameSpeakers)

           // binding.speakersEdit.setText(nameSpeakersString)
        }

        with(binding) {
            edit.apply {
                arguments?.let { bundle ->
                    this.setText(bundle.getString(EditEventFragment.EDIT_EVENT_CONTENT_KEY))
                    this.requestFocus()
                }
            }

            binding.speakersEdit.setOnClickListener {
                val bundle = Bundle().apply { putString("open", "speaker") }
                findNavController().navigate(R.id.navigation_users, bundle)
            }
            binding.participantsEdit.setOnClickListener {
                val bundle = Bundle().apply { putString("open", "speaker") }
                findNavController().navigate(R.id.navigation_users, bundle)
            }

            arguments?.longArg?.let { id -> eventViewModel.getEventById(id) }
            binding.edit.requestFocus()

            activity?.let {
                ArrayAdapter.createFromResource(
                    it,
                    R.array.event_types,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinner.adapter = adapter
                    if (format.text == "OFFLINE" || format.text == "offline") binding.spinner.setSelection(
                        1
                    )
                }
            }
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View?, selectedItemPosition: Int, selectedId: Long
            ) {
                when (selectedItemPosition) {
                    0 -> {
                        binding.tableRowLink.visibility = View.VISIBLE
                        binding.tableRowCoord.visibility = View.GONE
                        format = EventType.ONLINE
                    }
                    1 -> {
                        binding.tableRowLink.visibility = View.GONE
                        binding.tableRowCoord.visibility = View.VISIBLE
                        format = EventType.OFFLINE
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
            binding.edit.requestFocus()

            binding.edit.setText(eventViewModel.edited.value?.content)

            val pickPhotoLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    when (it.resultCode) {
                        ImagePicker.RESULT_ERROR -> {
                            Snackbar.make(
                                binding.root,
                                ImagePicker.getError(it.data),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        Activity.RESULT_OK -> {
                            val uri: Uri? = it.data?.data
                            eventViewModel.changePhoto(uri, uri?.toFile())
                        }
                    }
                }

            binding.pickPhoto.setOnClickListener {
                ImagePicker.with(this)
                    .crop()
                    .compress(2048)
                    .provider(ImageProvider.GALLERY)
                    .galleryMimeTypes(
                        arrayOf(
                            "image/png",
                            "image/jpeg",
                        )
                    )
                    .createIntent(pickPhotoLauncher::launch)
            }

            binding.takePhoto.setOnClickListener {
                ImagePicker.with(this)
                    .crop()
                    .compress(2048)
                    .provider(ImageProvider.CAMERA)
                    .createIntent(pickPhotoLauncher::launch)
            }

            binding.deletePhoto.setOnClickListener {
                eventViewModel.changePhoto(null, null)
            }

            eventViewModel.postCreated.observe(viewLifecycleOwner) {
                findNavController().navigateUp()
            }

            eventViewModel.file.observe(viewLifecycleOwner) {
                if (it.uri == null) {
                    binding.photoLayout.visibility = View.GONE
                    return@observe
                }

                binding.photoLayout.visibility = View.VISIBLE
                binding.photo.setImageURI(it.uri)
            }

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.new_object, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            binding?.let {
                                eventViewModel.changeContent(it.edit.text.toString())
                                eventViewModel.changeEventType(it.spinner.toString())
                                eventViewModel.changeDatetime(("${it.dateEdit.text} ${it.timeEdit.text}").toLong())
                                if (latitude != null || longitude != null) eventViewModel.saveCoordinates(
                                    latitude!!,
                                    longitude!!
                                )
                                eventViewModel.saveSpeaker()
                                eventViewModel.save()
                                AndroidUtils.hideKeyboard(requireView())
                            }
                            true
                        }
                        else -> false
                    }

            }, viewLifecycleOwner)
            return binding.root
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(DATE_KEY, binding.timeEdit.text.toString())
        outState.putString(TIME_KEY, binding.dateEdit.text.toString())
    }
}
