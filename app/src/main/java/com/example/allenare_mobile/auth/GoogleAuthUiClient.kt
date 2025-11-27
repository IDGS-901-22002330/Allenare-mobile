package com.example.allenare_mobile.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.allenare_mobile.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

// Clase que se encarga de la lógica de Google Sign-In
class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            throw e
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): UserData? {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        if (googleIdToken != null) {
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            return try {
                val user = auth.signInWithCredential(firebaseCredential).await().user
                user?.toUserData()
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e
                throw e
            }
        }
        return null
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.toUserData()

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.google_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}

// Clase para unificar los datos del usuario
data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?,
    val email: String?
)

// Función para convertir un FirebaseUser a nuestro UserData
fun com.google.firebase.auth.FirebaseUser.toUserData(): UserData {
    return UserData(
        userId = uid,
        username = displayName,
        profilePictureUrl = photoUrl?.toString(),
        email = email
    )
}