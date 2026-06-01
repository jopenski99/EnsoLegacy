// Supabase Edge Function (Deno runtime).
// Example: query Open-Meteo for rain in the next window. The mobile app does
// its own local WorkManager check too; this exists for server-side triggers.
//
// Deploy:  supabase functions deploy weather-rain-check
// Invoke:  GET /weather-rain-check?lat=7.07&lon=125.61

Deno.serve(async (req) => {
  const url = new URL(req.url);
  const lat = url.searchParams.get("lat");
  const lon = url.searchParams.get("lon");

  if (!lat || !lon) {
    return new Response(JSON.stringify({ error: "lat and lon are required" }), {
      status: 400,
      headers: { "content-type": "application/json" },
    });
  }

  const api = new URL("https://api.open-meteo.com/v1/forecast");
  api.searchParams.set("latitude", lat);
  api.searchParams.set("longitude", lon);
  api.searchParams.set("hourly", "precipitation_probability");
  api.searchParams.set("forecast_days", "1");

  const res = await fetch(api);
  const data = await res.json();

  return new Response(JSON.stringify(data), {
    headers: { "content-type": "application/json" },
  });
});
