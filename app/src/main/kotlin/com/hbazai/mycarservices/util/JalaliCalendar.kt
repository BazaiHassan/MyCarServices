package com.hbazai.mycarservices.util

import android.icu.util.Calendar
import android.icu.util.ULocale

/**
 * Jalali (Persian) calendar conversions backed by Android's built-in ICU
 * Persian calendar, which is exact in both directions.
 *
 * (Replaces a hand-rolled converter whose epoch was anchored at 1600-01-01
 * instead of Nowruz, shifting every displayed date ~79 days ahead.)
 */
object JalaliCalendar {

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    private fun persianCalendar(): Calendar =
        Calendar.getInstance(ULocale("fa_IR@calendar=persian"))

    /** Epoch millis → (jalali year, month 1..12, day 1..31). */
    fun toJalali(epochMillis: Long): Triple<Int, Int, Int> {
        val cal = persianCalendar().apply { timeInMillis = epochMillis }
        return Triple(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** (jalali year, month 1..12, day) → epoch millis at local noon. */
    fun toEpochMillis(jy: Int, jm: Int, jd: Int): Long =
        persianCalendar().apply {
            clear()
            set(jy, jm - 1, jd, 12, 0, 0)
        }.timeInMillis

    /** Number of days in the given Jalali month (handles leap Esfand). */
    fun monthLength(jy: Int, jm: Int): Int =
        persianCalendar().apply {
            clear()
            set(jy, jm - 1, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

    fun todayJalali(): Triple<Int, Int, Int> = toJalali(System.currentTimeMillis())

    fun format(epochMillis: Long): String {
        val (jy, jm, jd) = toJalali(epochMillis)
        return "$jd ${monthNames[jm - 1]} $jy"
    }

    fun formatShort(epochMillis: Long): String {
        val (jy, jm, jd) = toJalali(epochMillis)
        // Year-first so the date reads left-to-right: 1404/04/12
        return "$jy/${jm.toString().padStart(2, '0')}/${jd.toString().padStart(2, '0')}"
    }
}
