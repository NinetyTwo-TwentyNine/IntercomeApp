package com.example.intercomeapp.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.intercomeapp.R
import com.example.intercomeapp.data.AuthenticationStatus
import com.example.intercomeapp.data.Constants.APP_MIN_PASSWORD_LENGTH
import com.example.intercomeapp.data.Constants.APP_TOAST_PASSWORDS_DONT_MATCH
import com.example.intercomeapp.data.Constants.APP_TOAST_PASSWORD_TOO_SHORT
import com.example.intercomeapp.data.Constants.APP_TOAST_SIGNUP_FAILED
import com.example.intercomeapp.data.Constants.APP_TOAST_SIGNUP_SUCCESS
import com.example.intercomeapp.databinding.FragmentRegistrationBinding
import com.example.intercomeapp.utils.Utils.getBlankStringsChecker
import com.example.intercomeapp.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationFragment : Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var setButtonVisibility: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistrationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_SignUpFragment_to_SignInFragment)
        }

        setButtonVisibility = {
            if (viewModel.authState.value != AuthenticationStatus.Progress) {
                binding.registerButton.isEnabled =
                    !(binding.userEmail.text.toString()
                        .isBlank() || binding.userPassword1.text.toString()
                        .isBlank() || binding.userPassword2.text.toString().isBlank())
            }
        }

        binding.userEmail.addTextChangedListener(
            getBlankStringsChecker(
                binding.userEmail,
                setButtonVisibility
            )
        )
        binding.userPassword1.addTextChangedListener(
            getBlankStringsChecker(
                binding.userPassword1,
                setButtonVisibility
            )
        )
        binding.userPassword2.addTextChangedListener(
            getBlankStringsChecker(
                binding.userPassword2,
                setButtonVisibility
            )
        )

        binding.registerButton.setOnClickListener {
            signUp()
        }
        initObservers()
    }

    private fun signUp() {
        if (binding.userPassword1.text.toString().count() < APP_MIN_PASSWORD_LENGTH) {
            Toast.makeText(activity, APP_TOAST_PASSWORD_TOO_SHORT, Toast.LENGTH_SHORT).show()
        } else if (!binding.userPassword1.text.toString()
                .equals(binding.userPassword2.text.toString())
        ) {
            Toast.makeText(activity, APP_TOAST_PASSWORDS_DONT_MATCH, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.signIn(
                binding.userEmail.text.toString(),
                binding.userPassword1.text.toString(),
                true
            )
        }
    }

    private fun initObservers() {
        viewModel.resetAuthState()
        viewModel.authState.observe(viewLifecycleOwner) { authStatus ->
            when (authStatus) {
                is AuthenticationStatus.Success -> {
                    setButtonVisibility()
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(activity, "$APP_TOAST_SIGNUP_SUCCESS.", Toast.LENGTH_SHORT)
                        .show()
                    Log.d("TAG", "Successful registration")
                    requireView().findNavController()
                        .navigate(R.id.action_SignUpFragment_to_WorkFragment)
                }
                is AuthenticationStatus.Error -> {
                    setButtonVisibility()
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        activity,
                        "$APP_TOAST_SIGNUP_FAILED: ${authStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("TAG", authStatus.message)
                }
                is AuthenticationStatus.Progress -> {
                    binding.registerButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }
}