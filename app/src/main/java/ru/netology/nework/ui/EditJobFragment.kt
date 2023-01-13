package ru.netology.nework.ui


import android.os.Bundle
import android.text.Editable
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEditJobBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.CurrentTimes
import ru.netology.nework.util.CurrentTimes.*
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.JobViewModel
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

const val START_KEY = "START_KEY"
const val FINISH_KEY = "FINISH_KEY"

@AndroidEntryPoint
class EditJobFragment : Fragment() {

    private val viewModel: JobViewModel by activityViewModels()
    private var fragmentBinding: FragmentEditJobBinding? = null
    private var calendar: Calendar? = null

    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditJobBinding.inflate(
            inflater,
            container,
            false
        )

        val userId: Long? = arguments?.getLong("user")
        fragmentBinding = binding
        calendar = Calendar.getInstance()
        with(binding) {
            startDateEdit.text = (savedInstanceState?.getString(START_KEY)?: getString(R.string.startDate)) as Editable?
            finishDateEdit.text = (savedInstanceState?.getString(FINISH_KEY)?: getString(R.string.finishDate)) as Editable?
        }

        binding.startDateEdit.setOnClickListener {
            setDate(calendar) {
                initStart()
            }
        }

        binding.finishDateEdit.setOnClickListener {
            setDate(calendar) {
                initFinish()
            }
        }



        val company: String? = arguments?.getString("company")
        val position: String? = arguments?.getString("position")
        val start: Long? = CurrentTimes.formatDate(arguments?.getLong("start") as Long)
        val finish: Long? = CurrentTimes.formatDate(arguments?.getLong("finish"))
        val link: String? = arguments?.getString("link")


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
                                viewModel.changeContent(it.editNew.text.toString())
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
    override fun onDetach() {
        super.onDetach()
        calendar = null
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