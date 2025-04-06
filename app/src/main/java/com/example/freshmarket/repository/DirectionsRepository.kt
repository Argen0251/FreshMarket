    package com.example.freshmarket.repository

    import android.util.Log
    import com.google.android.gms.maps.model.LatLng
    import com.google.maps.android.PolyUtil
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext
    import okhttp3.OkHttpClient
    import okhttp3.Request
    import org.json.JSONObject

    class DirectionsRepository {

        private val client = OkHttpClient()

        /**
         * Запрашивает маршрут между [originLatLng] и [destinationLatLng] через Directions API,
         * и возвращает список точек (LatLng), декодированных из overview_polyline.
         */
        suspend fun getRoutePoints(
            apiKey: String,
            originLatLng: LatLng,
            destinationLatLng: LatLng
        ): List<LatLng> = withContext(Dispatchers.IO) {
            val origin = "${originLatLng.latitude},${originLatLng.longitude}"
            val destination = "${destinationLatLng.latitude},${destinationLatLng.longitude}"
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=$origin&destination=$destination&mode=driving&key=$apiKey"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList<LatLng>()

            // Логирование JSON-ответа для отладки
            Log.d("Directions", "JSON response: $body")

            val json = JSONObject(body)
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) {
                Log.e("Directions", "No routes found")
                return@withContext emptyList<LatLng>()
            }

            val overviewPolyline = routes
                .getJSONObject(0)
                .getJSONObject("overview_polyline")
                .getString("points")

            return@withContext PolyUtil.decode(overviewPolyline)
        }
    }
