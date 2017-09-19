package com.layer.ui.util;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatterImpl implements DateFormatter {
    private final int TIME_HOURS_24 = 24 * 60 * 60 * 1000;
    private final SimpleDateFormat DAY_OF_WEEK;
    private final DateFormat TIME_FORMAT;
    private Context mContext;

    public DateFormatterImpl(Context context) {
        mContext = context;
        DAY_OF_WEEK = new SimpleDateFormat("EEE, LLL dd", Locale.getDefault());
        TIME_FORMAT = android.text.format.DateFormat.getTimeFormat(context);
    }

    /**
     * Returns Today, Yesterday, the day of the week within one week, or a date if greater.
     *
     * @param date
     * @return
     */

    @Override
    public String formatTimeDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long todayMidnight = cal.getTimeInMillis();
        long yesterdayMidnight = todayMidnight - TIME_HOURS_24;
        long weekAgoMidnight = todayMidnight - TIME_HOURS_24 * 7;

        String timeBarDayText;
        if (date.getTime() > todayMidnight) {
            timeBarDayText = mContext.getString(com.layer.ui.R.string.layer_ui_time_today);
        } else if (date.getTime() > yesterdayMidnight) {
            timeBarDayText = mContext.getString(com.layer.ui.R.string.layer_ui_time_yesterday);
        } else if (date.getTime() > weekAgoMidnight) {
            cal.setTime(date);
            timeBarDayText = mContext.getResources().getStringArray(com.layer.ui.R.array.layer_ui_time_days_of_week)[cal.get(Calendar.DAY_OF_WEEK) - 1];
        } else {
            timeBarDayText = DAY_OF_WEEK.format(date);
        }
        return timeBarDayText;
    }

    @Override
    public String formatTime(Date date) {
        return TIME_FORMAT.format(date);
    }

    @Override
    public String formatTime(Date date, DateFormat timeFormat, DateFormat dateFormat) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long todayMidnight = cal.getTimeInMillis();
        long yesterMidnight = todayMidnight - TIME_HOURS_24;
        long weekAgoMidnight = todayMidnight - TIME_HOURS_24 * 7;

        String timeText;
        if (date.getTime() > todayMidnight) {
            timeText = timeFormat.format(date.getTime());
        } else if (date.getTime() > yesterMidnight) {
            timeText = mContext.getString(com.layer.ui.R.string.layer_ui_time_yesterday);
        } else if (date.getTime() > weekAgoMidnight) {
            cal.setTime(date);
            timeText = mContext.getResources().getStringArray(com.layer.ui.R.array.layer_ui_time_days_of_week)[cal.get(Calendar.DAY_OF_WEEK) - 1];
        } else {
            timeText = dateFormat.format(date);
        }
        return timeText;
    }
}
