package com.bronski.googlesignincompose.presentation.signIn

data class SignInResult(
    val data: UserData?,
    val error: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)
