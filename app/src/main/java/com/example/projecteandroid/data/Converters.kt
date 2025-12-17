package com.example.projecteandroid.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(status: String): TaskStatus = TaskStatus.valueOf(status)

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority = TaskPriority.valueOf(priority)
}