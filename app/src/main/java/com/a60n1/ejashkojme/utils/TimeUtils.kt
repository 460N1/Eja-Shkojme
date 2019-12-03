package com.a60n1.ejashkojme.utils

import android.annotation.SuppressLint
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    @JvmStatic
    @SuppressLint("SimpleDateFormat")
    fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat("h:mm a").format(Date(timestamp))
    }

    @JvmStatic
    @SuppressLint("SimpleDateFormat")
    fun getLastMessageTime(timeGot: Long): String? {
        var time = timeGot
        if (time < 1000000000000L) // if timestamp given in seconds, convert to millis
            time *= 1000
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        if (time > now || time <= 0)
            return null
        val diff = now - time
        return when {
            diff < 24 * HOUR_MILLIS -> SimpleDateFormat("h:mm a").format(Date(time))
            diff < 48 * HOUR_MILLIS -> "YESTERDAY"
            else -> SimpleDateFormat("M/dd/yyyy").format(Date(time))
        }
    }

    @JvmStatic
    fun getLastSeenTime(timeGot: Long): String? {
        var time = timeGot
        if (time < 1000000000000L) // if timestamp given in seconds, convert to millis
            time *= 1000
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        if (time > now || time <= 0)
            return null
        val diff = now - time
        return when {
            diff < MINUTE_MILLIS ->
                "Last seen just now"
            diff < 2 * MINUTE_MILLIS ->
                "Last seen 1 minute ago"
            diff < 50 * MINUTE_MILLIS ->
                "Last seen " + diff / MINUTE_MILLIS + " minutes ago"
            diff < 90 * MINUTE_MILLIS ->
                "Last seen 1 hour ago"
            diff < 24 * HOUR_MILLIS ->
                "Last seen " + diff / HOUR_MILLIS + " hours ago"
            diff < 48 * HOUR_MILLIS ->
                "Last seen yesterday"
            else ->
                "Last seen " + diff / DAY_MILLIS + " days ago"
        }
    }

    @JvmStatic
    fun getDaysSince(date: String?): String {
        val oldDate = DateTime.parse(date,
                DateTimeFormat.forPattern("M/dd/yyyy"))
        val today = DateTime()
        val days = Days.daysBetween(oldDate, today).days
        return when {
            days <= 0 ->
                "You have just become friends"
            days == 1 ->
                "You have been friends for 1 day"
            else ->
                "You have been friends for $days days"
        }
    }
}