package com.hbazai.mycarservices.util

object JalaliCalendar {

    private val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val g_d_no: Int
        val j_d_no: Int
        val j_np: Int

        val gDaysInMonth = intArrayOf(31, 28 + if (gy % 4 == 0 && (gy % 100 != 0 || gy % 400 == 0)) 1 else 0,
            31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        var jy = gy - 1600
        var jm: Int
        var jd: Int
        var gy2 = gy - 600

        var i = 0
        var gDayNo = 365 * (gy - 1) + (gy - 1) / 4 - (gy - 1) / 100 + (gy - 1) / 400
        i = 0
        while (i < gm - 1) {
            gDayNo += gDaysInMonth[i]
            i++
        }
        gDayNo += gd - 1

        var jDayNo = gDayNo - 79

        j_np = jDayNo / 12053
        jDayNo %= 12053

        jy = 979 + 33 * j_np + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        i = 0
        while (i < 11 && jDayNo >= jDaysInMonth[i]) {
            jDayNo -= jDaysInMonth[i]
            i++
        }
        jm = i + 1
        jd = jDayNo + 1

        return Triple(jy, jm, jd)
    }

    fun format(epochMillis: Long): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = epochMillis }
        val (jy, jm, jd) = toJalali(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
        val months = listOf(
            "فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
            "مهر","آبان","آذر","دی","بهمن","اسفند"
        )
        return "$jd ${months[jm - 1]} $jy"
    }
}