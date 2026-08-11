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

    // 1. Busca todos os produtos ordenados por descrição
    @Query("SELECT * FROM produto ORDER BY descricao ASC")
    fun obterTodos(): Flow<List<Produto>>

    // 2. Busca por título (LIKE %texto%)
    @Query("SELECT * FROM produto WHERE descricao LIKE '%' || :pesqEdit || '%' ORDER BY descricao ASC")
    fun buscarPorTitulo(pesqEdit: String): Flow<List<Produto>>

    // 3. Busca por Grupo ID exato (Ajuste para String se o seu grupo_id na entidade for String)
    @Query("SELECT * FROM produto WHERE grupo_id = :grupoId ORDER BY descricao ASC")
    fun buscarPorGrupo(grupoId: String): Flow<List<Produto>>

    // 4. Inserções
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirProdutos(produtos: List<Produto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirProduto(produto: Produto)

    // 5. Atualização e Deleção
    @Update
    suspend fun atualizarProduto(produto: Produto)

    @Delete
    suspend fun deletarProduto(produto: Produto)

    @Query("DELETE FROM produto")
    suspend fun limparTabela()
}