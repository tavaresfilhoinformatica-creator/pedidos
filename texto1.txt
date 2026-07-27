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
        // ⚠️ Troque 'recyclerViewProdutos' pelo ID exato que estiver no seu XML!
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProdutos)

        // 3. Associa o LayoutManager e o Adapter ao RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = produtoAdapter

        // 4. Busca os dados da API na nuvem
        carregarProdutos()
    }

    private fun carregarProdutos() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Faz a chamada ao Node/Aiven usando o Retrofit
                val listaProdutos = RetrofitClient.apiService.obterProdutos()

                withContext(Dispatchers.Main) {
                    if (listaProdutos.isNotEmpty()) {
                        // Envia os dados para o ListAdapter preencher o CardView!
                        produtoAdapter.submitList(listaProdutos)
                        Log.d("API_SUCESSO", "Produtos carregados: ${listaProdutos.size}")
                    } else {
                        Log.w("API_AVISO", "A lista retornou vazia!")
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERRO", "Erro ao buscar produtos: ${e.message}", e)
            }
        }
    }
}