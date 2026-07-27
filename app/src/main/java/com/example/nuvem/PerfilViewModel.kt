package com.exemplo.meuapp.ui.perfil // Ajuste o pacote de acordo com a pasta do seu arquivo

import DadosPessoaisDao
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuvem.DadosPessoais
//import com.example.nuvem.DadosPessoaisDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PerfilViewModel(private val dao: DadosPessoaisDao) : ViewModel() {

    // LiveData que armazenará os dados recuperados do banco
    val dadosPessoaisLiveData = MutableLiveData<DadosPessoais?>()

    // Função para salvar ou atualizar os dados
    fun salvarDadosUsuario(dados: DadosPessoais) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.salvar(dados)
        }
    }

    // --- ADICIONE ESTA FUNÇÃO ABAIXO ---
    fun carregarDadosUsuario() {
        viewModelScope.launch(Dispatchers.IO) {
            val dados = dao.obterDadosPessoais() // Consulta no Room
            dadosPessoaisLiveData.postValue(dados) // Notifica a Activity com o resultado
        }
    }
}