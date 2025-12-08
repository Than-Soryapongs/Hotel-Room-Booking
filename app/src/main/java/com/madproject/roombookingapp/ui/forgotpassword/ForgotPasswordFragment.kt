package com.madproject.roombookingapp.ui.forgotpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.madproject.roombookingapp.databinding.FragmentForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.forgotPasswordState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ForgotPasswordViewModel.ForgotPasswordState.Loading -> {
                    showLoading(true)
                }
                is ForgotPasswordViewModel.ForgotPasswordState.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        state.response.message,
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
                }
                is ForgotPasswordViewModel.ForgotPasswordState.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString()
            viewModel.forgotPassword(email)
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}