package com.hbazai.mycarservices.util

import java.util.Calendar

object JalaliCalendar {

    fun format(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        val (jy, jm, jd) = toJalali(gy, gm, gd)
        val months = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )
        return "$jd ${months[jm - 1]} $jy"
    }

    fun formatShort(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        val (jy, jm, jd) = toJalali(gy, gm, gd)
        // Year-first so the date reads left-to-right: 1404/04/12
        return "$jy/${jm.toString().padStart(2, '0')}/${jd.toString().padStart(2, '0')}"
    }

    private fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val g2j = g2d(gy, gm, gd) - g2d(1600, 1, 1)

        var jy = 979
        val remaining: Int

        val cycle = g2j / 12053
        val rem1  = g2j % 12053

        jy += 33 * cycle
        jy += 4 * (rem1 / 1461)
        val rem2 = rem1 % 1461

        if (rem2 >= 366) {
            jy += (rem2 - 1) / 365
            remaining = (rem2 - 1) % 365
        } else {
            remaining = rem2
        }

        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        var jm = 0
        var left = remaining
        for (i in 0..11) {
            if (left < jDaysInMonth[i]) {
                jm = i + 1
                break
            }
            left -= jDaysInMonth[i]
        }
        val jd = left + 1

        return Triple(jy, jm, jd)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var y = gy - 1600
        var m = gm - 1
        val d = gd - 1

        var jdn = 365 * y + (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400

        val daysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if ((gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0) daysInMonth[1] = 29

        for (i in 0 until m) jdn += daysInMonth[i]
        jdn += d

        return jdn
    }
}