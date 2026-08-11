package com.example.nuvem

import androidx.room.Entity
import androidx.room.ForeignKey
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

@Entity(
    tableName = "itempedido",
    foreignKeys = [
        ForeignKey(
            entity = Pedido::class,
            parentColumns = ["numero"],
            childColumns = ["pedido"]
        )
    ],
    primaryKeys = ["pedido", "produto"]
)
data class ItemPedido(
    val cpf: String, // Adicionado CPF para vincular ao Aiven
    val pedido: String,
    val produto: String,
    val descricao: String,
    val quantidade: Int,

    @SerializedName("precoVenda")
    val preco_venda: BigDecimal,

    @SerializedName("totalItem")
    val total_item: BigDecimal
)