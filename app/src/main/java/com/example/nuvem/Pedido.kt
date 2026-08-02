package com.example.nuvem

import androidx.room.Entity
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Entity(
    tableName = "pedido",
    primaryKeys = ["cpf", "numero"] // 👈 Define a chave primária composta (CPF + Número)
)
data class Pedido(
    val cpf: String,
    val numero: Int,

    // CORRIGIDO: Usa o Calendar para pegar a data corrente no formato AAAA-MM-DD
    val data_pedido: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time),

    val total_pedido: BigDecimal,
    val endereco_entrega: String,
    val bairro_entrega: String,
    val cep_entrega: String,
    val telefone_entrega: String,
    val obs: String
)