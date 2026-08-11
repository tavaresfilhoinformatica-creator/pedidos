package com.example.nuvem

import android.icu.lang.UCharacter
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "pagamento")
data class Pagamento(
    @PrimaryKey val codigo: Long,
    val descricao: String,
    val taxa: BigDecimal,
    val prazo1: Int,
    val prazo2: Int,
    val prazo3: Int
)