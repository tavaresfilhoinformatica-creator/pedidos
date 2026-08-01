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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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

        // Configuração do Adapter do Spinner uma única vez
        val adapter = ArrayAdapter<Grupo>(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerGrupos.adapter = adapter

        // 1. Observa o Flow do Room em tempo real e atualiza a lista do Adapter existente
        lifecycleScope.launch {
            grupoDao.buscarGrupo().collect { listaGrupos ->
                Log.d("TESTE_ROOM", "QuantidadeActivity de grupos no Room: ${listaGrupos.size}")

                adapter.clear()
                if (listaGrupos.isEmpty()) {
                    adapter.add(Grupo(codigo = "000", descricao = "Carregando grupos..."))
                } else {
                    adapter.addAll(listaGrupos)
                }
                adapter.notifyDataSetChanged()
            }
        }

        // 2. Busca os dados no servidor (Aiven via Retrofit) e salva no Room
        lifecycleScope.launch {
            try {
                val gruposDaNuvem = RetrofitClient.apiService.atualizarTabelaRemota()
                grupoDao.inserirGrupo(gruposDaNuvem)
            } catch (e: Exception) {
                Log.e("TESTE_ROOM", "Erro ao carregar dados da nuvem: ${e.message}")
            }
        }

        val radioGroup = findViewById<RadioGroup>(R.id.radioOpcoes)
        val opcaoTitulo = findViewById<RadioButton>(R.id.tituloOpcao)
        val btnPesquisar = findViewById<TextView>(R.id.btnPesquisar)
        val textoBusca = findViewById<EditText>(R.id.pesqEdit)
        val opcaogrupo=findViewById<Spinner>(R.id.spinnerGrupos)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.tituloOpcao -> {
                    textoBusca.visibility = View.VISIBLE
                    opcaogrupo.visibility = View.GONE
                }
                R.id.grupoOpcao -> {
                    textoBusca.visibility = View.GONE
                    opcaogrupo.visibility = View.VISIBLE
                }
            }
        }

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
                    val grupoSelecionado = spinnerGrupos.selectedItem as? Grupo

                    // Validação para evitar prosseguir se ainda estiver carregando
                    if (grupoSelecionado == null || grupoSelecionado.codigo == "000") {
                        Toast.makeText(this, "Aguarde o carregamento dos grupos...", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    intent.putExtra("TIPO_BUSCA", "GRUPO")
                    intent.putExtra("VALOR_BUSCA", grupoSelecionado.codigo)
                }
            }

            startActivity(intent)
        }
    }

    fun telaInicial(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun pesquisar(view: View) {
        val intent = Intent(this, cardapio::class.java)
        startActivity(intent)
    }
}