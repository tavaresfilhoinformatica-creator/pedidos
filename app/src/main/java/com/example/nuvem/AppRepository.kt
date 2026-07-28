package com.example.nuvem

import android.util.Log
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val apiService: ApiService,
    private val grupoDao: GrupoDao,
    private val produtoDao: ProdutoDao,
    private val pagamentoDao: PagamentoDao,
    private val pedidoDao: PedidoDao
) {

    // --- GRUPOS ---
    suspend fun sincronizarGrupos(): Result<Unit> {
        return try {
            val gruposWeb = apiService.obterGrupos()
            grupoDao.inserirGrupo(gruposWeb) // Chama a função de inserção
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // --- PRODUTOS ---


    fun obterProdutosLocais(): Flow<List<Produto>> {
        return produtoDao.obterTodos()// ajuste para a sua função do ProdutoDao
    }


    // --- PAGAMENTOS ---
    suspend fun sincronizarPagamentos(): Result<Unit> {
        return try {
            val pagamentosWeb = apiService.obterFormasPagamento()
            pagamentoDao.inserirPagamentos(pagamentosWeb)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // --- PEDIDO (ENVIO PARA AIVEN) ---
    suspend fun enviarPedidoParaAiven(pedido: Pedido): Result<Boolean> {
        return try {
            val resposta = apiService.enviarPedido(pedido)
            if (resposta.isSuccessful) {
                // Se enviou com sucesso pra nuvem, marca ou salva localmente se quiser
                Result.success(true)
            } else {
                Result.failure(Exception("Erro no servidor: ${resposta.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}