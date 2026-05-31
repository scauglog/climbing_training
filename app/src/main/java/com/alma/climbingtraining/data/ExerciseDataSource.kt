package com.alma.climbingtraining.data

import com.alma.climbingtraining.model.Exercise

interface ExerciseDataSource {
    fun loadExercises(): List<Exercise>
}
