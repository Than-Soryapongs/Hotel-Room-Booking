package com.madproject.roombookingapp.ui.resetpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.madproject.roombookingapp.databinding.FragmentResetPasswordBinding

class ResetPasswordFragment : Fragment() {

	private var _binding: FragmentResetPasswordBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.btnResetPassword.setOnClickListener {
			val code = binding.etCode.text?.toString().orEmpty()
			val newPassword = binding.etNewPassword.text?.toString().orEmpty()
			val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

			if (code.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
				Snackbar.make(binding.root, "Please fill every field", Snackbar.LENGTH_LONG).show()
				return@setOnClickListener
			}

			if (newPassword != confirmPassword) {
				Snackbar.make(binding.root, "Passwords do not match", Snackbar.LENGTH_LONG).show()
				return@setOnClickListener
			}

			binding.progressReset.visibility = View.VISIBLE
			binding.btnResetPassword.isEnabled = false

			binding.root.postDelayed({
				binding.progressReset.visibility = View.GONE
				binding.btnResetPassword.isEnabled = true
				Snackbar.make(binding.root, "Password updated", Snackbar.LENGTH_LONG).show()
				findNavController().navigateUp()
			}, 1200)
		}

		binding.tvBackToLogin.setOnClickListener {
			findNavController().navigateUp()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}