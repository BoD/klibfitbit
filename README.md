# KLibGHealth

A [Google Health](https://developers.google.com/health) (previously Fitbit) API client library for Kotlin.

KLibGHealth is a Kotlin Multiplatform client for accessing Google Health data. It
handles OAuth authentication and currently supports reading and creating exercise
data points.

## Usage

Add the library to your Kotlin project:

```kotlin
dependencies {
  implementation("org.jraf.klibghealth:klibghealth:$latest_version")
}
```

Create a `GoogleHealthClient` with the OAuth client ID and secret from your
Google Cloud project. On the first run, request an authorization URL, send the
user to that URL, and exchange the callback URL for tokens. Store the returned
access and refresh tokens and provide them on subsequent runs:

```kotlin
val client = GoogleHealthClient(
  GoogleHealthClient.Configuration(
    auth = GoogleHealthClient.Configuration.Auth(
      clientId = clientId,
      clientSecret = clientSecret,
      oAuthTokens = savedTokens,
    ),
  ),
) { renewedTokens ->
  saveTokens(renewedTokens)
}

client.use {
  val authorization = it.oAuth.createAuthorizationUrl(
    GoogleHealthClient.Configuration.Auth.Scope.ActivityAndFitness.ReadOnly,
  )
  // Open authorization.authorizeUrl and obtain the callback URL.
  it.oAuth.fetchTokens(authorization, callbackUrl).getOrThrow()

  val dataPoints = it.dataPoint.getDataPointList(fromDate).getOrThrow()
}
```

Use `dataPoint.createDataPoint(...)` to add exercise data. See the
[`sample-jvm`](samples/sample-jvm/src/main/kotlin/Main.kt) module for a
complete example, including token renewal and additional scopes.
