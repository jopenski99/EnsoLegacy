package com.ensolegacy.mobile.domain

enum class Placement(val value: String, val label: String) {
    OUTDOOR("outdoor", "Outdoor"),
    INDOOR("indoor", "Indoor");

    companion object {
        fun fromValue(value: String?): Placement? =
            entries.firstOrNull { it.value == value }
    }
}
