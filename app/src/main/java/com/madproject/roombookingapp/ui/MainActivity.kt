package com.madproject.roombookingapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.madproject.roombookingapp.data.repository.AuthRepository
import com.madproject.roombookingapp.databinding.ActivityMainBinding
import com.madproject.roombookingapp.ui.auth.AuthActivity
import com.madproject.roombookingapp.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var repository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        lifecycleScope.launch {
            val isLoggedIn = repository.isLoggedIn()

            if (isLoggedIn) {
                navigateToHome()
            } else {
                navigateToAuth()
            }
        }
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}