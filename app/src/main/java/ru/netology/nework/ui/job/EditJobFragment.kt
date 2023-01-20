package ru.netology.nework.ui.job

import android.os.Bundle
import android.text.Editable
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEditJobBinding
import ru.netology.nework.ui.user.USER_ID
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.CurrentTimes
import ru.netology.nework.util.CurrentTimes.dateToEpochSec
import ru.netology.nework.util.CurrentTimes.formatDateL
import ru.netology.nework.util.CurrentTimes.showDateDialog
import ru.netology.nework.util.afterTextChanged
import ru.netology.nework.util.setDate
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.JobViewModel
import java.util.*

@AndroidEntryPoint
class EditJobFragment : Fragment() {

    private val viewModel: JobViewModel by activityViewModels()
    private var fragmentBinding: FragmentEditJobBinding? = null
    private var calendar: Calendar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        val binding = FragmentEditJobBinding.inflate(
            inflater,
            container,
            false
        )

        val userId: Long? = arguments?.getLong("user")

        fragmentBinding = binding
        calendar = Calendar.getInstance()
        with(binding) {
            startDateEdit.text = (savedInstanceState?.getString(START_KEY)
                ?: getString(R.string.startDate)) as Editable?
            finishDateEdit.text = (savedInstanceState?.getString(FINISH_KEY)
                ?: getString(R.string.finishDate)) as Editable?
        }

        binding.startDateEdit.setOnClickListener {
            setDate(calendar) {
                calendar?.timeInMillis?.let {
                    binding.startDateEdit.text = formatDateL(it) as Editable?
                    viewModel.changeStart(it)
                }
            }
        }

        binding.finishDateEdit.setOnClickListener {
                setDate(calendar) {
                    calendar?.timeInMillis?.let {
                        binding.finishDateEdit.text = formatDateL(it) as Editable?
                        viewModel.changeFinish(it)
                    }
                }
            }

            val company = arguments?.getString("company")
            val position = arguments?.getString("position")
            val start = formatDateL(arguments?.getLong("start"))
            val finish = formatDateL(arguments?.getLong("finish"))
            val link = arguments?.getString("link")

            viewModel.jobCreated.observe(viewLifecycleOwner) {
                findNavController().navigateUp()
            }

            with(binding) {
                startDateEdit.setText(start)
                finishDateEdit.setText(finish)
                companyEdit.setText(company)
                linkEdit.setText(link)
                positionEdit.setText(position)
            }

            binding.startDateEdit.setOnClickListener {
                binding.startDateEdit.error = null
                context?.let { context -> showDateDialog(binding.startDateEdit, context) }
            }

            binding.finishDateEdit.setOnClickListener {
                context?.let { context -> showDateDialog(binding.finishDateEdit, context) }
            }

            binding.linkEdit.afterTextChanged {
                viewModel.isLinkValid(it)
            }


            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.new_object, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            fragmentBinding?.let {
                                val start = it.startDateEdit.text.toString()
                                val company = it.companyEdit.text.toString()
                                val position = it.positionEdit.text.toString()

                                viewModel.requireData(start, position, company)

                                val state = viewModel.dataState.value

                                if (state != null) {
                                    if (state.emptyToDate != null) {
                                        it.startDateEdit.error =
                                            getString(state.emptyToDate)
                                    }

                                    if (state.emptyPositionError != null) {
                                        it.positionEdit.error =
                                            getString(state.emptyPositionError)
                                    }

                                    if (state.emptyCompanyError != null) {
                                        it.companyEdit.error =
                                            getString(state.emptyCompanyError)
                                    }

                                    if (state.isDataNotBlank) {
                                        dateToEpochSec(start)?.let { startLong ->
                                            viewModel.changeData(
                                                start = startLong,
                                                finish = dateToEpochSec(it.finishDateEdit.text.toString()),
                                                company = company,
                                                link = it.linkEdit.text.toString(),
                                                position = position
                                            )
                                        }
                                viewModel.save(USER_ID.toLong())
                                AndroidUtils.hideKeyboard(requireView())
                            } else return false

                        }
                    }
                            true
            }
                        else -> false
         }

    }, viewLifecycleOwner)
        return binding.root
    }
    override fun onDetach() {
        super.onDetach()
        calendar = null
    }
}
