/**
 * Shared constants for Ensō Legacy.
 * Product rules that must stay identical across web and mobile live here.
 */

/** Max photos attached to a single milestone (the "10-photo flow"). */
export const MAX_MILESTONE_PHOTOS = 10;

/** Free tier shows 3 non-intrusive ad placements. */
export const FREE_TIER_AD_PLACEMENTS = 3;

/** Ad experience per support tier (number of placements shown). */
export const AD_PLACEMENTS_BY_TIER = {
  free: FREE_TIER_AD_PLACEMENTS,
  donor: Math.ceil(FREE_TIER_AD_PLACEMENTS / 2), // ~50% fewer, footer removed
  mid: 0,
  legacy: 0,
} as const;

/** Trees planted via One Tree Planted. */
export const TREES_PLANTED_PER_ACCOUNT = 1;
export const TREES_PLANTED_PER_LEAF = 1;

/** Weather provider — free, no API key required. */
export const WEATHER_API_BASE = "https://api.open-meteo.com/v1";

/** Open-Meteo forecast endpoint used by the rain-check job. */
export const WEATHER_FORECAST_URL = `${WEATHER_API_BASE}/forecast`;
