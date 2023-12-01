package com.example.examen.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Lugar::class], version =1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lugarDao(): LugarDao

    companion object {
        // Volatile asegura que sea actualizada la propiedad
        // atómicamente
        @Volatile
        private var BASE_DATOS: AppDatabase? = null
        fun getInstance(contexto: Context): AppDatabase {
            // synchronized previene el acceso de múltiples threads de manera simultánea
            return BASE_DATOS ?: synchronized(this) {
                Room.databaseBuilder(
                    contexto.applicationContext,
                    AppDatabase::class.java,
                    "Lugar.bd"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { BASE_DATOS = it }
            }
        }
    }
}