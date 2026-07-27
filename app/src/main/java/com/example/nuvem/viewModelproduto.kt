package com.example.nuvem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProdutoViewModel(private val produtoDao: ProdutoDao) : ViewModel() {

    // Mantém a lista de produtos atualizada em tempo real via Room
    val listaProdutos: StateFlow<List<Produto>> = produtoDao.buscarProduto()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Toda vez que a ViewModel for criada, ela dispara a busca na API
        sincronizarProdutosComApi()
    }

    fun sincronizarProdutosComApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Busca os produtos da API Node/Aiven via Retrofit
                val produtosDaApi = RetrofitClient.apiService.obterProdutos()

                // 2. Salva no banco local (Room)
                // Certifique-se de que no seu ProdutoDao existe a função de inserir
                produtoDao.inserirProdutos(produtosDaApi)

            } catch (e: Exception) {
                e.printStackTrace()
                // Em caso de erro de rede (API desligada, IP incorreto, etc.)
                // o Room continuará exibindo os últimos produtos salvos offline!
            }
        }
    }
}

