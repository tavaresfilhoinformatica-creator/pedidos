package com.example.nuvem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import buscarCepViaCep
import com.exemplo.meuapp.ui.perfil.PerfilViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class activity_dados_pessoais : AppCompatActivity() {

    private var cepAnterior: String? = null

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

        // Busca o LinearLayout interno onde o padding será aplicado corretamente
        val container = findViewById<View>(R.id.llContainer)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()) // Teclado

            // Aplica o padding no LinearLayout sem acumular ou travar
            container.setPadding(
                16.dpToPx(this),
                systemBars.top + 16.dpToPx(this),
                16.dpToPx(this),
                maxOf(systemBars.bottom, ime.bottom) + 16.dpToPx(this)
            )

            insets
        }

        // --- Observa e preenche os campos quando a tela abre ---
        observarDadosERecuperar()

        // Solicita o carregamento dos dados gravados no banco
        viewModel.carregarDadosUsuario()

        val edtCep = findViewById<EditText>(R.id.etCep)
        edtCep.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val cepFormatado = s.toString().replace("-", "").replace(".", "").trim()

                // Executa a busca assim que preencher os 8 números do CEP, APENAS se for diferente do anterior
                if (cepFormatado.length == 8 && cepFormatado != cepAnterior) {
                    consultarEAtualizarEnderecoPorCep(cepFormatado)
                }
            }
        })
    }

    // Função utilitária para converter DP para Pixels dinamicamente
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun observarDadosERecuperar() {
        viewModel.dadosPessoaisLiveData.observe(this) { dados ->
            if (dados != null) {
                // 1. IMPORTANTE: Atualiza o cepAnterior ANTES de alterar o texto do CEP na tela
                cepAnterior = dados.cep?.replace("-", "")?.replace(".", "")?.trim()

                findViewById<EditText>(R.id.etNome).setText(dados.nome)
                findViewById<EditText>(R.id.etEndereco).setText(dados.endereco)
                findViewById<EditText>(R.id.etCpf).setText(dados.cpf)
                findViewById<EditText>(R.id.etBairro).setText(dados.bairro)
                findViewById<EditText>(R.id.etEstado).setText(dados.estado)
                findViewById<EditText>(R.id.etMunicipio).setText(dados.municipio)
                findViewById<EditText>(R.id.etTelefone).setText(dados.telefone)

                // Quando o setText do CEP roda aqui, o cepAnterior já está populado!
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

        // Atualiza a referência do cepAnterior após salvar
        cepAnterior = cep?.replace("-", "")?.replace(".", "")?.trim()

        Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
    }

    fun telaInicial(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun meusPedidos(view: View) {
        val intent = Intent(this, activity_historico_pedidos::class.java)
        startActivity(intent)
    }

    private fun consultarEAtualizarEnderecoPorCep(cep: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val endereco = buscarCepViaCep(cep)

            withContext(Dispatchers.Main) {
                if (endereco != null) {
                    val etEndereco = findViewById<EditText>(R.id.etEndereco)
                    val etBairro = findViewById<EditText>(R.id.etBairro)
                    val etMunicipio = findViewById<EditText>(R.id.etMunicipio)
                    val etEstado = findViewById<EditText>(R.id.etEstado)

                    // Atualiza a referência para evitar consultas em loop/repetidas
                    cepAnterior = cep

                    // Preenche a tela com os dados do novo CEP para o usuário complementar se necessário
                    etEndereco.setText(endereco.logradouro)
                    etBairro.setText(endereco.bairro)
                    etMunicipio.setText(endereco.localidade)
                    etEstado.setText(endereco.uf)

                } else {
                    Toast.makeText(this@activity_dados_pessoais, "CEP não encontrado.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}