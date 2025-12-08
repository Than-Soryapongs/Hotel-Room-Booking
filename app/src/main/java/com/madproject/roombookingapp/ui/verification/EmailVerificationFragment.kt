package com.madproject.roombookingapp.ui.verification

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.madproject.roombookingapp.databinding.FragmentEmailVerificationBinding

class EmailVerificationFragment : Fragment() {

	private var _binding: FragmentEmailVerificationBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.btnVerify.setOnClickListener {
			val code = binding.etVerificationCode.text?.toString().orEmpty()
			if (code.length < 6) {
				Snackbar.make(binding.root, "Enter the 6-digit code", Snackbar.LENGTH_LONG).show()
			} else {
				Snackbar.make(binding.root, "Email confirmed", Snackbar.LENGTH_LONG).show()
				findNavController().navigateUp()
			}
		}

		binding.btnOpenEmail.setOnClickListener { openEmailApp() }

		binding.tvResend.setOnClickListener {
			Snackbar.make(binding.root, "A new code is on the way", Snackbar.LENGTH_SHORT).show()
		}

		binding.tvUseDifferentEmail.setOnClickListener {
			findNavController().navigateUp()
		}
	}

	private fun openEmailApp() {
		try {
			val intent = Intent(Intent.ACTION_MAIN).apply {
				addCategory(Intent.CATEGORY_APP_EMAIL)
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			}
			startActivity(intent)
		} catch (_: ActivityNotFoundException) {
			try {
				val intent = Intent(Settings.ACTION_SETTINGS)
				startActivity(intent)
			} catch (_: ActivityNotFoundException) {
				Snackbar.make(binding.root, "Unable to open email app", Snackbar.LENGTH_LONG).show()
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}