package com.example.nuvem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



    }
    fun encerrar_aplicativo(view : View) {
        val btnEncerrar = findViewById<Button>(R.id.btnEncerrar)
        btnEncerrar.setOnClickListener { finishAffinity() }
    }
    fun dadosPessoais(view : View) {
        val intent = Intent(this, activity_dados_pessoais::class.java)
        startActivity(intent)
    }
    fun cardapio(view : View) {
        val intent = Intent(this, telaPesquisa()::class.java)
        startActivity(intent)
    }
}