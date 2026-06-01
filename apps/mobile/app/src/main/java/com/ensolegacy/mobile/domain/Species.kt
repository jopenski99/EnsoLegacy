package com.ensolegacy.mobile.domain

/**
 * A bonsai-suitable species from the starter catalog.
 *
 * Mirrors the shared `Species` type in packages/shared. The data itself ships
 * as a bundled asset (`assets/species.json`) and is loaded by [com.ensolegacy.mobile.data.SpeciesCatalog].
 */
data class Species(
    val commonName: String,
    val scientificName: String,
    val family: String,
    val bonsaiType: String,
)
