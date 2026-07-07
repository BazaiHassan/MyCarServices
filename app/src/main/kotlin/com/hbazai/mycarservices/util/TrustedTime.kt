package com.hbazai.mycarservices.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone

/**
 * Time source for saving records.
 *
 * Asks timeapi.io for the current time in the device's zone so saved dates
 * are correct even when the device clock is wrong. When there is no internet
 * or the API is slow ([TIMEOUT_MS]), it silently falls back to the device
 * clock. A successful response is kept as a clock offset for [CACHE_TTL_MS],
 * so at most one network call is made per period.
 */
object TrustedTime {

    private const val TIMEOUT_MS   = 2500L
    private const val CACHE_TTL_MS = 6 * 60 * 60 * 1000L

    @Volatile private var offsetMillis: Long? = null
    @Volatile private var offsetFetchedAt = 0L

    suspend fun now(): Long {
        val cachedOffset = offsetMillis
        if (cachedOffset != null &&
            System.currentTimeMillis() - offsetFetchedAt < CACHE_TTL_MS
        ) {
            return System.currentTimeMillis() + cachedOffset
        }

        val networkNow = withTimeoutOrNull(TIMEOUT_MS) {
            withContext(Dispatchers.IO) { fetchNetworkTime() }
        }

        return if (networkNow != null) {
            offsetMillis    = networkNow - System.currentTimeMillis()
            offsetFetchedAt = System.currentTimeMillis()
            networkNow
        } else {
            System.currentTimeMillis()
        }
    }

    private fun fetchNetworkTime(): Long? = try {
        val zone = TimeZone.getDefault().id
        val url  = URL("https://timeapi.io/api/Time/current/zone?timeZone=$zone")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 2000
            readTimeout    = 2000
            requestMethod  = "GET"
        }
        try {
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                // "dateTime" is the wall-clock time in the requested zone.
                LocalDateTime.parse(JSONObject(body).getString("dateTime"))
                    .atZone(ZoneId.of(zone))
                    .toInstant()
                    .toEpochMilli()
            } else {
                null
            }
        } finally {
            conn.disconnect()
        }
    } catch (_: Exception) {
        null
    }
}
