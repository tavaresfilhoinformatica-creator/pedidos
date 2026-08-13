const axios = require('axios');

// Cole a sua NOVA chave gerada aqui (sem espaços)
const COMTELE_API_KEY = 'e785b099-cd9f-48bc-bd2b-bc9c63639906';

async function enviarSMSComtele(telefoneDestino, mensagemTexto) {
  try {
    const numeroLimpo = telefoneDestino.replace(/\D/g, '');

    const response = await axios({
      method: 'post',
      url: 'https://sms.comtele.com.br/api/v2/send',
      headers: {
        'auth-key': COMTELE_API_KEY,
        'Content-Type': 'application/json'
      },
      data: {
        Sender: 'AppPedidos',
        Receivers: numeroLimpo,
        Content: mensagemTexto
      }
    });

    console.log("Resposta da Comtele:", response.data);
  } catch (error) {
    if (error.response) {
      console.log("Status do Erro:", error.response.status);
      console.log("Detalhes do Erro:", error.response.data);
    } else {
      console.log("Erro de Conexão:", error.message);
    }
  }
}

// Coloque seu telefone pessoal com DDD (ex: '21999999999')
enviarSMSComtele('+5521982409308', 'Teste de envio Comtele');