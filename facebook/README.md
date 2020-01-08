# RxSocialLogin

For all library parts to work, you need to add Core dependency

```groovy
compile 'cz.ackee.rxsociallogin:core:x.x.x'
```

## RxFacebookLogin
Reactive wrapper for Facebook login.

### Dependencies
```groovy
compile 'cz.ackee.rxsociallogin:facebook:x.x.x'
```

### Setup
Follow [Getting started](https://developers.facebook.com/docs/facebook-login/android/) and add newest dependency to Facebook SDK along with AndroidManifest setup. You also need to setup your app in Facebook developers [page](https://developers.facebook.com/apps) 

### Usage
Create `RxFacebookLogin`, you may then provide it via dependency injection or use directly.

```kotlin
val rxFacebookLogin = RxFacebookLogin()
```

When the user wants to sign up or sign in to your app, use one of the `login()` functions. You need to provide `Activity` or `Fragment` which will handle the result afterwards. Moreover, you may provide additional permissions. Since Facebook login may be performed with only one type of permissions, if at least one publish permission is added, the login will be performed with publish permissions. Otherwise, read permissions are used. By default, "public_profile" is the only one permission used.

```kotlin
rxFacebookLogin.login(activity).subscribe({
            // Success, token is in the result
        }, {
            // Error, handle if needed
        })
```

The result of this chain will be the access token string, use it then to authenticate with your server.

For the result to be pushed to the reactive chain you need to call `onActivityResult()` function from the provided `Activity` or `Fragment`.

```kotlin
fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    rxFacebookLogin.onActivityResult(requestCode, resultCode, data)
}
```

To log out and clear Facebook login data, call `logout()`.

```kotlin
rxFacebookLogin.logout()
```