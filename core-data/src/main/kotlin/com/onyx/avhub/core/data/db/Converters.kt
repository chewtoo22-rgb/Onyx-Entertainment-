package com.onyx.avhub.core.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun intListToString(value: List<Int>): String = value.joinToString(",")

    @TypeConverter
    fun stringToIntList(value: String): List<Int> =
        if (value.isEmpty()) emptyList() else value.split(",").map { it.toInt() }

    @TypeConverter
    fun floatListToString(value: List<Float>): String = value.joinToString(",")

    @TypeConverter
    fun stringToFloatList(value: String): List<Float> =
        if (value.isEmpty()) emptyList() else value.split(",").map { it.toFloat() }
}
