/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.vxquery.datamodel.util;

import java.io.DataOutput;
import java.io.IOException;

import org.apache.vxquery.datamodel.api.ITimezone;
import org.apache.vxquery.datamodel.values.ValueTag;

public class DateTime {
    public static final long[] DAYS_OF_MONTH_ORDI = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    public static final long[] DAYS_OF_MONTH_LEAP = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    public static final long CHRONON_OF_SECOND = 1000;
    public static final long CHRONON_OF_MINUTE = 60 * CHRONON_OF_SECOND;
    public static final long CHRONON_OF_HOUR = 60 * CHRONON_OF_MINUTE;
    public static final long CHRONON_OF_DAY = 24 * CHRONON_OF_HOUR;

    /**
     * Minimum feasible value of each field
     */
    public static final int[] FIELD_MINS = { Short.MIN_VALUE, // year
            1, // month
            1, // day
            0, // hour
            0, // minute
            0 // millisecond
    };

    public static final int[] FIELD_MAXS = { Short.MAX_VALUE, // year
            12, // month
            31, // day
            23, // hour
            59, // minute
            59999 // millisecond
    };

    public static final int TIMEZONE_HOUR_MIN = -14, TIMEZONE_HOUR_MAX = 14, TIMEZONE_MINUTE_MIN = -59,
            TIMEZONE_MINUTE_MAX = 59;
    // Used to store the timezone value when one does not exist.
    public static final byte TIMEZONE_HOUR_NULL = 127, TIMEZONE_MINUTE_NULL = 127;

    public static final int YEAR_FIELD_INDEX = 0, MONTH_FIELD_INDEX = 1, DAY_FIELD_INDEX = 2, HOUR_FIELD_INDEX = 3,
            MINUTE_FIELD_INDEX = 4, MILLISECOND_FIELD_INDEX = 5;

    // Default date for time to datetime conversions.
    public static final int TIME_DEFAULT_YEAR = 1972, TIME_DEFAULT_MONTH = 12, TIME_DEFAULT_DAY = 31;

    /**
     * Check whether a given year is a leap year.
     *
     * @param year
     *            A long for year.
     * @return Boolean for leap year.
     */
    public static boolean isLeapYear(long year) {
        return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
    }

    /**
     * Check whether a given year is a leap year.
     *
     * @param year
     *            year
     * @param month
     *            month
     * @param day
     *            day
     * @param hour
     *            hour
     * @param minute
     *            minute
     * @param millisecond
     *            millisecond
     * @param timezoneHour
     *            timezoneHour
     * @param timezoneMinute
     *            timezoneMinute
     * @return Boolean for valid date.
     */
    public static boolean valid(long year, long month, long day, long hour, long minute, long millisecond,
            long timezoneHour, long timezoneMinute) {
        if (year > FIELD_MAXS[DateTime.YEAR_FIELD_INDEX] || year < FIELD_MINS[DateTime.YEAR_FIELD_INDEX]) {
            return false;
        }
        long[] monthCheck = DAYS_OF_MONTH_ORDI;
        if (isLeapYear(year)) {
            monthCheck = DAYS_OF_MONTH_LEAP;
        }
        if (month > FIELD_MAXS[DateTime.MONTH_FIELD_INDEX] || month < FIELD_MINS[DateTime.MONTH_FIELD_INDEX]) {
            return false;
        }
        if (day > monthCheck[(int) (month - 1)] || day < FIELD_MINS[DateTime.DAY_FIELD_INDEX]) {
            return false;
        }
        if (hour > FIELD_MAXS[DateTime.HOUR_FIELD_INDEX] || hour < FIELD_MINS[DateTime.HOUR_FIELD_INDEX]) {
            return false;
        }
        if (minute > FIELD_MAXS[DateTime.MINUTE_FIELD_INDEX] || minute < FIELD_MINS[DateTime.MINUTE_FIELD_INDEX]) {
            return false;
        }
        if (millisecond > FIELD_MAXS[DateTime.MILLISECOND_FIELD_INDEX]
                || millisecond < FIELD_MINS[DateTime.MILLISECOND_FIELD_INDEX]) {
            return false;
        }
        if ((timezoneHour > TIMEZONE_HOUR_MAX || timezoneHour < TIMEZONE_HOUR_MIN)
                && (timezoneHour != TIMEZONE_HOUR_NULL)) {
            return false;
        }
        if ((timezoneMinute > TIMEZONE_MINUTE_MAX || timezoneMinute < TIMEZONE_MINUTE_MIN)
                && (timezoneMinute != TIMEZONE_MINUTE_NULL)) {
            return false;
        }
        return true;
    }

    /**
     * Return a normalized time.
     *
     * @param yearMonth
     *            Months
     * @param dayTime
     *            Time
     * @param timezoneHour
     *            timezoneHour
     * @param timezoneMinute
     *            timezoneMinute
     * @param dOut
     *            Data out
     * @throws IOException
     *             Could not write result.
     */
    public static void normalizeDateTime(long yearMonth, long dayTime, long timezoneHour, long timezoneMinute,
            DataOutput dOut) throws IOException {
        long[] monthDayLimits;

        long day = dayTime / CHRONON_OF_DAY;
        dayTime %= CHRONON_OF_DAY;
        long hour = dayTime / CHRONON_OF_HOUR;
        dayTime %= CHRONON_OF_HOUR;
        long minute = dayTime / CHRONON_OF_MINUTE;
        dayTime %= CHRONON_OF_MINUTE;
        long millisecond = dayTime;
        long month = (yearMonth % 12 == 0 ? 12 : yearMonth % 12);
        long year = (yearMonth / 12) + (month == 12 ? -1 : 0);

        monthDayLimits = (isLeapYear(year) ? DateTime.DAYS_OF_MONTH_LEAP : DateTime.DAYS_OF_MONTH_ORDI);
        while (day < DateTime.FIELD_MINS[DateTime.DAY_FIELD_INDEX] || day > monthDayLimits[(int) month - 1]
                || month < DateTime.FIELD_MINS[DateTime.MONTH_FIELD_INDEX]
                || month > DateTime.FIELD_MAXS[DateTime.MONTH_FIELD_INDEX]
                || hour < DateTime.FIELD_MINS[DateTime.HOUR_FIELD_INDEX]
                || hour > DateTime.FIELD_MAXS[DateTime.HOUR_FIELD_INDEX]
                || minute < DateTime.FIELD_MINS[DateTime.MINUTE_FIELD_INDEX]
                || minute > DateTime.FIELD_MAXS[DateTime.MINUTE_FIELD_INDEX]
                || millisecond < DateTime.FIELD_MINS[DateTime.MILLISECOND_FIELD_INDEX]
                || millisecond > DateTime.FIELD_MAXS[DateTime.MILLISECOND_FIELD_INDEX]) {
            if (millisecond < DateTime.FIELD_MINS[DateTime.MILLISECOND_FIELD_INDEX]) {
                // Too small
                --minute;
                millisecond += 60000;
            } else if (millisecond > DateTime.FIELD_MAXS[DateTime.MILLISECOND_FIELD_INDEX]) {
                // Too large
                ++minute;
                millisecond -= 60000;
            }
            if (minute < DateTime.FIELD_MINS[DateTime.MINUTE_FIELD_INDEX]) {
                // Too small
                --hour;
                minute += 60;
            } else if (minute > DateTime.FIELD_MAXS[DateTime.MINUTE_FIELD_INDEX]) {
                // Too large
                ++hour;
                minute -= 60;
            }
            if (hour < DateTime.FIELD_MINS[DateTime.HOUR_FIELD_INDEX]) {
                // Too small
                --day;
                hour += 24;
            } else if (hour > DateTime.FIELD_MAXS[DateTime.HOUR_FIELD_INDEX]) {
                // Too large
                ++day;
                hour -= 24;
            }
            if (day < DateTime.FIELD_MINS[DateTime.DAY_FIELD_INDEX]) {
                // Too small
                --month;
                if (month < DateTime.FIELD_MINS[DateTime.MONTH_FIELD_INDEX]) {
                    // Too small
                    month = DateTime.FIELD_MAXS[DateTime.MONTH_FIELD_INDEX];
                    --year;
                }
                day += monthDayLimits[(int) month - 1];
            } else if (day > monthDayLimits[(int) month - 1]) {
                // Too large
                day -= monthDayLimits[(int) month - 1];
                ++month;
            }
            if (month < DateTime.FIELD_MINS[DateTime.MONTH_FIELD_INDEX]) {
                // Too small
                month = DateTime.FIELD_MAXS[DateTime.MONTH_FIELD_INDEX];
                --year;
            } else if (month > DateTime.FIELD_MAXS[DateTime.MONTH_FIELD_INDEX]) {
                // Too large
                month = DateTime.FIELD_MINS[DateTime.MONTH_FIELD_INDEX];
                ++year;
            }
            monthDayLimits = (isLeapYear(year) ? DateTime.DAYS_OF_MONTH_LEAP : DateTime.DAYS_OF_MONTH_ORDI);
        }
        dOut.write(ValueTag.XS_DATETIME_TAG);
        dOut.writeShort((short) year);
        dOut.writeByte((byte) month);
        dOut.writeByte((byte) day);
        dOut.writeByte((byte) hour);
        dOut.writeByte((byte) minute);
        dOut.writeInt((int) millisecond);
        dOut.writeByte((byte) timezoneHour);
        dOut.writeByte((byte) timezoneMinute);
    }

    public static void getUtcTimezoneDateTime(ITimezone timezonep, ITimezone defaultTimezonep, DataOutput dOut)
            throws IOException {
        long timezoneHour;
        long timezoneMinute;
        // Consider time zones.
        if (timezonep.getTimezoneHour() == DateTime.TIMEZONE_HOUR_NULL
                || timezonep.getTimezoneMinute() == DateTime.TIMEZONE_MINUTE_NULL) {
            timezoneHour = defaultTimezonep.getTimezoneHour();
            timezoneMinute = defaultTimezonep.getTimezoneMinute();
        } else {
            timezoneHour = timezonep.getTimezoneHour();
            timezoneMinute = timezonep.getTimezoneMinute();
        }
        long dayTime = timezonep.getDayTime()
                - (timezoneHour * DateTime.CHRONON_OF_HOUR + timezoneMinute * DateTime.CHRONON_OF_MINUTE);
        DateTime.normalizeDateTime(timezonep.getYearMonth(), dayTime, 0, 0, dOut);
    }

    public static void adjustDateTimeToTimezone(ITimezone timezonep, long timezone, DataOutput dOut)
            throws IOException {
        long timezoneHour = timezone / 60;
        long timezoneMinute = timezone % 60;
        long dayTime = timezonep.getDayTime();
        if (timezonep.getTimezoneHour() == DateTime.TIMEZONE_HOUR_NULL
                || timezonep.getTimezoneMinute() == DateTime.TIMEZONE_MINUTE_NULL) {
            // No change.
        } else {
            dayTime -= (timezonep.getTimezoneHour() * DateTime.CHRONON_OF_HOUR
                    + timezonep.getTimezoneMinute() * DateTime.CHRONON_OF_MINUTE);
            dayTime += (timezoneHour * DateTime.CHRONON_OF_HOUR + timezoneMinute * DateTime.CHRONON_OF_MINUTE);
        }
        DateTime.normalizeDateTime(timezonep.getYearMonth(), dayTime, timezoneHour, timezoneMinute, dOut);
    }

}
