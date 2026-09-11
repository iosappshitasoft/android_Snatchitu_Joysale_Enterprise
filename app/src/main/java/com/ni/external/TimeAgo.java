package com.ni.external;

import android.content.Context;
import android.text.format.DateUtils;

import com.ni.helper.LocaleManager;

import java.util.Date;
import java.util.Locale;

/**
 * Created by hitasoft on 9/1/17.
 */

public class TimeAgo {
    public static final long AVERAGE_MONTH_IN_MILLIS = DateUtils.DAY_IN_MILLIS * 30;

    private static String TAG = TimeAgo.class.getSimpleName();
    protected Context context;
    Locale locale;

    public TimeAgo(Context context) {
        this.context = context;
        locale = LocaleManager.getLocale(context.getResources());
    }

/*
    public String timeAgo(long millis) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));

        long diff = new Date().getTime() - millis;
        Resources r = context.getResources();
        double seconds = Math.abs(diff) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double weeks = days / 7;
        double years = days / 365;
        String words;
        if (seconds < 60) {
            words = r.getString(R.string.time_ago_seconds, nf.format(Math.round(seconds)));
        } else if (seconds < 120) {
            words = r.getString(R.string.time_ago_minute);
        } else if (minutes < 60) {
            words = r.getString(R.string.time_ago_minutes, nf.format(Math.round(minutes)));
        } else if (minutes < 120) {
            words = r.getString(R.string.time_ago_hour);
        } else if (hours < 24) {
            String temp = nf.format(Math.round(hours));
            words = r.getString(R.string.time_ago_hours, temp);
        } else if (hours < 42) {
            words = r.getString(R.string.time_ago_day);
        } else if (days < 30) {
            words = r.getString(R.string.time_ago_days, nf.format(Math.round(days)));
        } else if (days < 60) {
            words = r.getString(R.string.time_ago_month);
        } else if (days < 365) {
            words = r.getString(R.string.time_ago_months, nf.format(Math.round(days / 30)));
        } else if (years < 2) {
            words = r.getString(R.string.time_ago_year);
        } else {
            words = r.getString(R.string.time_ago_years, nf.format(years));
        }
        return words.trim();
    }
*/

    public String timeAgo(long time) {
        final long now = new Date().getTime();
        final long delta = now - time;
        long resolution;
        if (delta <= DateUtils.MINUTE_IN_MILLIS) {
            resolution = DateUtils.SECOND_IN_MILLIS;
        } else if (delta <= DateUtils.HOUR_IN_MILLIS) {
            resolution = DateUtils.MINUTE_IN_MILLIS;
        } else if (delta <= DateUtils.DAY_IN_MILLIS) {
            resolution = DateUtils.HOUR_IN_MILLIS;
        } else if (delta <= DateUtils.WEEK_IN_MILLIS) {
            resolution = DateUtils.DAY_IN_MILLIS;
        } else if (delta <= AVERAGE_MONTH_IN_MILLIS) {
            if (delta / DateUtils.WEEK_IN_MILLIS == 1)
                return Integer.toString((int) (delta / DateUtils.WEEK_IN_MILLIS)) + " week ago";
            else
                return Integer.toString((int) (delta / DateUtils.WEEK_IN_MILLIS)) + " weeks ago";
        } else if (delta <= DateUtils.YEAR_IN_MILLIS) {
            if (delta / AVERAGE_MONTH_IN_MILLIS == 1)
                return Integer.toString((int) (delta / AVERAGE_MONTH_IN_MILLIS)) + " month ago";
            else
                return Integer.toString((int) (delta / AVERAGE_MONTH_IN_MILLIS)) + " months ago";
        } else {
            if (delta / DateUtils.YEAR_IN_MILLIS == 1)
                return Integer.toString((int) (delta / DateUtils.YEAR_IN_MILLIS)) + " year ago";
            else
                return Integer.toString((int) (delta / DateUtils.YEAR_IN_MILLIS)) + " years ago";
        }
        return DateUtils.getRelativeTimeSpanString(time, now, resolution).toString();
    }

}
