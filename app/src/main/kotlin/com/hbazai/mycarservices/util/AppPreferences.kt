package com.hbazai.mycarservices.util

import android.content.Context

object AppPreferences {
    private const val PREFS_NAME       = "car_service_prefs"
    private const val KEY_CURRENCY     = "currency"
    private const val KEY_DISTANCE     = "distance_unit"
    private const val KEY_SETUP_DONE   = "initial_setup_done"

    fun isSetupDone(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SETUP_DONE, false)

    fun markSetupDone(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SETUP_DONE, true).apply()

    fun getCurrency(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CURRENCY, "€") ?: "€"

    fun saveCurrency(context: Context, currency: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_CURRENCY, currency).apply()

    fun getDistanceUnit(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DISTANCE, "km") ?: "km"

    fun saveDistanceUnit(context: Context, unit: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_DISTANCE, unit).apply()

    fun formatCost(context: Context, amount: Double): String {
        val currency = getCurrency(context)
        
        // Currencies that don't use decimals
        val noDecimalCurrencies = listOf("﷼", "تومان")
        
        return if (currency in noDecimalCurrencies) {
            // Round to Int, format with thousands separator
            val formatted = String.format("%,d", amount.toLong())
            "${ltr(formatted)} $currency"
        } else {
            // Keep 2 decimals, format with thousands separator
            val formatted = String.format("%,.2f", amount)
            ltr("$currency $formatted")
        }
    }
}