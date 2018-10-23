package cz.ackee.rxsociallogin.google

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import cz.ackee.rxsociallogin.core.LoginCancelledException
import io.reactivex.Single
import io.reactivex.SingleEmitter

/**
 * Class that handles Google login logic and transforms the result into reactive response.
 */
class RxGoogleLogin(context: Context, additionalScopes: Array<Scope> = arrayOf()) {

    companion object {
        private const val REQUEST_SIGN_IN = 10000
    }

    private val googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(context.getString(R.string.google_server_client_id).also {
                if (it.isEmpty()) {
                    throw RuntimeException("Valid google_server_client_id should be provided in the app")
                }
            })
            .requestScopes(Scope(Scopes.EMAIL), *additionalScopes)
            .build())
    private lateinit var signInEmitter: SingleEmitter<String>

    fun login(activity: Activity) = loginInternal(activity, null)

    fun login(fragment: Fragment) = loginInternal(null, fragment)

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == Activity.RESULT_CANCELED) {
                signInEmitter.onError(LoginCancelledException())
            } else {
                handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
            }
        }
    }

    fun logout() {
        googleSignInClient.signOut()
    }

    private fun loginInternal(activity: Activity?, fragment: Fragment?): Single<String> {
        return Single.create<String> { emitter ->
            if (activity != null) {
                activity.startActivityForResult(googleSignInClient.signInIntent, REQUEST_SIGN_IN)
            } else {
                fragment!!.startActivityForResult(googleSignInClient.signInIntent, REQUEST_SIGN_IN)
            }
            signInEmitter = emitter
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            signInEmitter.onSuccess(completedTask.getResult(ApiException::class.java)!!.serverAuthCode!!)
        } catch (e: ApiException) {
            Log.e("RxGoogleLogin", "Login failed: status code = ${e.statusCode}")
            signInEmitter.onError(e)
        }
    }
}