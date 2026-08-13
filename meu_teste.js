// Substitua esta linha:
// const url = 'https://portal.comtele.com.br/';

// Por esta linha (endpoint oficial da API v2):
const url = 'https://sms.comtele.com.br/api/v2/send'; 

fetch(url, {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'auth-key': 'e785b099-cd9f-48bc-bd2b-bc9c63639906' // Sua chave de API da Comtele
    },
    body: JSON.stringify({
        Sender: "Teste",
        Content: "Testando envio de SMS",
        Receivers: "21982409308"
    })
})
.then(async response => {
    // Tenta ler o JSON diretamente ou pega o texto para debugar se der erro HTTP
    const isJson = response.headers.get('content-type')?.includes('application/json');
    const data = isJson ? await response.json() : await response.text();

    if (!response.ok) {
        console.error(`Erro do Servidor (${response.status}):`, data);
        throw new Error(`Erro na API (${response.status})`);
    }
    
    return data;
})
.then(data => console.log("Sucesso:", data))
.catch(error => console.error("Erro na requisição:", error.message));