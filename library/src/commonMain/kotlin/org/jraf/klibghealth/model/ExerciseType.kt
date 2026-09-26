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

package org.jraf.klibghealth.model

// Not sealed to allow users to add types not defined here
open class ExerciseType(val exerciseType: String) {
  object ExerciseTypeUnspecified : ExerciseType("EXERCISE_TYPE_UNSPECIFIED")
  object Running : ExerciseType("RUNNING")
  object Walking : ExerciseType("WALKING")
  object Biking : ExerciseType("BIKING")
  object Swimming : ExerciseType("SWIMMING")
  object Hiking : ExerciseType("HIKING")
  object Yoga : ExerciseType("YOGA")
  object Pilates : ExerciseType("PILATES")
  object Workout : ExerciseType("WORKOUT")
  object Hiit : ExerciseType("HIIT")
  object Weightlifting : ExerciseType("WEIGHTLIFTING")
  object Spinning : ExerciseType("SPINNING")
  object StrengthTraining : ExerciseType("STRENGTH_TRAINING")
  object Treadmill : ExerciseType("TREADMILL")
  object TreadmillWalk : ExerciseType("TREADMILL_WALK")
  object Other : ExerciseType("OTHER")

  class Unknown(exerciseType: String) : ExerciseType(exerciseType)

  internal companion object {
    private val values = setOf(
      ExerciseTypeUnspecified,
      Running,
      Walking,
      Biking,
      Swimming,
      Hiking,
      Yoga,
      Pilates,
      Workout,
      Hiit,
      Weightlifting,
      Spinning,
      StrengthTraining,
      Treadmill,
      TreadmillWalk,
      Other,
    )

    fun fromString(exerciseType: String): ExerciseType {
      return values.firstOrNull { it.exerciseType == exerciseType } ?: Unknown(exerciseType)
    }
  }
}
