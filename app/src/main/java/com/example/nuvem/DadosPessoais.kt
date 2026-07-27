package com.example.nuvem

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "dadospessoais")
data class DadosPessoais(
    @PrimaryKey val codigo: Int = 1,
    val nome: String,
    val endereco: String,
    val cpf: String,
    val bairro: String,
    val estado: String,
    val municipio: String,
    val cep: String? = null,
    val email: String? = null,
    val niver: String? = null,
    val telefone: String
)