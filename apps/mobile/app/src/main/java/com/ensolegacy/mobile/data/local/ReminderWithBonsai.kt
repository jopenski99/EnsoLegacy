package com.ensolegacy.mobile.data.local

import androidx.room.Embedded

/** Care reminder joined with its tree's name, for cross-tree views like the Schedule tab. */
data class ReminderWithBonsai(
    @Embedded val reminder: CareReminderEntity,
    val bonsaiName: String,
)
