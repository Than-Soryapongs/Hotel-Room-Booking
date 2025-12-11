package com.madproject.roombookingapp.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.madproject.roombookingapp.R
import com.madproject.roombookingapp.data.model.UserResponse
import com.madproject.roombookingapp.data.repository.AuthRepository
import com.madproject.roombookingapp.databinding.ActivityProfileBinding
import com.madproject.roombookingapp.ui.auth.AuthActivity
import com.madproject.roombookingapp.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    @Inject lateinit var repository: AuthRepository

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        contentResolver.openInputStream(uri)?.use { stream ->
            val bytes = stream.readBytes()
            val name = uri.lastPathSegment ?: "avatar.jpg"
            viewModel.uploadProfilePicture(bytes, name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupGenderDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationIcon(R.drawable.ic_arrow_left)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            title = getString(R.string.profile_title)
        }
    }

    private fun setupGenderDropdown() {
        val genders = listOf("MALE", "FEMALE", "OTHER")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, genders)
        binding.etGender.apply {
            setAdapter(adapter)
            keyListener = null
            isCursorVisible = false
            setOnClickListener { showDropDown() }
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }
        }
    }

    private fun setupListeners() {
        binding.btnUploadPhoto.setOnClickListener { imagePicker.launch("image/*") }
        binding.btnRemovePhoto.setOnClickListener { viewModel.deleteProfilePicture() }

        binding.btnSaveProfile.setOnClickListener {
            viewModel.updateProfile(
                binding.etFirstName.text?.toString(),
                binding.etLastName.text?.toString(),
                binding.etGender.text?.toString(),
                binding.etAddress.text?.toString()
            )
        }

        binding.btnChangeEmail.setOnClickListener { showChangeEmailDialog() }
        binding.btnChangePassword.setOnClickListener { showChangePasswordDialog() }
        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun observeViewModel() {
        viewModel.profileState.observe(this) { state ->
            binding.progress.isVisible = state is ProfileViewModel.ProfileState.Loading
            binding.profileScroll.isVisible = state !is ProfileViewModel.ProfileState.Loading

            when (state) {
                is ProfileViewModel.ProfileState.Data -> renderProfile(state.user)
                is ProfileViewModel.ProfileState.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                else -> Unit
            }
        }

        viewModel.uiMessage.observe(this) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.consumeMessage()
            }
        }
    }

    private fun renderProfile(user: UserResponse) {
        val displayName = listOfNotNull(user.firstName, user.lastName).joinToString(" ").ifBlank { user.username }
        binding.tvName.text = displayName
        binding.tvEmail.text = user.pendingEmail?.let {
            getString(R.string.profile_pending_email, it)
        } ?: user.email
        binding.etFirstName.setText(user.firstName)
        binding.etLastName.setText(user.lastName)
        binding.etGender.setText(user.gender.ifBlank { "" }, false)
        binding.etAddress.setText(user.address)

        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val createdAt = formatDate(user.createdAt, formatter)
        val lastLogin = user.lastLoginAt?.let { formatDate(it, formatter) } ?: "—"
        binding.tvMeta.text = getString(R.string.profile_meta, createdAt, lastLogin)

        val fullAvatarUrl = if (user.profileImageUrl != null && !user.profileImageUrl.startsWith("http")) {
            Constants.BASE_URL.trimEnd('/') + user.profileImageUrl
        } else {
            user.profileImageUrl
        }

        binding.ivAvatar.load(fullAvatarUrl) {
            placeholder(R.drawable.ic_user)
            error(R.drawable.ic_user)
            crossfade(true)
        }

        binding.chipRoles.removeAllViews()
        user.roles.forEach { role ->
            val chip = Chip(this).apply {
                text = role.replace("ROLE_", "", true)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                isCheckable = false
            }
            binding.chipRoles.addView(chip)
        }
    }

    private fun formatDate(value: String?, formatter: DateTimeFormatter): String {
        if (value.isNullOrBlank()) return "—"
        return try {
            LocalDateTime.parse(value).format(formatter)
        } catch (ex: DateTimeParseException) {
            value
        }
    }

    private fun showChangeEmailDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_email, null)
        val newEmailInput = view.findViewById<TextInputEditText>(R.id.etNewEmail)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.etCurrentPassword)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile_dialog_change_email_title)
            .setView(view)
            .setPositiveButton(R.string.profile_dialog_change_email_action) { _, _ ->
                viewModel.changeEmail(
                    newEmailInput.text?.toString().orEmpty(),
                    passwordInput.text?.toString().orEmpty()
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val oldPassword = view.findViewById<TextInputEditText>(R.id.etOldPassword)
        val newPassword = view.findViewById<TextInputEditText>(R.id.etNewPassword)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile_dialog_change_password_title)
            .setView(view)
            .setPositiveButton(R.string.profile_dialog_change_password_action) { _, _ ->
                viewModel.changePassword(
                    oldPassword.text?.toString().orEmpty(),
                    newPassword.text?.toString().orEmpty()
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            repository.logout()
            Toast.makeText(this@ProfileActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@ProfileActivity, AuthActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
}
