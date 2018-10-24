## RxGoogleLogin
Reactive wrapper for Google login.

### Dependencies
```groovy
compile 'cz.ackee.rxsociallogin:google:x.x.x'
```

### Setup
1. You need to add `google_server_client_id` to your project string resources. You may find them in your Google project page.
2. Ensure you have a valid `google-services.json` file associated with your project.

### Usage
Create `RxGoogleLogin` and provide it a `Context`. By default, Google client is created with Email scope only, but you may request additional scopes in the constructor.

```kotlin
val rxGoogleLogin = RxGoogleLogin(context, listOf(/* add scopes here */))
```

When the user wants to sign up or sign in to your app, use one of the `login()` functions. You need to provide `Activity` or `Fragment` which will handle the result afterwards.

```kotlin
rxGoogleLogin.login(activity).subscribe({
            // Success, server auth code is in the result
        }, {
            // Error, handle if needed
        })
```

The result of this chain will be the server auth code string, use it then to authenticate with your server.

For the result to be pushed to the reactive chain you need to call `onActivityResult()` function from the provided `Activity` or `Fragment`.

```kotlin
fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    rxGoogleLogin.onActivityResult(requestCode, resultCode, data)
}
```

To log out and clear Google login data, call `logout()`.

```kotlin
rxGoogleLogin.logout()
```