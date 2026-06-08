package com.ensolegacy.mobile.domain

/** Collected form state passed from AddTreeSheet to the caller's onSave lambda. */
data class TreeFormData(
    val name: String,
    val species: String,
    val stage: BonsaiStage,
    val health: HealthStatus,
    val acquiredYear: Int?,
    val acquisitionSource: AcquisitionSource?,
    val placement: Placement?,
    val origin: String?,
    val acquiredFrom: String?,
)
