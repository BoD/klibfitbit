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

package org.jraf.klibfitbit.internal.json

import kotlinx.serialization.Serializable

@Serializable
data class JsonInterval(
  val startTime: String,
  val startUtcOffset: String,
  val endTime: String,
  val endUtcOffset: String,
)

@Serializable
data class MetricsSummary(
  val caloriesKcal: Float = 0f,
  val distanceMillimeters: Int = 0,
)

@Serializable
data class JsonExercise(
  val interval: JsonInterval,
  val activeDuration: String,
  val exerciseType: String,
  val metricsSummary: MetricsSummary,
)

@Serializable
data class JsonDataPoint(
  val name: String? = null,
  val exercise: JsonExercise,
)


@Serializable
data class JsonDataPoints(
  val dataPoints: List<JsonDataPoint> = emptyList(),
)
