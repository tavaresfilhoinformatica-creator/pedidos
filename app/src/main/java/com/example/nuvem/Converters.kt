package com.example.nuvem

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class Converters {

    // Sempre que o Room vir QUALQUER campo BigDecimal no banco (String), ele usa isso
    @TypeConverter
    fun fromString(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it).setScale(2, RoundingMode.HALF_EVEN) }
    }

    // Sempre que o Room for salvar QUALQUER campo BigDecimal, ele usa isso
    @TypeConverter
    fun amountToString(value: BigDecimal?): String? {
        return value?.setScale(2, RoundingMode.HALF_EVEN)?.toPlainString()
    }

    // O mesmo vale para as datas: um único conversor atende o app inteiro
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
}