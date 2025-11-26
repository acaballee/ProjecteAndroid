package com.example.projecteandroid.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // Si s'esborra un usuari, s'esborren les seves tasques
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int, // Clau forana per enllaçar amb l'usuari
    val title: String,
    val subject: String,
    val dueDate: String,
    val isCompleted: Boolean = false
)