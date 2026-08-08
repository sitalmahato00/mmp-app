package com.example.mmp_app.core.utils

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object NepaliDateUtils {

    val bsMonthNames = arrayOf(
        "बैशाख", "जेठ", "असार", "श्रावण", "भाद्र", "आश्विन",
        "कार्तिक", "मंसिर", "पौष", "माघ", "फाल्गुण", "चैत्र"
    )

    val bsMonthNamesEn = arrayOf(
        "Baisakh", "Jestha", "Ashad", "Shrawan", "Bhadra", "Ashwin",
        "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
    )

    private val bsData = arrayOf(
        intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2000
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2001
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2002
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2003
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2004
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2005
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2006
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2007
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2008
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2009
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2010
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2011
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2012
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2013
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2014
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2015
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2016
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2017
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2018
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2019
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2020
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2021
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2022
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2023
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2024
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2025
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2026
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2027
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2028
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2029
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2030
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2031
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2032
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2033
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2034
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2035
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2036
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2037
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2038
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2039
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2040
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2041
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2042
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2043
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2044
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2045
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2046
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2047
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2048
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2049
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2050
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2051
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2052
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2053
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2054
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2055
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2056
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2057
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2058
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2059
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2060
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2061
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2062
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2063
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2064
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2065
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2066
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2067
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2068
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2069
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2070
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2071
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2072
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2073
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2074
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2075
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2076
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2077
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2078
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2079
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2080
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2081
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2082
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2083
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2084
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2085
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2086
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2087
        intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 30, 29, 30), // 2088
        intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 30, 30, 30), // 2089
        intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31)  // 2090
    )

    private val minBsYear = 2000
    private val maxBsYear = 2090

    // BS 2000-01-01 was AD 1943-04-14
    private fun getReferenceCalendar(): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(1943, Calendar.APRIL, 14, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun adToBs(year: Int, month: Int, day: Int): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(year, month - 1, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        val refCal = getReferenceCalendar()
        var totalDays = ((cal.timeInMillis - refCal.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()

        if (totalDays < 0) return "Before 2000 BS"

        var bsYear = minBsYear
        var bsMonth = 1

        var yearIdx = 0
        while (yearIdx < bsData.size) {
            val yearDays = bsData[yearIdx].sum()
            if (totalDays < yearDays) break
            totalDays -= yearDays
            bsYear++
            yearIdx++
        }

        if (yearIdx >= bsData.size) return "After 2090 BS"

        var monthIdx = 0
        while (monthIdx < 12) {
            val monthDays = bsData[yearIdx][monthIdx]
            if (totalDays < monthDays) break
            totalDays -= monthDays
            bsMonth++
            monthIdx++
        }

        val bsDay = totalDays + 1

        return String.format("%04d-%02d-%02d", bsYear, bsMonth, bsDay)
    }

    fun bsToAd(bsDate: String): String {
        val parts = bsDate.split("-")
        if (parts.size != 3) return ""
        return bsToAd(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }

    fun bsToAd(year: Int, month: Int, day: Int): String {
        if (year < minBsYear || year > maxBsYear) return ""

        var totalDays = 0L
        for (y in minBsYear until year) {
            totalDays += bsData[y - minBsYear].sum()
        }

        for (m in 1 until month) {
            totalDays += bsData[year - minBsYear][m - 1]
        }

        totalDays += (day - 1)

        val refCal = getReferenceCalendar()
        refCal.add(Calendar.DAY_OF_YEAR, totalDays.toInt())
        
        val y = refCal.get(Calendar.YEAR)
        val m = refCal.get(Calendar.MONTH) + 1
        val d = refCal.get(Calendar.DAY_OF_MONTH)
        
        return String.format("%04d-%02d-%02d", y, m, d)
    }

    fun getTodayBs(): String {
        val today = Calendar.getInstance()
        return adToBs(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH))
    }

    fun formatBsDisplay(bsDate: String): String = "$bsDate BS"

    fun formatBsWithMonthName(bsDate: String): String {
        return try {
            val parts = bsDate.split("-")
            val y = parts[0]
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            "$d ${bsMonthNames[m - 1]} $y"
        } catch (e: Exception) { bsDate }
    }

    fun getDaysInBsMonth(year: Int, month: Int): Int {
        if (year < minBsYear || year > maxBsYear || month < 1 || month > 12) return 30
        return bsData[year - minBsYear][month - 1]
    }

    fun isValidBsDate(bsDate: String): Boolean {
        return try {
            val parts = bsDate.split("-")
            if (parts.size != 3) return false
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            if (y < minBsYear || y > maxBsYear) return false
            if (m !in 1..12) return false
            val maxDays = getDaysInBsMonth(y, m)
            d in 1..maxDays
        } catch (e: Exception) { false }
    }
}
