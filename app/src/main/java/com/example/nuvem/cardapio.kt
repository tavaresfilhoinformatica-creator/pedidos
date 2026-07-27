package com.example.nuvem

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class cardapio : AppCompatActivity() {

    // 1. Instancia o seu ProdutoAdapter
    private val produtoAdapter = ProdutoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cardapio)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Busca o RecyclerView do seu activity_cardapio.xml
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProdutos)

        // 3. Associa o LayoutManager e o Adapter ao RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = produtoAdapter

        // 4. Recebe os parâmetros de busca enviados pela telaPesquisa
        val tipoBusca = intent.getStringExtra("TIPO_BUSCA")
        val valorBusca = intent.getStringExtra("VALOR_BUSCA")

        // 5. Busca os dados na nuvem aplicando os filtros
        carregarProdutos(tipoBusca, valorBusca)
    }

    private fun carregarProdutos(tipoBusca: String?, valorBusca: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Faz a chamada ao Node/Aiven usando o Retrofit
                val listaProdutos = RetrofitClient.apiService.obterProdutos()

                // Aplica a filtragem baseada no tipo e valor recebidos
                val listaFiltrada = when (tipoBusca) {
                    "TITULO" -> {
                        if (!valorBusca.isNullOrBlank()) {
                            // Filtra os produtos cuja descrição contenha o texto buscado (ignorando maiúsculas/minúsculas)
                            listaProdutos.filter { produto ->
                                produto.descricao.contains(valorBusca, ignoreCase = true)
                            }
                        } else {
                            listaProdutos
                        }
                    }
                    "GRUPO" -> {
                        if (!valorBusca.isNullOrBlank()) {
                            listaProdutos.filter { produto ->
                                Log.d("TESTE_FILTRO", "ID Produto: ${produto.grupoId} | ID Buscado: $valorBusca")

                                // Converte ambos para Int para ignorar os zeros à esquerda ("003" vira 3)
                                val idProdutoInt = produto.grupoId.toString().toIntOrNull()
                                val idBuscadoInt = valorBusca.toIntOrNull()

                                idProdutoInt != null && idBuscadoInt != null && idProdutoInt == idBuscadoInt
                            }
                        } else {
                            listaProdutos
                        }
                    }
                    else -> listaProdutos
                }


                withContext(Dispatchers.Main) {
                    if (listaFiltrada.isNotEmpty()) {
                        // Envia os dados filtrados para o ListAdapter preencher o CardView!
                        produtoAdapter.submitList(listaFiltrada)
                        Log.d("API_SUCESSO", "Produtos exibidos: ${listaFiltrada.size}")
                    } else {
                        produtoAdapter.submitList(emptyList())
                        Log.w("API_AVISO", "Nenhum produto encontrado para esse filtro!")
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERRO", "Erro ao buscar produtos: ${e.message}", e)
            }
        }
    }
}