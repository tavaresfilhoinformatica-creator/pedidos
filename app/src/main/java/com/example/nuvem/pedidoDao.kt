package com.example.nuvem

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface pedidoDao {
    @Query("SELECT MAX(numero) FROM pedido WHERE cpf = :cpf")
    suspend fun obterUltimoNumeroPedidoPorCpf(cpf: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPedido(pedido: Pedido)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirItensPedido(itens: List<ItemPedido>)
}