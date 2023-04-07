package com.bronski.googlesignincompose.presentation.signIn

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
