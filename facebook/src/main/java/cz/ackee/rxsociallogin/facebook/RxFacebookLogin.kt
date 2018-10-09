package cz.ackee.rxsociallogin.facebook

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import io.reactivex.Single

/**
 * Class that handles Facebook login logic and transforms the result into reactive response.
 */
class RxFacebookLogin {

    private val callbackManager = CallbackManager.Factory.create()
    private val loginManager get() = LoginManager.getInstance()

    fun login(activity: Activity, readPermissions: Collection<String> = listOf("public_profile"), publishPermissions: Collection<String>? = null) =
            loginInternal(activity, null, readPermissions, publishPermissions)

    fun login(fragment: Fragment, readPermissions: Collection<String> = listOf("public_profile"), publishPermissions: Collection<String>? = null) =
            loginInternal(null, fragment, readPermissions, publishPermissions)

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun logout() {
        loginManager.logOut()
    }

    private fun loginInternal(activity: Activity?, fragment: Fragment?, readPermissions: Collection<String>, publishPermissions: Collection<String>?): Single<String> {
        return Single.create<String> { emitter ->
            loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    emitter.onSuccess(result.accessToken.token)
                }

                override fun onError(error: FacebookException) {
                    emitter.onError(error)
                }

                override fun onCancel() {
                    emitter.onError(LoginCancelledException())
                }
            })
            if (publishPermissions != null) {
                if (activity != null) {
                    loginManager.logInWithPublishPermissions(activity, publishPermissions)
                } else {
                    loginManager.logInWithPublishPermissions(fragment, publishPermissions)
                }
            } else {
                if (activity != null) {
                    loginManager.logInWithReadPermissions(activity, readPermissions)
                } else {
                    loginManager.logInWithReadPermissions(fragment, readPermissions)
                }
            }
            emitter.setCancellable {
                loginManager.unregisterCallback(callbackManager)
            }
        }
    }
}