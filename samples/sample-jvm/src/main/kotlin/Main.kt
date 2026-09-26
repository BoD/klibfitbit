/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2025-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 * and contributors (https://github.com/BoD/klibghealth/graphs/contributors)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jraf.klibghealth.client.GoogleHealthClient
import org.jraf.klibghealth.client.GoogleHealthClient.Configuration.Auth
import org.jraf.klibghealth.client.GoogleHealthClient.Configuration.Auth.OAuthTokens
import org.jraf.klibghealth.client.GoogleHealthClient.Configuration.Auth.Scope
import org.jraf.klibghealth.model.ExerciseType
import org.jraf.klibnanolog.logd
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

suspend fun main(av: Array<String>) {
  GoogleHealthClient(
    GoogleHealthClient.Configuration(
      Auth(
        clientId = av[0],
        clientSecret = av[1],
        // You can pass null the first time when you don't have OAuth tokens yet.
        // Otherwise, pass the accessToken/refreshToken that are logged below.
        oAuthTokens = OAuthTokens(
          accessToken = av[2],
          refreshToken = av[3],
        ),
      ),
      GoogleHealthClient.Configuration.Http(
        loggingLevel = GoogleHealthClient.Configuration.Http.HttpLoggingLevel.ALL,
      ),
    ),
  ) { newOAuthTokens ->
    logd("accessToken: " + newOAuthTokens.accessToken)
    logd("refreshToken: " + newOAuthTokens.refreshToken)
  }.use { googleHealthClient ->
    // Do this only the first time, to get OAuth tokens
    val fetchTokens = false
    if (fetchTokens) {
      val authorizationUrlAndCodeVerifier = googleHealthClient.oAuth.createAuthorizationUrl(
        Scope.ActivityAndFitness.ReadOnly,
        Scope.ActivityAndFitness.WriteOnly,
        Scope.Sleep.ReadOnly,
        Scope.Sleep.WriteOnly,
      )
      println("Please visit this URL: ${authorizationUrlAndCodeVerifier.authorizeUrl}")
      println("Enter the callback URL:")
      val callbackUrl = readln().trim()
      googleHealthClient.oAuth.fetchTokens(authorizationUrlAndCodeVerifier, callbackUrl).getOrThrow()
    }

    // Create new exercise data point
    googleHealthClient.dataPoint.createDataPoint(
      exerciseType = ExerciseType.TreadmillWalk,
      startTime = (Clock.System.now() - 5.minutes),
      activeDuration = 3.minutes,
      distanceMeters = 342.5,
    ).getOrThrow()

    // Get all activities from today
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    logd(googleHealthClient.dataPoint.getDataPointList(today).getOrThrow())
  }
}
