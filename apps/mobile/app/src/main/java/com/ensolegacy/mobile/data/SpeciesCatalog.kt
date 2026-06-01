package com.ensolegacy.mobile.data

import android.content.Context
import com.ensolegacy.mobile.domain.Species
import org.json.JSONArray

/**
 * Read-only starter species catalog, loaded once from the bundled
 * `assets/species.json` asset (the hand-mirror of `BONSAI_SPECIES` in
 * packages/shared). Static reference data, so it lives outside Room.
 */
class SpeciesCatalog(context: Context) {

    val all: List<Species> by lazy { load(context) }

    private fun load(context: Context): List<Species> {
        val json = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            Species(
                commonName = obj.getString("commonName"),
                scientificName = obj.getString("scientificName"),
                family = obj.getString("family"),
                bonsaiType = obj.getString("bonsaiType"),
            )
        }
    }

    private companion object {
        const val ASSET_NAME = "species.json"
    }
}
