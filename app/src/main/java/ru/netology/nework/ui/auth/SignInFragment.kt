package ru.netology.nework.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentSignInBinding
import ru.netology.nework.util.AndroidUtils.hideKeyboard
import ru.netology.nework.util.afterTextChanged
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.SignInViewModel


@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        viewModel.data.observe(viewLifecycleOwner) {
            viewModel.auth.setAuth(it.id, it.token!!, it.login!!)
            findNavController().navigate(R.id.action_navigation_sign_in_to_navigation_main)
        }

        with(binding) {
            password.apply {
                afterTextChanged {
                    authViewModel.loginDataChanged(
                        password.text.toString()
                    )
                }

                login.requestFocus()
                signInButton.setOnClickListener {
                    println("pushed button")
                    viewModel.loginAttempt(
                        login.text.toString(),
                        password.text.toString()
                    )
                }
                signInButton.setOnClickListener {
                    hideKeyboard(requireView())
                    authViewModel.authUser(
                        login.text.toString(),
                        password.text.toString()
                    )
                }
            }
            authViewModel.loginFormState.observe(viewLifecycleOwner, Observer {
                val loginState = it ?: return@Observer

                binding.signInButton.isEnabled = loginState.isDataValid

                if (loginState.passwordError != null) {
                    binding.password.error = getString(loginState.passwordError)
                }

                if (loginState.errorAuth) {
                    Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok) {
                            login.text.clear()
                            login.requestFocus()
                            password.text.clear()
                        }
                        .show()
                }
            })

            transitionSignUp.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_sign_in_to_navigation_signUp)
            }
        }


            return binding.root
        }
    }

