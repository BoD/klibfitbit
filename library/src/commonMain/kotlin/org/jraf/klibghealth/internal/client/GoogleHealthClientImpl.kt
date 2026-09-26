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

package org.jraf.klibghealth.internal.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.toByteString
import org.jraf.klibghealth.client.GoogleHealthClient
import org.jraf.klibghealth.client.GoogleHealthClient.Configuration
import org.jraf.klibghealth.internal.json.JsonDataPoint
import org.jraf.klibghealth.internal.model.ExerciseImpl
import org.jraf.klibghealth.internal.model.OAuthAuthorizationUrlAndCodeVerifierImpl
import org.jraf.klibghealth.model.DataPoint
import org.jraf.klibghealth.model.ExerciseType
import org.jraf.klibghealth.model.OAuthAuthorizationUrlAndCodeVerifier
import org.jraf.klibnanolog.logd
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal class GoogleHealthClientImpl(
  private var clientConfiguration: Configuration,
  private val onOAuthTokensRenewed: suspend (newOAuthTokens: Configuration.Auth.OAuthTokens) -> Unit,
) : GoogleHealthClient {
  private val service: GoogleHealthService by lazy {
    GoogleHealthService(provideHttpClient())
  }

  private fun provideHttpClient(): HttpClient {
    return HttpClient {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            useAlternativeNames = false
          },
        )
      }
      install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 60_000
        socketTimeoutMillis = 60_000
      }
      engine {
        // Set up a proxy if requested
        clientConfiguration.http.httpProxy?.let { httpProxy ->
          proxy = ProxyBuilder.http(
            URLBuilder().apply {
              host = httpProxy.host
              port = httpProxy.port
            }.build(),
          )
        }
      }
      install(Auth) {
        bearer {
          loadTokens {
            clientConfiguration.auth.oAuthTokens?.let { oAuthTokens ->
              BearerTokens(
                accessToken = oAuthTokens.accessToken,
                refreshToken = oAuthTokens.refreshToken,
              )
            }
          }

          refreshTokens {
            val refreshTokenResponse = service.newToken(
              oAuthRefreshToken = clientConfiguration.auth.oAuthTokens!!.refreshToken,
              clientId = clientConfiguration.auth.clientId,
              clientSecret = clientConfiguration.auth.clientSecret,
            )
            val oAuthTokens = Configuration.Auth.OAuthTokens(
              accessToken = refreshTokenResponse.access_token,
              refreshToken = clientConfiguration.auth.oAuthTokens!!.refreshToken,
            )
            saveOAuthTokens(oAuthTokens)
            BearerTokens(
              accessToken = oAuthTokens.accessToken,
              refreshToken = oAuthTokens.refreshToken,
            )
          }
        }
      }

      // Setup logging if requested
      if (clientConfiguration.http.loggingLevel != Configuration.Http.HttpLoggingLevel.NONE) {
        install(Logging) {
          logger = object : Logger {
            override fun log(message: String) {
              logd(message)
            }
          }
          level = when (clientConfiguration.http.loggingLevel) {
            Configuration.Http.HttpLoggingLevel.NONE -> LogLevel.NONE
            Configuration.Http.HttpLoggingLevel.INFO -> LogLevel.INFO
            Configuration.Http.HttpLoggingLevel.HEADERS -> LogLevel.HEADERS
            Configuration.Http.HttpLoggingLevel.BODY -> LogLevel.BODY
            Configuration.Http.HttpLoggingLevel.ALL -> LogLevel.ALL
          }
        }
      }
    }
  }

  private suspend fun saveOAuthTokens(oAuthTokens: Configuration.Auth.OAuthTokens) {
    // Update client configuration with new tokens
    clientConfiguration = Configuration(
      http = clientConfiguration.http,
      auth = Configuration.Auth(
        clientId = clientConfiguration.auth.clientId,
        clientSecret = clientConfiguration.auth.clientSecret,
        oAuthTokens = oAuthTokens,
      ),
    )

    // Inform the client that new tokens are available
    onOAuthTokensRenewed(oAuthTokens)
  }

  override val oAuth: GoogleHealthClient.OAuth = object : GoogleHealthClient.OAuth {
    override fun createAuthorizationUrl(vararg scopes: Configuration.Auth.Scope): OAuthAuthorizationUrlAndCodeVerifier {
      val randomLetterList: List<Char> = (1..Random.nextInt(43..128))
        .map { Random.nextInt(from = 'a'.code, until = 'z'.code).toChar() }
      val codeVerifier = randomLetterList.toCharArray().concatToString()

      // A SHA-256 hash of the code verifier, base64url encoded with padding omitted, called the code challenge
      val codeChallenge = randomLetterList.map { it.code.toByte() }.toByteArray().toByteString().sha256().base64Url().removeSuffix("=")

      val url = URLBuilder("https://accounts.google.com/o/oauth2/v2/auth").apply {
        parameters.apply {
          append("client_id", clientConfiguration.auth.clientId)
          append("response_type", "code")
          append("scope", scopes.toSet().joinToString(" ") { it.scope })
          append("code_challenge_method", "S256")
          append("code_challenge", codeChallenge)
          append("redirect_uri", "http://localhost")
        }
      }.buildString()

      return OAuthAuthorizationUrlAndCodeVerifierImpl(
        authorizeUrl = url,
        codeVerifier = codeVerifier,
      )
    }

    override suspend fun fetchTokens(
      oAuthAuthorizationUrlAndCodeVerifier: OAuthAuthorizationUrlAndCodeVerifier,
      authorizationCallbackUrl: String,
    ): Result<Unit> = runCatching {
      val url = Url(authorizationCallbackUrl)
      val code: String = url.parameters["code"] ?: throw IllegalArgumentException("No code parameter in callback URL")
      val jsonOAuthTokens = service.createOAuthTokens(
        code = code,
        codeVerifier = oAuthAuthorizationUrlAndCodeVerifier.codeVerifier,
        clientId = clientConfiguration.auth.clientId,
        clientSecret = clientConfiguration.auth.clientSecret,
      )
      saveOAuthTokens(
        oAuthTokens = Configuration.Auth.OAuthTokens(
          accessToken = jsonOAuthTokens.access_token,
          refreshToken = jsonOAuthTokens.refresh_token,
        ),
      )
    }
  }

  override val dataPoint: GoogleHealthClient.DataPoint = object : GoogleHealthClient.DataPoint {
    @OptIn(FormatStringsInDatetimeFormats::class)
    override suspend fun getDataPointList(
      fromDate: LocalDate,
      toDate: LocalDate,
    ): Result<List<DataPoint>> = runCatching {
      val dateTimeFormat = LocalDate.Format {
        byUnicodePattern("yyyy-MM-dd")
      }
      val fromDateStr = dateTimeFormat.format(fromDate)
      val toDateStr = dateTimeFormat.format(toDate)

      val jsonDataPoints = service.getDataPointList(fromDate = fromDateStr, toDate = toDateStr)
      jsonDataPoints.dataPoints.map { it.toDataPoint() }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun createDataPoint(
      exerciseType: ExerciseType,
      startTime: Instant,
      activeDuration: Duration,
      distanceMeters: Double,
    ): Result<Unit> = runCatching {
      service.createDataPoint(
        exerciseType = exerciseType.exerciseType,
        startTime = startTime,
        activeDuration = activeDuration,
        distanceMillimeters = (distanceMeters * 1000).toInt(),
      )
    }
  }

  override fun close() {
    service.close()
  }
}

@OptIn(ExperimentalTime::class)
private fun JsonDataPoint.toDataPoint(): DataPoint.Exercise {
  return ExerciseImpl(
    name = name!!,
    startTime = Instant.parse(exercise.interval.startTime),
    endTime = Instant.parse(exercise.interval.endTime),
    // Duration is like "123s"
    activeDuration = Duration.parse(exercise.activeDuration),
    exerciseType = ExerciseType.fromString(exercise.exerciseType),
    caloriesKcal = exercise.metricsSummary.caloriesKcal.toInt(),
    distanceMeters = exercise.metricsSummary.distanceMillimeters / 1000.0,
  )
}
