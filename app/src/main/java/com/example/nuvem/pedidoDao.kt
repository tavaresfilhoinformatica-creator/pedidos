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

    // Busca todos os pedidos ordenando do mais novo para o mais antigo
    @Query("SELECT * FROM pedido ORDER BY numero DESC")
    suspend fun obterTodosPedidos(): List<Pedido>

    // Busca os itens de um pedido específico
    @Query("SELECT * FROM itempedido WHERE pedido = :numeroPedido")
    suspend fun obterItensDoPedido(numeroPedido: Int): List<ItemPedido>
}