package com.example.examen.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Lugar(
    @PrimaryKey(autoGenerate = true) val uid: Int=0,
    var lugar:String,
    var imagen:String,
    var latlong:String,
    var orden:String,
    var costoa:String,
    var costot:String,
    var comentarios:String
)