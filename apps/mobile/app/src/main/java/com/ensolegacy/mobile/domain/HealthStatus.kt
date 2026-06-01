package com.ensolegacy.mobile.domain

/**
 * Care status of a tree, surfaced on the Health view.
 *
 * Mirrors the shared `HealthStatus` union in packages/shared. [value] is the
 * stable string persisted in Room — keep it identical to the shared values.
 */
enum class HealthStatus(val value: String, val label: String) {
    HEALTHY("healthy", "Healthy"),
    NEEDS_CARE("needs_care", "Needs Care"),
    CRITICAL("critical", "Critical");

    companion object {
        /** Resolve a stored [value] back to a status, defaulting to [HEALTHY]. */
        fun fromValue(value: String): HealthStatus =
            entries.firstOrNull { it.value == value } ?: HEALTHY
    }
}
