package com.ensolegacy.mobile.domain

enum class AcquisitionSource(val value: String, val label: String) {
    COLLECTED("collected", "Collected"),
    NURSERY_STOCK("nursery_stock", "Nursery Stock"),
    CUTTING("cutting", "Cutting"),
    SEEDLING("seedling", "Seedling");

    companion object {
        fun fromValue(value: String?): AcquisitionSource? =
            entries.firstOrNull { it.value == value }
    }
}
