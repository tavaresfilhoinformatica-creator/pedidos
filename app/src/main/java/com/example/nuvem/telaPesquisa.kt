package com.example.nuvem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
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
        val radioGroup = findViewById<RadioGroup>(R.id.radioOpcoes)
        val opcaoTitulo = findViewById<RadioButton>(R.id.tituloOpcao)
        val btnPesquisar = findViewById<Button>(R.id.btnPesquisar) // Substitua pelo ID do seu botão
        val textoBusca = findViewById<EditText>(R.id.pesqEdit)

        // 2. Define o foco inicial (como você já fez)
        opcaoTitulo.requestFocus()

        // 3. Ação do botão Pesquisar
        btnPesquisar.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            val intent = Intent(this, cardapio::class.java)

            when (selectedId) {
                R.id.tituloOpcao -> {
                    val texto = textoBusca.text.toString()
                    intent.putExtra("TIPO_BUSCA", "TITULO")
                    intent.putExtra("VALOR_BUSCA", texto)
                }
                R.id.grupoOpcao -> {
                    // Pega o objeto Grupo selecionado no Spinner
                    val grupoSelecionado = spinnerGrupos.selectedItem as? Grupo
                    intent.putExtra("TIPO_BUSCA", "GRUPO")
                    // Passa o código ou a descrição do grupo
                    intent.putExtra("VALOR_BUSCA", grupoSelecionado?.codigo.toString())
                }
            }

            startActivity(intent)
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