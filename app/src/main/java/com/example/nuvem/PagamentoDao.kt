package com.example.nuvem

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PagamentoDao {
    @Query("SELECT * FROM pagamento ORDER BY DESCRICAO ASC")
     fun buscarPagamento(): Flow<List<Pagamento>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPagamentos(pagamento: List<Pagamento>)
}