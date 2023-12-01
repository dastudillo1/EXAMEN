package com.example.examen.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LugarDao {

    @Query("SELECT * FROM lugar ORDER BY orden")
    fun findAll(): List<Lugar>

    @Query("SELECT COUNT(*) FROM lugar")
    fun contar(): Int

    @Insert
    fun insertar(lugar: Lugar):Long

    @Update
    fun actualizar(lugar: Lugar)

    @Delete
    fun eliminar(lugar: Lugar)

    @Query("SELECT * FROM lugar")
    fun getAll():List<Lugar>

    @Insert
    fun insertAll(vararg lugar:Lugar)
    @Update
    fun update(vararg lugar:Lugar)

    @Insert
    fun insert(contacto:Lugar):Long

    @Query("DELETE FROM lugar")
    fun getDeleteAll()

}