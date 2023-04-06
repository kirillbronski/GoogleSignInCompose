package com.bronski.googlesignincompose.presentation.signIn

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.bronski.googlesignincompose.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? =
        runCatching {
            oneTapClient.beginSignIn(buildSignInRequest()).await()
        }.fold(
            onSuccess = {
                it.pendingIntent.intentSender
            },
            onFailure = {
                it.printStackTrace()
                null
            }
        )


    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return runCatching {
            auth.signInWithCredential(googleCredentials).await()
        }.fold(
            onSuccess = {
                SignInResult(
                    data = it.user?.run {
                        UserData(
                            userId = uid,
                            username = displayName,
                            profilePictureUrl = photoUrl?.toString()
                        )
                    },
                    error = null
                )
            },
            onFailure = {
                it.printStackTrace()
                SignInResult(
                    data = null,
                    error = it.message
                )
            }
        )
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    suspend fun signOut() {
        runCatching {
            oneTapClient.signOut().await()
        }.fold(
            onSuccess = {
                auth.signOut()
            },
            onFailure = {
                it.printStackTrace()
            }
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }


}