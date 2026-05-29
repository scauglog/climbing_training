package com.alma.climbingtraining.model

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    FRENCH("fr");

    companion object {
        fun fromTag(tag: String): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: ENGLISH
    }
}
