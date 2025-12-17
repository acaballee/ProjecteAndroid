package com.example.projecteandroid.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, Task::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao // Afegim el nou DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .fallbackToDestructiveMigration() // Per a la demo, si canviem versió, es destrueix i es reconstrueix la BD
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Classe interna per poblar la BD inicialment
        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.userDao())
                    }
                }
            }

            suspend fun populateDatabase(userDao: UserDao) {
                // Esborrem tot per començar net (opcional)
                // userDao.deleteAll()

                // CREEM L'USUARI PER DEFECTE
                // Assegura't que el teu User té un constructor compatible
                val adminUser = User(id = 1, username = "admin", password = "admin")
                userDao.insertUser(adminUser)
            }
        }
    }
}