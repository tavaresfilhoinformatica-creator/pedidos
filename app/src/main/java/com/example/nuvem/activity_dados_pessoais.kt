package com.example.nuvem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.exemplo.meuapp.ui.perfil.PerfilViewModel

class activity_dados_pessoais : AppCompatActivity() {

    private val viewModel: PerfilViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "furabolo"
                ).build()

                @Suppress("UNCHECKED_CAST")
                return PerfilViewModel(db.DadosPessoaisDao()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dados_pessoais)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- NOVO: Observa e preenche os campos quando a tela abre ---
        observarDadosERecuperar()

        // Solicita o carregamento dos dados gravados no banco
        viewModel.carregarDadosUsuario()
    }

    private fun observarDadosERecuperar() {
        viewModel.dadosPessoaisLiveData.observe(this) { dados ->
            // Se existirem dados salvos anteriormente, preenche na tela
            if (dados != null) {
                findViewById<EditText>(R.id.etNome).setText(dados.nome)
                findViewById<EditText>(R.id.etEndereco).setText(dados.endereco)
                findViewById<EditText>(R.id.etCpf).setText(dados.cpf)
                findViewById<EditText>(R.id.etBairro).setText(dados.bairro)
                findViewById<EditText>(R.id.etEstado).setText(dados.estado)
                findViewById<EditText>(R.id.etMunicipio).setText(dados.municipio)
                findViewById<EditText>(R.id.etTelefone).setText(dados.telefone)
                findViewById<EditText>(R.id.etCep).setText(dados.cep ?: "")
            }
        }
    }

    fun salvarDados(view: View) {
        val edtNome = findViewById<EditText>(R.id.etNome)
        val edtEndereco = findViewById<EditText>(R.id.etEndereco)
        val edtCpf = findViewById<EditText>(R.id.etCpf)
        val edtBairro = findViewById<EditText>(R.id.etBairro)
        val edtEstado = findViewById<EditText>(R.id.etEstado)
        val edtMunicipio = findViewById<EditText>(R.id.etMunicipio)
        val edtTelefone = findViewById<EditText>(R.id.etTelefone)
        val edtCep = findViewById<EditText>(R.id.etCep)

        val nome = edtNome.text.toString()
        val endereco = edtEndereco.text.toString()
        val cpf = edtCpf.text.toString()
        val bairro = edtBairro.text.toString()
        val estado = edtEstado.text.toString()
        val municipio = edtMunicipio.text.toString()
        val telefone = edtTelefone.text.toString()
        val cep = edtCep.text.toString().ifEmpty { null }

        if (nome.isBlank() || cpf.isBlank()) {
            Toast.makeText(this, "Por favor, preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        val novosDados = DadosPessoais(
            nome = nome,
            endereco = endereco,
            cpf = cpf,
            bairro = bairro,
            estado = estado,
            municipio = municipio,
            telefone = telefone,
            cep = cep
        )

        viewModel.salvarDadosUsuario(novosDados)
        Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
    }

    fun telaInicial(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    fun meusPedidos(view: View){
        val intent = Intent(this, activity_historico_pedidos::class.java)
        startActivity(intent)
    }
}