const twilio = require('twilio');

// Cole suas credenciais da Twilio abaixo:
const accountSid = 'AC36fa547c0cc4c3a14bc597d39635afd6'; // Começa com AC...
const authToken = '1f5a0d872583be39448d36c25126d4eb';   // Seu token secreto

const client = twilio(accountSid, authToken);

async function enviarSMSTwilio() {
  try {
    const message = await client.messages.create({
      body: 'Seu pedido #1001 foi registrado com sucesso via Twilio! 🚀',
      from: '+17372508034', // Seu número Twilio (mantenha o +1)
      to: '+5521982409308cls'  // Seu celular real com +55 e DDD (ex: +5521999999999)
    });

    console.log(' SMS ENVIADO COM SUCESSO!');
    console.log('ID da Mensagem (SID):', message.sid);
  } catch (error) {
    console.error(' Erro ao enviar SMS via Twilio:');
    console.error(error.message);
  }
}

enviarSMSTwilio();