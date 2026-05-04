package com.hbazai.mycarservices.util

import android.content.Context

object PasswordManager {
    private const val PREFS_NAME = "car_service_prefs"
    private const val KEY_PASSWORD = "delete_password"
    private const val KEY_PASSWORD_SET = "password_is_set"

    fun isPasswordSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PASSWORD_SET, false)
    }

    fun savePassword(context: Context, password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PASSWORD, password)
            .putBoolean(KEY_PASSWORD_SET, true)
            .apply()
    }

    fun checkPassword(context: Context, input: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PASSWORD, "") == input
    }
}