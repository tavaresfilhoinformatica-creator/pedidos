import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nuvem.DadosPessoais
import kotlinx.coroutines.flow.Flow

@Dao
interface DadosPessoaisDao {

    // Salva ou atualiza os dados do usuário (por conta da chave primária fixada)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(dadosPessoais: DadosPessoais)

    // Busca os dados do usuário
    @Query("SELECT * FROM dadospessoais WHERE codigo = 1 LIMIT 1")
    suspend fun obterDadosPessoais(): DadosPessoais?

    // Opcional: Se quiser observar os dados em tempo real na UI
    @Query("SELECT * FROM dadospessoais WHERE codigo = 1 LIMIT 1")
    fun observarDadosPessoais(): Flow<DadosPessoais?>
}