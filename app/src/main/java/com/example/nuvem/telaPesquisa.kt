package com.example.nuvem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.jvm.java

class telaPesquisa : AppCompatActivity() {

    private lateinit var spinnerGrupos: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tela_pesquisa)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spinnerGrupos = findViewById(R.id.spinnerGrupos)

        // Instância do banco local (Room)
        val grupoDao = AppDatabase.getDatabase(this).GrupoDao()

        // 1. Observa o Flow do Room em tempo real e atualiza o Spinner
        lifecycleScope.launch {
            grupoDao.buscarGrupo().collect { listaGrupos ->
                Log.d("TESTE_ROOM", "Quantidade de grupos no Room: ${listaGrupos.size}")

                val listaExibicao = if (listaGrupos.isEmpty()) {
                    listOf(Grupo(codigo = 0, descricao = "Carregando grupos..."))
                } else {
                    listaGrupos
                }

                val adapter = ArrayAdapter(
                    this@telaPesquisa,
                    android.R.layout.simple_spinner_item,
                    listaExibicao
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerGrupos.adapter = adapter
            }
        }

        // 2. Busca os dados no servidor (Aiven via Retrofit) e salva no Room
        lifecycleScope.launch {
            try {
                // Busca da API (certifique-se de ter o método buscarGrupos() ou similar no ApiService)
                val gruposDaNuvem = RetrofitClient.apiService.obterGrupos()

                // Insere no banco local Room (isso fará o Flow do passo 1 disparar automaticamente)
                grupoDao.inserirGrupo(gruposDaNuvem)
            } catch (e: Exception) {
                Log.e("TESTE_ROOM", "Erro ao carregar dados da nuvem: ${e.message}")
            }
        }
    }

    // Função movida para fora do onCreate para funcionar com o android:onClick no XML
    fun telaInicial(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun pesquisar(view: View) {
        val intent = Intent(this, cardapio::class.java)
        startActivity(intent)
    }
}