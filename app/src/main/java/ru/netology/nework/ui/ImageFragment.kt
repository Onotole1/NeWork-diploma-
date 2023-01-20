package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.BuildConfig.BASE_URL
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentImageBinding

@AndroidEntryPoint
class ImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val attachmentUrl = arguments?.getString("image")
        val binding = FragmentImageBinding.inflate(inflater, container, false)
        binding.apply {
            imageAttachmentFullScreen.visibility = View.GONE
            attachmentUrl?.let {
                val url = "${BASE_URL}/media/${it}"


                Glide.with(imageAttachmentFullScreen)
                    .load(url)
                    .placeholder(R.drawable.ic_loading_24)
                    .error(R.drawable.ic_baseline_error_outline_24)
                    .timeout(10_000)
                    .into(imageAttachmentFullScreen)
            }
            imageAttachmentFullScreen.visibility = View.VISIBLE
        }

        binding.imageAttachmentFullScreen.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }
}

