package ru.netology.nework.ui
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.viewmodel.SignUpViewModel

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        viewModel.data.observe(viewLifecycleOwner) {
            viewModel.auth.setAuth(it.id, it.token)
            findNavController().navigateUp()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.registrationError)
                Snackbar.make(binding.root, R.string.error_loading, LENGTH_INDEFINITE)
                    .setAction(R.string.retry_loading) {
                        viewModel.registrationUser(
                            binding.login.text.toString(),
                            binding.password.text.toString(),
                            binding.name.text.toString()
                        )
                    }.show()
        }
        with(binding) {
            signUpButton.setOnClickListener {
                if (password.text.toString() == repeatPassword.text.toString()) {
                    viewModel.registrationUser(
                        login.text.toString(),
                        password.text.toString(),
                        name.text.toString()
                    )
                } else {
                    repeatPassword.error = getString(R.string.password_error)
                }
            }
        }
        return binding.root
    }
}

