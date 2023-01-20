package ru.netology.nework.ui.post

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEditPostBinding
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.viewmodel.PostViewModel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
@AndroidEntryPoint
class EditPostFragment : Fragment() {

    var type: AttachmentType? = null

    private val viewModel: PostViewModel by activityViewModels()
    private var fragmentBinding: FragmentEditPostBinding? = null
    private var shared: SharedPreferences? = null
    var lat: Double? = null
    var lng: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditPostBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding
        shared = activity?.getSharedPreferences("draft", Context.MODE_PRIVATE)
        val keyShared = "content"

        val content = arguments?.getString("content") ?: shared?.getString(keyShared, null)

        arguments?.textArg
            ?.let { binding.edit.setText(it) }

        binding.edit.requestFocus()

        binding.edit.setText(viewModel.edited.value?.content)

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
                        it.data?.data.let { uri ->
                            val stream = uri?.let { context?.contentResolver?.openInputStream(it) }
                            viewModel.changeMedia(uri, stream, type)
                        }
                    }
                }
            }

        val pickMediaLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    val stream = context?.contentResolver?.openInputStream(it)
                    viewModel.changeMedia(it, stream, type)
                }
            }

        binding.pickPhoto.setOnClickListener {
            pickMediaLauncher.launch("image/*")
            type = AttachmentType.IMAGE
        }

        binding.audio.setOnClickListener {
            pickMediaLauncher.launch("audio/*")
            type = AttachmentType.AUDIO
        }

        binding.editVideo.setOnClickListener {
            pickMediaLauncher.launch("video/*")
            type = AttachmentType.VIDEO
        }

        binding.deletePhoto.setOnClickListener {
            viewModel.changeMedia(null, null, null)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
            type = AttachmentType.IMAGE
        }

        binding.editLocation.setOnClickListener {
            shared?.edit {
                putString(keyShared, binding.edit.text.toString())
                apply()
            }
            val bundle = Bundle().apply {
                putString("fragment", "newPost")
                if (lat != null) {
                    putDouble("lat", lat!!)
                }
                if (lng != null) {
                    putDouble("lng", lng!!)
                }
            }
            findNavController().navigate(
                R.id.mapFragment, bundle
            )
        }

        binding.editMentions.setOnClickListener {

            findNavController().navigate(
                R.id.action_newJobFragment_to_navigation_users
            )
        }

        viewModel.media.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoLayout.visibility = View.GONE
                return@observe
            }
            binding.info.text = viewModel.media.value?.uri?.path
            binding.photoLayout.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


    requireActivity().addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.new_object, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
            when (menuItem.itemId) {
                R.id.save -> {
                    fragmentBinding?.let {
                        viewModel.changeContent(
                            it.edit.text.toString(),
                            coord = if (lat != null && lng != null) Coordinates(
                                lat!!,
                                lng!!
                            ) else null
                        )
                        viewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                    }
                    true
                }
                else -> false
            }

    }, viewLifecycleOwner)
        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    object StringArg : ReadWriteProperty<Bundle, String?> {
        override fun getValue(thisRef: Bundle, property: KProperty<*>): String? {
            return thisRef.getString(property.name)
        }

        override fun setValue(thisRef: Bundle, property: KProperty<*>, value: String?) {
            thisRef.putString(property.name, value)
        }
    }
}