package ru.netology.nework.ui.job

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.CardJobBinding

import ru.netology.nework.dto.Job
import ru.netology.nework.util.IntArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.JobViewModel

@AndroidEntryPoint
class CardJobFragment : Fragment()
{
    private val viewModel: JobViewModel by activityViewModels()
    private val viewModelAuth: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CardJobBinding.inflate(
            inflater,
            container,
            false
        )

        val job: Job = arguments?.get("job") as Job
        with(binding) {
            nameOfCompany.text = job.name
            position.text = job.position
            period.text = job.getString()
            link.text = job.link
            menu.isVisible = job.ownedByMe


            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.object_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.object_remove -> {
                                if (viewModelAuth.authenticated) {
                                    viewModel.removeById(job.id)
                                    findNavController().navigateUp()

                                } else {
                                    authenticate()
                                }
                                true
                            }
                            R.id.object_edit -> {
                                if (viewModelAuth.authenticated) {
                                    viewModel.edit(job)
                                    findNavController().navigate(
                                        R.id.action_cardJobFragment_to_newJobFragment,
                                        Bundle().apply {
                                        }
                                    )

                                } else {
                                    authenticate()
                                }
                                true
                            }
                            R.id.object_hide -> {
                                viewModel.hideById(job.id)
                                findNavController().navigateUp()
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
            return binding.root
        }
    }
    fun authenticate() =
        findNavController().navigate(R.id.action_cardJobFragment_to_navigation_sign_in)
    companion object {

        private const val TEXT_KEY = "TEXT_KEY"
        var Bundle.textArg: String?
            set(value) = putString(TEXT_KEY, value)
            get() = getString(TEXT_KEY)

        var Bundle.idArg: Int by IntArg

    }

}