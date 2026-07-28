package com.example.nuvem

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "grupo")
data class Grupo(
    @PrimaryKey val codigo: String,
    val descricao: String
) {
    // O Spinner utiliza o toString() para saber qual texto mostrar
    override fun toString(): String {
        return descricao
    }
}