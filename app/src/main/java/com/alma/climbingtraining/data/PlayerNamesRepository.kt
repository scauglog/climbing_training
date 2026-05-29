package com.alma.climbingtraining.data

interface PlayerNamesRepository {
    fun savePlayerNames(names: List<String>)
    fun loadPlayerNames(): List<String>
}
