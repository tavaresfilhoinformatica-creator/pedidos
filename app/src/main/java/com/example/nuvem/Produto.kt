package com.example.nuvem

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.math.BigDecimal

@Entity(
    tableName = "produto",
    // Declarando a relação com a tabela grupo
    foreignKeys = [
        ForeignKey(
            entity = Grupo::class,          // A classe da tabela Pai
            parentColumns = ["codigo"],     // O campo chave na tabela Grupo
            childColumns = ["grupo_id"],    // O campo que faz o vínculo aqui na tabela Produto

        )
    ]
)

data class Produto(
    @PrimaryKey val codigo: String,
    @SerializedName("titulo")
    val descricao: String,

    // Mudamos o nome para deixar claro que armazena a chave/código do grupo
    @SerializedName("grupo")
    @ColumnInfo(name = "grupo_id") val grupoId: String,

    val venda: BigDecimal,
    @SerializedName("imagem_web")
     val imagem_web: String?,
    val obs: String
): Serializable

