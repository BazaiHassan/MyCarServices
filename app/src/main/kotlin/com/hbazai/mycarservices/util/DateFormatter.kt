package com.hbazai.mycarservices.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {

    fun format(context: Context, epochMillis: Long): String {
        return if (LocaleHelper.getSavedLanguage(context) == "fa") {
            JalaliCalendar.format(epochMillis)
        } else {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(epochMillis))
        }
    }

    fun formatShort(context: Context, epochMillis: Long): String {
        return if (LocaleHelper.getSavedLanguage(context) == "fa") {
            ltr(JalaliCalendar.formatShort(epochMillis))
        } else {
            SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(Date(epochMillis))
        }
    }
}