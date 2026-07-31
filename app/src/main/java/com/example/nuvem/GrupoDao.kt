package com.example.nuvem

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GrupoDao {
    @Query("SELECT * FROM grupo ORDER BY DESCRICAO ASC")
    fun buscarGrupo(): Flow<List<Grupo>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirGrupo(grupo: List<Grupo>)

    @Query("DELETE FROM grupo")
    suspend fun deletarTodos()

    @Transaction
    suspend fun atualizarTabelaRemota(gruposRemotos: List<Grupo>) {
        deletarTodos()
        inserirGrupo(gruposRemotos)
    }
}