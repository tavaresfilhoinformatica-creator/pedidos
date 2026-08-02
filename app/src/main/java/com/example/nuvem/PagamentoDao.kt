package com.example.nuvem

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PagamentoDao {
    // 👈 'suspend' em vez de Flow e 'SELECT *' para buscar o objeto Pagamento completo
    @Query("SELECT * FROM pagamento ORDER BY descricao ASC")
    suspend fun obterTodas(): List<Pagamento>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPagamentos(pagamento: List<Pagamento>)
}