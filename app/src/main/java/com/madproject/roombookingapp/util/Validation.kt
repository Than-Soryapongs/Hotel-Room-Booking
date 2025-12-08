package com.madproject.roombookingapp.util

object Validation {

    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "Email is required")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult(false, "Invalid email format")
        }
        return ValidationResult(true)
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(false, "Password is required")
        }
        if (password.length < 8) {
            return ValidationResult(false, "Password must be at least 8 characters")
        }
        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(false, "Password must contain an uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(false, "Password must contain a lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            return ValidationResult(false, "Password must contain a number")
        }
        return ValidationResult(true)
    }

    fun validateUsername(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(false, "Username is required")
        }
        if (username.length < 3) {
            return ValidationResult(false, "Username must be at least 3 characters")
        }
        return ValidationResult(true)
    }

    fun validateName(name: String, fieldName: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "$fieldName is required")
        }
        if (name.length < 2) {
            return ValidationResult(false, "$fieldName must be at least 2 characters")
        }
        return ValidationResult(true)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
}