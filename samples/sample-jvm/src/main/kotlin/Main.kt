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
 * and contributors (https://github.com/BoD/klibfitbit/graphs/contributors)
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
import org.jraf.klibfitbit.client.FitbitClient
import org.jraf.klibfitbit.client.configuration.ClientConfiguration
import org.jraf.klibfitbit.client.configuration.HttpConfiguration
import org.jraf.klibfitbit.client.configuration.HttpLoggingLevel
import org.jraf.klibfitbit.client.configuration.OAuthTokens
import org.jraf.klibfitbit.model.ExerciseType
import org.jraf.klibnanolog.logd
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

suspend fun main(av: Array<String>) {
  FitbitClient.newInstance(
    ClientConfiguration(
      clientId = av[0],
      clientSecret = av[1],
      // You can pass null the first time when you don't have OAuth tokens yet.
      // Otherwise, pass the accessToken/refreshToken that are logged below
      oAuthTokens = OAuthTokens(
        accessToken = av[2],
        refreshToken = av[3],
      ),
      httpConfiguration = HttpConfiguration(
        loggingLevel = HttpLoggingLevel.ALL,
      ),
    ),
  ) { oAuthTokens ->
    logd("accessToken: " + oAuthTokens.accessToken)
    logd("refreshToken: " + oAuthTokens.refreshToken)
  }.use { fitbitClient ->
    // Do this only the first time, to get OAuth tokens
    val fetchTokens = false
    if (fetchTokens) {
      val authorizationUrlResult = fitbitClient.oAuthCreateAuthorizationUrl(
        listOf(
          "https://www.googleapis.com/auth/googlehealth.activity_and_fitness.readonly",
          "https://www.googleapis.com/auth/googlehealth.activity_and_fitness.writeonly",
          "https://www.googleapis.com/auth/googlehealth.sleep.readonly",
          "https://www.googleapis.com/auth/googlehealth.sleep.writeonly",
        ),
      )
      println("Please visit this URL: ${authorizationUrlResult.authorizeUrl}")
      println("Enter the callback URL:")
      val callbackUrl = readln().trim()
      fitbitClient.oAuthFetchTokens(authorizationUrlResult, callbackUrl)
    }

    // Create new exercise data point
    fitbitClient.createDataPoint(
      exerciseType = ExerciseType.TREADMILL_WALK,
      startTime = (Clock.System.now() - 5.minutes),
      activeDuration = 3.minutes,
      distanceMeters = 342.5,
    )

    // Get all activities from today
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    logd(fitbitClient.getDataPointList(today))
  }
}
