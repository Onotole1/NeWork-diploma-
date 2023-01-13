package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentSignInBinding
import ru.netology.nework.viewmodel.SignInViewModel


@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        viewModel.data.observe(viewLifecycleOwner) {
            viewModel.auth.setAuth(it.id, it.token!!.token, it.login!!)
            findNavController().navigateUp()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.loginError) {
                binding.password.error = getString(R.string.password_error)
            }
        }

        with(binding) {
            login.requestFocus()
            signInButton.setOnClickListener {
                println("pushed button")
                viewModel.loginAttempt(
                    login.text.toString(),
                    password.text.toString()
                )
            }
        }
        return binding.root
    }
}
