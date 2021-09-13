package io.github.ackeecz.rxsociallogin.google

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import io.github.ackeecz.rxsociallogin.core.LoginCancelledException
import io.reactivex.Single
import io.reactivex.SingleEmitter

/**
 * Class that handles Google login logic and transforms the result into reactive response.
 */
class RxGoogleLogin(
    context: Context,
    additionalScopes: Array<Scope> = emptyArray(),
    private val tokenType: GoogleTokenType = GoogleTokenType.SERVER_AUTH_CODE
) {

    companion object {

        private const val REQUEST_SIGN_IN = 10000
    }

    private val client = GoogleSignIn.getClient(context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestToken(context.getString(R.string.google_server_client_id))
        .requestScopes(Scope(Scopes.EMAIL), *additionalScopes)
        .build())
    private val googleSignInClient = client
    private var signInEmitter: SingleEmitter<String>? = null

    private fun GoogleSignInOptions.Builder.requestToken(serverClientId: String): GoogleSignInOptions.Builder {
        if (serverClientId.isEmpty()) {
            throw RuntimeException("Valid google_server_client_id should be provided in the app")
        } else {
            return when (tokenType) {
                GoogleTokenType.SERVER_AUTH_CODE -> requestServerAuthCode(serverClientId)
                GoogleTokenType.ID_TOKEN -> requestIdToken(serverClientId)
            }
        }
    }

    fun login(activity: Activity) = loginInternal(activity, null)

    fun login(fragment: Fragment) = loginInternal(null, fragment)

    private fun loginInternal(activity: Activity?, fragment: Fragment?): Single<String> {
        return Single.create<String> { emitter ->
            if (activity != null) {
                activity.startActivityForResult(googleSignInClient.signInIntent, REQUEST_SIGN_IN)
            } else {
                fragment!!.startActivityForResult(googleSignInClient.signInIntent, REQUEST_SIGN_IN)
            }
            signInEmitter = emitter
        }.doFinally {
            // Clear reference on emitter to prevent leakage of whole returned Single together with
            // fragment or activity when RxGoogleLogin is used as singleton (or generally lives longer
            // than fragment or activity)
            signInEmitter = null
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == Activity.RESULT_CANCELED) {
                signInEmitter?.onError(LoginCancelledException())
            } else {
                handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val taskResult = completedTask.getResult(ApiException::class.java)!!
            val tokenResult = when (tokenType) {
                GoogleTokenType.SERVER_AUTH_CODE -> taskResult.serverAuthCode!!
                GoogleTokenType.ID_TOKEN -> taskResult.idToken!!
            }
            signInEmitter?.onSuccess(tokenResult)
        } catch (e: ApiException) {
            Log.e("RxGoogleLogin", "Login failed: status code = ${e.statusCode}")
            signInEmitter?.onError(e)
        }
    }

    fun logout() {
        googleSignInClient.signOut()
    }
}

enum class GoogleTokenType {
    SERVER_AUTH_CODE,
    ID_TOKEN
}
