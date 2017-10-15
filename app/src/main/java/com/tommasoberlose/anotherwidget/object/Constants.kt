package com.tommasoberlose.anotherwidget.`object`

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by tommaso on 05/10/17.
 */

@SuppressLint("SimpleDateFormat")
object Constants {
    val CALENDAR_REQUEST_CODE = 1
    val LOCATION_REQUEST_CODE = 2
    val CALENDAR_APP_REQUEST_CODE = 3
    val WEATHER_APP_REQUEST_CODE = 4


    val RESULT_CODE_CUSTOM_LOCATION = 45
    val RESULT_APP_NAME = "RESULT_APP_NAME"
    val RESULT_APP_PACKAGE = "RESULT_APP_PACKAGE"

    val PREF_SHOW_EVENTS = "PREF_SHOW_EVENTS"
    val PREF_SHOW_WEATHER = "PREF_SHOW_WEATHER"
    val PREF_WEATHER_ICON = "PREF_WEATHER_ICON"
    val PREF_WEATHER_TEMP = "PREF_WEATHER_TEMP"
    val PREF_WEATHER_TEMP_UNIT = "PREF_WEATHER_TEMP_UNIT"
    val PREF_CALENDAR_ALL_DAY = "PREF_CALENDAR_ALL_DAY"
    val PREF_CALENDAR_FILTER = "PREF_CALENDAR_FILTER"

    val PREF_NEXT_EVENT_ID = "PREF_NEXT_EVENT_ID"
    val PREF_NEXT_EVENT_NAME = "PREF_NEXT_EVENT_NAME"
    val PREF_NEXT_EVENT_START_DATE = "PREF_NEXT_EVENT_START_DATE"
    val PREF_NEXT_EVENT_ALL_DAY = "PREF_NEXT_EVENT_ALL_DAY"
    val PREF_NEXT_EVENT_END_DATE = "PREF_NEXT_EVENT_END_DATE"
    val PREF_NEXT_EVENT_CALENDAR_ID = "PREF_NEXT_EVENT_CALENDAR_ID"
    val PREF_CUSTOM_LOCATION_LAT = "PREF_CUSTOM_LOCATION_LAT"
    val PREF_CUSTOM_LOCATION_LON = "PREF_CUSTOM_LOCATION_LON"
    val PREF_CUSTOM_LOCATION_ADD = "PREF_CUSTOM_LOCATION_ADD"
    val PREF_HOUR_FORMAT = "PREF_HOUR_FORMAT"
    val PREF_ITA_FORMAT_DATE = "PREF_ITA_FORMAT_DATE"
    val PREF_WEATHER_REFRESH_PERIOD = "PREF_WEATHER_REFRESH_PERIOD"
    val PREF_SHOW_UNTIL = "PREF_SHOW_UNTIL"
    val PREF_CALENDAR_APP_NAME = "PREF_CALENDAR_APP_NAME"
    val PREF_CALENDAR_APP_PACKAGE = "PREF_CALENDAR_APP_PACKAGE"
    val PREF_WEATHER_APP_NAME = "PREF_WEATHER_APP_NAME"
    val PREF_WEATHER_APP_PACKAGE = "PREF_WEATHER_APP_PACKAGE"

    val itDateFormat = SimpleDateFormat("EEEE, d MMM")
    val engDateFormat = SimpleDateFormat("EEEE, MMM d")
    val goodHourFormat = SimpleDateFormat("HH:mm")
    val badHourFormat = SimpleDateFormat("hh:mm a")

    val ACTION_TIME_UPDATE = "com.tommasoberlose.anotherwidget.action.ACTION_TIME_UPDATE"
    val ACTION_CALENDAR_UPDATE = "com.tommasoberlose.anotherwidget.action.ACTION_CALENDAR_UPDATE"
    val ACTION_WEATHER_UPDATE = "com.tommasoberlose.anotherwidget.action.ACTION_WEATHER_UPDATE"
}
