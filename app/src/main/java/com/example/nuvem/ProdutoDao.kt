package com.example.nuvem

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdutoDao {

    // 1. Sua consulta de busca ordenada (mantida exatamente como você fez)
    @Query("SELECT * FROM produto ORDER BY DESCRICAO ASC")
    fun buscarProduto(): Flow<List<Produto>>

    // 2. Insere uma lista de produtos vinda da API (Aiven)
    // OnConflictStrategy.REPLACE atualiza o produto se o ID já existir no Room
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirProdutos(produtos: List<Produto>)

    // 3. Insere apenas um único produto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirProduto(produto: Produto)

    // 4. Atualiza os dados de um produto existente
    @Update
    suspend fun atualizarProduto(produto: Produto)

    // 5. Remove um produto do banco local
    @Delete
    suspend fun deletarProduto(produto: Produto)

    // 6. Opcional: Limpa a tabela para recarregar da API quando necessário
    @Query("DELETE FROM produto")
    suspend fun limparTabela()

    @Query("SELECT * FROM produto WHERE descricao LIKE '%' || :pesqEdit || '%'")
    fun buscarPorTitulo(pesqEdit: String): Flow<List<Produto>>
}