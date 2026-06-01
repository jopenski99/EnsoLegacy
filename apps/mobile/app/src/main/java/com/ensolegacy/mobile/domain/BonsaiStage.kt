package com.ensolegacy.mobile.domain

/**
 * Life stage of a tree. Stage transitions are tracked events.
 *
 * Mirrors the shared `BonsaiStage` union in packages/shared. [value] is the
 * stable string persisted in Room and serialized across the web boundary —
 * keep it identical to the shared values.
 */
enum class BonsaiStage(val value: String, val label: String) {
    JUVENILE("juvenile", "Juvenile"),
    IN_TRAINING("in_training", "In Training"),
    MATURE("mature", "Mature");

    companion object {
        /** Resolve a stored [value] back to a stage, defaulting to [JUVENILE]. */
        fun fromValue(value: String): BonsaiStage =
            entries.firstOrNull { it.value == value } ?: JUVENILE
    }
}
