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

package org.jraf.klibghealth.client

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jraf.klibghealth.internal.client.GoogleHealthClientImpl
import org.jraf.klibghealth.model.ExerciseType
import org.jraf.klibghealth.model.OAuthAuthorizationUrlAndCodeVerifier
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface GoogleHealthClient : AutoCloseable {
  class Configuration(
    val auth: Auth,
    val http: Http = Http(),
  ) {
    class Http(
      val loggingLevel: HttpLoggingLevel = HttpLoggingLevel.NONE,
      val httpProxy: HttpProxy? = null,
    ) {
      class HttpProxy(
        val host: String,
        val port: Int,
      )

      enum class HttpLoggingLevel {
        /**
         * No logs.
         */
        NONE,
        INFO,
        HEADERS,
        BODY,
        ALL,
      }
    }

    class Auth(
      val clientId: String,
      /**
       * Google requires client_secret even for desktop applications
       * https://developers.google.com/identity/protocols/oauth2/native-app
       * "The client_secret is not applicable to requests from clients registered as Android, iOS, or Chrome applications"
       */
      val clientSecret: String,
      val oAuthTokens: OAuthTokens?,
    ) {
      class OAuthTokens(
        val accessToken: String,
        val refreshToken: String,
      )

      // Not sealed to allow users to add scopes not defined here
      open class Scope(internal val scope: String) {
        interface ActivityAndFitness {
          object ReadOnly : Scope("https://www.googleapis.com/auth/googlehealth.activity_and_fitness.readonly")
          object WriteOnly : Scope("https://www.googleapis.com/auth/googlehealth.activity_and_fitness.writeonly")
        }

        interface Sleep {
          object ReadOnly : Scope("https://www.googleapis.com/auth/googlehealth.sleep.readonly")
          object WriteOnly : Scope("https://www.googleapis.com/auth/googlehealth.sleep.writeonly")
        }
      }
    }
  }

  interface OAuth {
    fun createAuthorizationUrl(vararg scopes: Configuration.Auth.Scope): OAuthAuthorizationUrlAndCodeVerifier

    suspend fun fetchTokens(
      oAuthAuthorizationUrlAndCodeVerifier: OAuthAuthorizationUrlAndCodeVerifier,
      authorizationCallbackUrl: String,
    ): Result<Unit>
  }

  val oAuth: OAuth

  interface DataPoint {
    /**
     * @param toDate is exclusive.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun getDataPointList(
      fromDate: LocalDate,
      // Default: tomorrow
      toDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(1, DateTimeUnit.DAY),
    ): Result<List<org.jraf.klibghealth.model.DataPoint>>

    @OptIn(ExperimentalTime::class)
    suspend fun createDataPoint(
      exerciseType: ExerciseType,
      startTime: Instant,
      activeDuration: Duration,
      distanceMeters: Double,
    ): Result<Unit>
  }

  val dataPoint: DataPoint
}

fun GoogleHealthClient(
  configuration: GoogleHealthClient.Configuration,
  onOAuthTokensRenewed: suspend (newOAuthTokens: GoogleHealthClient.Configuration.Auth.OAuthTokens) -> Unit,
): GoogleHealthClient = GoogleHealthClientImpl(configuration, onOAuthTokensRenewed)
