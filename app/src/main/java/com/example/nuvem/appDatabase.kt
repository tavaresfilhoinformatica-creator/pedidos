package com.example.nuvem

import DadosPessoaisDao
import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// 1. Registro de todas as entidades/tabelas que criamos
@Database(
    entities = [
        Produto::class,
        ItemPedido::class,
         Grupo::class,
         Pedido::class,
        Pagamento:: class,
        DadosPessoais:: class
    ],
    version = 4,
    exportSchema = false // Evita avisos de exportação de schema durante o build
)
// 2. Registro do nosso conversor para BigDecimal e Datas funcionar em todo o app
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // 3. Registro dos DAOs que você já tem (ex: ProdutoDao)
    abstract fun DadosPessoaisDao() : DadosPessoaisDao
    abstract fun GrupoDao() : GrupoDao
    abstract fun produtoDao(): ProdutoDao
    abstract fun PagamentoDao(): PagamentoDao
    abstract fun pedidoDao(): pedidoDao

    // Conforme você for criando os outros DAOs futuramente,
    // basta vir aqui e declarar as funções deles, por exemplo:
    // abstract fun itemPedidoDao(): ItemPedidoDao
    // abstract fun pedidoDao(): pedidoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 4. Padrão Singleton para garantir que o app use apenas UMA instância do banco
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "furabolo" // Nome físico do arquivo do banco de dados no aparelho
                )
                    // Usado provisoriamente para testes. Se você mudar a estrutura
                    // das entidades, ele limpa o banco anterior em vez de travar o app por falta de migração.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

