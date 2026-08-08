package com.example.nuvem

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "pedido")
data class Pedido(
    @PrimaryKey(autoGenerate = true)
    val numero: Int = 0,

    val cpf: String,

    val data_pedido: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time),

    @SerializedName("totalPedido")
    val total_pedido: BigDecimal,

    @SerializedName("enderecoEntrega")
    val endereco_entrega: String,

    @SerializedName("bairroEntrega")
    val bairro_entrega: String,

    @SerializedName("cep_Entrega")
    val cep_entrega: String,

    @SerializedName("telefoneEntrega")
    val telefone_entrega: String,

    @SerializedName("formaPagamento")
    val forma_pagamento: String = "01", // Adicionado padrão caso necessário

    val obs: String = ""
)