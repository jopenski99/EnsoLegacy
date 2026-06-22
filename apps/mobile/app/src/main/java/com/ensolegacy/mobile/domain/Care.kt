package com.ensolegacy.mobile.domain

/**
 * A kind of recurring bonsai care task. [value] is the stable string persisted
 * in Room — keep it identical if this ever crosses into shared types.
 */
enum class CareType(val value: String, val label: String, val emoji: String) {
    REPOTTING("repotting", "Repotting", "🪴"),
    DEFOLIATION("defoliation", "Defoliation", "🍃"),
    PRUNING("pruning", "Pruning", "✂️"),
    HEALTH_CHECK("health_check", "Health Check", "❤️");

    companion object {
        fun fromValue(value: String): CareType =
            entries.firstOrNull { it.value == value } ?: HEALTH_CHECK
    }
}

/**
 * A suggested default care task for a species: how often it recurs and a
 * human-readable cadence. These are *starter* horticultural defaults grouped by
 * the species' `bonsaiType` — sensible, not authoritative; the owner can adjust.
 */
data class CareDefault(
    val type: CareType,
    val intervalDays: Int,
    val cadenceLabel: String,
)

object CareDefaults {

    /**
     * Default schedule for a species, derived from its `bonsaiType`. Defoliation
     * is only offered for deciduous trees (the spec's `canDefoliate` rule).
     */
    fun forBonsaiType(bonsaiType: String?): List<CareDefault> {
        val type = bonsaiType.orEmpty()
        val isDeciduous = type.contains("Deciduous", ignoreCase = true)

        val repot = when {
            type.contains("Succulent", ignoreCase = true) ->
                CareDefault(CareType.REPOTTING, 1095, "Every 3 years")
            type.contains("Tropical", ignoreCase = true) || type.contains("ficus", ignoreCase = true) ->
                CareDefault(CareType.REPOTTING, 540, "Every 18 months")
            else ->
                CareDefault(CareType.REPOTTING, 730, "Every 2 years")
        }

        return buildList {
            add(repot)
            if (isDeciduous) add(CareDefault(CareType.DEFOLIATION, 365, "Once a year"))
            add(CareDefault(CareType.PRUNING, 90, "Every 3 months"))
            add(CareDefault(CareType.HEALTH_CHECK, 30, "Every 30 days"))
        }
    }
}

/** How a care task relates to its due date — shown as a status pill. */
enum class CareStatus(val label: String) {
    ON_SCHEDULE("On schedule"),
    DUE_SOON("Due soon"),
    OVERDUE("Overdue"),
}

/** Due in the past = overdue; within ~14 days = due soon; otherwise on schedule. */
fun careStatusOf(nextDueAt: Long, now: Long = System.currentTimeMillis()): CareStatus {
    val daysUntil = (nextDueAt - now) / (24L * 60 * 60 * 1000)
    return when {
        daysUntil < 0 -> CareStatus.OVERDUE
        daysUntil <= 14 -> CareStatus.DUE_SOON
        else -> CareStatus.ON_SCHEDULE
    }
}

/** Human-readable relative due label for a reminder (e.g. "Overdue by 3 days", "Due tomorrow"). */
fun relativeDueLabel(nextDueAt: Long, now: Long = System.currentTimeMillis()): String {
    val daysUntil = (nextDueAt - now) / (24L * 60 * 60 * 1000)
    return when {
        daysUntil < 0 -> {
            val d = -daysUntil
            if (d == 0L) "Overdue today" else if (d == 1L) "Overdue by 1 day" else "Overdue by $d days"
        }
        daysUntil == 0L -> "Due today"
        daysUntil == 1L -> "Due tomorrow"
        daysUntil <= 30 -> "Due in $daysUntil days"
        else -> "Due in $daysUntil days"
    }
}
