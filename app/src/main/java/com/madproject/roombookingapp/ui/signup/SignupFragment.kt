package com.madproject.roombookingapp.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.madproject.roombookingapp.databinding.FragmentSignupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGenderSpinner()
        setupObservers()
        setupClickListeners()
    }

    private fun setupGenderSpinner() {
        val genders = arrayOf("MALE", "FEMALE", "OTHER")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, genders)
        binding.spinnerGender.setAdapter(adapter)
        binding.spinnerGender.setText(genders.first(), false)
    }

    private fun setupObservers() {
        viewModel.signupState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SignupViewModel.SignupState.Loading -> {
                    showLoading(true)
                }
                is SignupViewModel.SignupState.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        state.response.message,
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
                }
                is SignupViewModel.SignupState.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val gender = binding.spinnerGender.text?.toString().orEmpty()

            viewModel.signup(username, email, password, confirmPassword, firstName, lastName, gender)
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignup.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}