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

class cardapio : AppCompatActivity() {

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

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProdutos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = produtoAdapter

        val tipoBusca = intent.getStringExtra("TIPO_BUSCA")
        val valorBusca = intent.getStringExtra("VALOR_BUSCA")

        // 1. Sincroniza dados com a nuvem em segundo plano
        sincronizarProdutosComNuvem()

        // 2. Consulta no Room com base no filtro
        carregarProdutosDoRoom(tipoBusca, valorBusca)
    }

    private fun sincronizarProdutosComNuvem() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val produtosNuvem = RetrofitClient.apiService.obterProdutos()
                val produtoDao = AppDatabase.getDatabase(applicationContext).produtoDao()
                produtoDao.inserirProdutos(produtosNuvem)
            } catch (e: Exception) {
                Log.e("ROOM_SYNC", "Erro ao atualizar produtos da nuvem: ${e.message}")
            }
        }
    }

    private fun carregarProdutosDoRoom(tipoBusca: String?, valorBusca: String?) {
        val produtoDao = AppDatabase.getDatabase(this).produtoDao()

        lifecycleScope.launch {
            val flowProdutos = when (tipoBusca) {
                "TITULO" -> {
                    if (!valorBusca.isNullOrBlank()) {
                        produtoDao.buscarPorTitulo(valorBusca.trim())
                    } else {
                        produtoDao.obterTodos()
                    }
                }
                "GRUPO" -> {
                    val textoLimpo = valorBusca?.trim()

                    if (!textoLimpo.isNullOrEmpty()) {
                        // Passa a String limpa diretamente para o DAO
                        Log.d("GRUPO_PARA_COMPARAR", "Grupo para comparação: ${textoLimpo}")
                        produtoDao.buscarPorGrupo(textoLimpo)
                    } else {
                        produtoDao.obterTodos()
                    }
                }
                else -> produtoDao.obterTodos()
            }

            // Observa os dados em tempo real vindos do Room já ordenados em ordem alfabética
            flowProdutos.collect { lista ->
                produtoAdapter.submitList(lista)
                Log.d("ROOM_SUCESSO", "Produtos encontrados: ${lista.size}")
            }
        }
    }
}