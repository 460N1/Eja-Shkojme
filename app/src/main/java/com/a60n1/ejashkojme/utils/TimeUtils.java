package com.a60n1.ejashkojme.utils;

import android.annotation.SuppressLint;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    @SuppressLint("SimpleDateFormat")
    public static String formatDateTime(long timestamp) {
        return new SimpleDateFormat("h:mm a").format(new Date(timestamp));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getLastMessageTime(long time) {
        if (time < 1000000000000L)
            // if timestamp given in seconds, convert to millis
            time *= 1000;

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        if (time > now || time <= 0)
            return null;

        final long diff = now - time;
        if (diff < 24 * HOUR_MILLIS)
            return new SimpleDateFormat("h:mm a").format(new Date(time));
        else if (diff < 48 * HOUR_MILLIS)
            return "YESTERDAY";
        else
            return new SimpleDateFormat("M/dd/yyyy").format(new Date(time));
    }

    public static String getLastSeenTime(long time) {
        if (time < 1000000000000L)
            // if timestamp given in seconds, convert to millis
            time *= 1000;

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        if (time > now || time <= 0)
            return null;

        final long diff = now - time;
        if (diff < MINUTE_MILLIS)
            return "Last seen just now";
        else if (diff < 2 * MINUTE_MILLIS)
            return "Last seen 1 minute ago";
        else if (diff < 50 * MINUTE_MILLIS)
            return "Last seen " + diff / MINUTE_MILLIS + " minutes ago";
        else if (diff < 90 * MINUTE_MILLIS)
            return "Last seen 1 hour ago";
        else if (diff < 24 * HOUR_MILLIS)
            return "Last seen " + diff / HOUR_MILLIS + " hours ago";
        else if (diff < 48 * HOUR_MILLIS)
            return "Last seen yesterday";
        else
            return "Last seen " + diff / DAY_MILLIS + " days ago";
    }

    public static String getDaysSince(String date) {
        DateTime oldDate = DateTime.parse(date,
                DateTimeFormat.forPattern("M/dd/yyyy"));
        DateTime today = new DateTime();
        int days = Days.daysBetween(oldDate, today).getDays();
        if (days <= 0)
            return "You have just become friends";
        else if (days == 1)
            return "You have been friends for 1 day";
        else
            return "You have been friends for " + days + " days";
    }
}
