package com.example.nuvem

import androidx.room.Entity
import androidx.room.ForeignKey
import java.math.BigDecimal

@Entity(
    tableName = "itempedido",
    // Declarando a relação com a tabela pedido
    foreignKeys = [
        ForeignKey(
            entity = Pedido::class,        // A classe da tabela Pai
            parentColumns = ["numero"],    // O campo chave na tabela Pedido
            childColumns = ["pedido"]      // O campo que faz o vínculo aqui na tabela ItemPedido
        )
    ], // <-- Essa vírgula aqui é obrigatória para separar os parâmetros
    primaryKeys = ["pedido", "produto"]
)
data class ItemPedido(
    val pedido: String,
    val produto: String,
    val quantidade: Int,
    val preco_venda: BigDecimal,
    val total_item: BigDecimal
)