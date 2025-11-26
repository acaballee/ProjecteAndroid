package com.example.projecteandroid.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Retorna un Flow amb totes les tasques d'un usuari específic
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY dueDate ASC")
    fun getTasksForUser(userId: Int): Flow<List<Task>>

    // Insereix una nova tasca a la base de dades
    @Insert
    suspend fun insertTask(task: Task)

    // Actualitza una tasca existent
    @Update
    suspend fun updateTask(task: Task)

    // Esborra una tasca
    @Delete
    suspend fun deleteTask(task: Task)
}
