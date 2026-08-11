const twilio = require('twilio');

// Suas credenciais já confirmadas
const accountSid = 'AC36fa547c0cc4c3a14bc597d39635afd6';
const authToken = '1f5a0d872583be39448d36c25126d4eb';

const client = twilio(accountSid, authToken);

async function obterNumeroTrial() {
  try {
    console.log("Procurando um número de teste disponível...");
    
    // 1. Procura um número disponível nos EUA
    const numeros = await client.availablePhoneNumbers('US')
      .local
      .list({ limit: 1 });

    if (numeros.length === 0) {
      console.log("Nenhum número encontrado.");
      return;
    }

    const numeroEncontrado = numeros[0].phoneNumber;
    console.log(`Número encontrado: ${numeroEncontrado}. Comprando com saldo Trial...`);

    // 2. Compra o número automaticamente para sua conta
    const numeroComprado = await client.incomingPhoneNumbers
      .create({ phoneNumber: numeroEncontrado });

    console.log("\n SUCCESS!");
    console.log(`Seu Twilio Phone Number é: ${numeroComprado.phoneNumber}`);
  } catch (error) {
    console.error("\n Erro ao gerar número via API:", error.message);
  }
}

obterNumeroTrial();