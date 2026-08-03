package com.example.nuvem

import androidx.room.Entity
import androidx.room.ForeignKey
import java.math.BigDecimal

@Entity(
    tableName = "itempedido",
    // Declarando a relação com a tabela Pedido (agora com chave composta)
    foreignKeys = [
        ForeignKey(
            entity = Pedido::class,
            parentColumns = ["cpf", "numero"],  // As 2 colunas da chave primária da tabela Pedido
            childColumns = ["cpf", "pedido"],   // As 2 colunas de vínculo aqui no ItemPedido
            onDelete = ForeignKey.CASCADE       // Opcional: exclui os itens se o pedido for excluído
        )
    ],
    primaryKeys = ["cpf", "pedido", "produto"]
)
data class ItemPedido(
    val cpf: String,
    val pedido: Int,
    val produto: String,
    val descricao: String? = null,
    val quantidade: Int,
    val preco_venda: BigDecimal,
    val total_item: BigDecimal
)