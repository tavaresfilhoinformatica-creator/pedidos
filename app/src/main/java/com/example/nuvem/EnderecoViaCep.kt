import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Data Class simples para o resultado do ViaCEP
data class EnderecoViaCep(
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String,
    val erro: Boolean = false
)

// Função que faz a requisição à API do ViaCEP
fun buscarCepViaCep(cep: String): EnderecoViaCep? {
    val cepLimpo = cep.replace("-", "").replace(".", "").trim()
    if (cepLimpo.length != 8) return null

    return try {
        val url = URL("https://viacep.com.br/ws/$cepLimpo/json/")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)

            if (json.has("erro") && json.getBoolean("erro")) {
                null
            } else {
                EnderecoViaCep(
                    logradouro = json.optString("logradouro", ""),
                    bairro = json.optString("bairro", ""),
                    localidade = json.optString("localidade", ""),
                    uf = json.optString("uf", "")
                )
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}