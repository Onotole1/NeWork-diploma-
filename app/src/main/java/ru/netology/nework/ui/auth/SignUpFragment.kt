package ru.netology.nework.ui.auth
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentSignUpBinding
import ru.netology.nework.util.AndroidUtils.hideKeyboard
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.SignUpViewModel


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentSignUpBinding.inflate(inflater, container, false)
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
                        authViewModel.changeAvatar(uri)
                    }
                }
            }

        viewModel.data.observe(viewLifecycleOwner) {
            viewModel.auth.setAuth(it.id, it.token!!, it.login!!)
            findNavController().navigate(R.id.action_navigation_sign_up_to_navigation_main)
        }

        authViewModel.loginFormState.observe(viewLifecycleOwner) { state ->

            binding.signUpButton.isEnabled = state.isDataValid

            if (state.passwordError != null) {
                binding.password.error = getString(state.passwordError)
            }

            if (state.errorRegistration)
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
            avatar.setOnClickListener {
                ImagePicker.with(this@SignUpFragment)
                    .cropSquare()
                    .compress(2048)
                    .provider(com.github.dhaval2404.imagepicker.constant.ImageProvider.GALLERY)
                    .createIntent(pickPhotoLauncher::launch)
            }

            authViewModel.photo.observe(viewLifecycleOwner) {
                if (it.uri == null) {
                    return@observe
                }
                avatar.setImageURI(it.uri)
            }

            signUpButton.setOnClickListener {
                hideKeyboard(requireView())
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

