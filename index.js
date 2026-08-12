require('dotenv').config();
const express = require('express');
const { Pool } = require('pg');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// Conexão com o PostgreSQL do Aiven
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: {
    rejectUnauthorized: false
  }
});

// --- ROTAS DA API ---

app.get('/teste', (req, res) => {
  res.send("API Node está viva!");
});

app.get('/grupos', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM grupo');
    res.json(rows);
  } catch (err) {
    console.error("Erro ao buscar grupos:", err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get('/produtos', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM produto');
    res.json(rows);
  } catch (err) {
    console.error("Erro ao buscar produtos:", err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get('/pagamentos', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM pagamento');
    res.json(rows);
  } catch (err) {
    console.error("Erro ao buscar pagamentos:", err.message);
    res.status(500).json({ error: err.message });
  }
});

// Rota de Clientes mantida caso seja chamada individualmente
app.post('/clientes', async (req, res) => {
  try {
    const { 
      codigo, nome, endereco, cpf, bairro, estado, municipio, 
      cep, email, niver, telefone_1, telefone_2, telefone_3, obs 
    } = req.body;

    const queryCliente = `
      INSERT INTO cliente (
        codigo, nome, endereco, cpf, bairro, estado, municipio, cep, email, niver, telefone_1, telefone_2, telefone_3, obs
      )
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
      ON CONFLICT (cpf) DO UPDATE SET
        nome = EXCLUDED.nome,
        endereco = EXCLUDED.endereco,
        bairro = EXCLUDED.bairro,
        estado = EXCLUDED.estado,
        municipio = EXCLUDED.municipio,
        cep = EXCLUDED.cep,
        email = EXCLUDED.email,
        niver = EXCLUDED.niver,
        telefone_1 = EXCLUDED.telefone_1,
        telefone_2 = EXCLUDED.telefone_2,
        telefone_3 = EXCLUDED.telefone_3,
        obs = EXCLUDED.obs;
    `;

    const valoresCliente = [
      codigo || '0001',
      nome || 'Não informado',
      endereco || '',
      cpf,
      bairro || '',
      estado || 'RJ',
      municipio || 'RIO DE JANEIRO',
      cep || '',
      email || '',
      niver || null,
      telefone_1 || '',
      telefone_2 || null,
      telefone_3 || null,
      obs || null
    ];

    await pool.query(queryCliente, valoresCliente);

    console.log(`[CLIENTE] CPF ${cpf} processado com sucesso.`);

    res.status(200).json({ 
      sucesso: true, 
      mensagem: "Cliente salvo/atualizado no Aiven com sucesso!" 
    });

  } catch (err) {
    console.error("Erro ao salvar cliente no Aiven:", err.message);
    res.status(500).json({ 
      sucesso: false, 
      mensagem: err.message 
    });
  }
});

// 4. Salvar Pedido, Cliente e Itens no Aiven (Unificado)
app.post('/pedidos', async (req, res) => {
  const client = await pool.connect();
  try {
    const { 
      cpf, 
      cliente_nome,
      codigo_cliente,
      numero, 
      data_pedido,      
      formaPagamento,
      totalPedido, 
      enderecoEntrega, 
      bairroEntrega, 
      telefoneEntrega, 
      obs, 
      cep_Entrega,
      itens 
    } = req.body;

    await client.query('BEGIN');

    // 1. GRAVA OU ATUALIZA O CLIENTE PRIMEIRO
    if (cpf) {
      const queryCliente = `
        INSERT INTO cliente (
          codigo, nome, endereco, cpf, bairro, estado, municipio, cep, telefone_1, obs
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
        ON CONFLICT (cpf) DO UPDATE SET
          nome = COALESCE(EXCLUDED.nome, cliente.nome),
          endereco = COALESCE(EXCLUDED.endereco, cliente.endereco),
          bairro = COALESCE(EXCLUDED.bairro, cliente.bairro),
          cep = COALESCE(EXCLUDED.cep, cliente.cep),
          telefone_1 = COALESCE(EXCLUDED.telefone_1, cliente.telefone_1);
      `;

      const valoresCliente = [
        codigo_cliente || '0001',
        cliente_nome || 'Não informado',
        enderecoEntrega || '',
        cpf,
        bairroEntrega || '',
        'RJ',
        'RIO DE JANEIRO',
        cep_Entrega || '',
        telefoneEntrega || '',
        obs || null
      ];

      await client.query(queryCliente, valoresCliente);
      console.log(`[CLIENTE] CPF ${cpf} gravado/atualizado via /pedidos.`);
    }

    // 2. INSERE O CABEÇALHO DO PEDIDO
    const queryPedido = `
      INSERT INTO pedido (
        cpf, 
        numero, 
        data_pedido, 
        forma_pagamento, 
        total_pedido, 
        endereco_entrega, 
        bairro_entrega, 
        telefone_entrega, 
        obs, 
        cep_entrega
      )
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
      RETURNING cpf, numero;
    `;
    
    const valoresPedido = [
      cpf, 
      numero, 
      data_pedido, 
      formaPagamento, 
      totalPedido, 
      enderecoEntrega, 
      bairroEntrega, 
      telefoneEntrega, 
      obs, 
      cep_Entrega
    ];

    const resPedido = await client.query(queryPedido, valoresPedido);
    const pedidoCriado = resPedido.rows[0];

    // 3. INSERE OS ITENS
    const queryItem = `
      INSERT INTO item_pedido (cpf, pedido, produto, quantidade, preco_venda, total_item)
      VALUES ($1, $2, $3, $4, $5, $6);
    `;

    if (itens && Array.isArray(itens)) {
      for (const item of itens) {
        await client.query(queryItem, [
          item.cpf || cpf,
          String(numero), 
          item.produto || item.produtoId, 
          item.quantidade,
          item.precoVenda || item.preco_venda,
          item.totalItem || item.total_item
        ]);
      }
    }

    await client.query('COMMIT');

    res.status(201).json({ 
      sucesso: true, 
      mensagem: "Cliente, Pedido e Itens gravados no Aiven com sucesso!",
      numeroPedido: pedidoCriado.numero
    });

  } catch (err) {
    await client.query('ROLLBACK');
    console.error("Erro ao salvar pedido no Aiven:", err.message);
    res.status(500).json({ 
      sucesso: false, 
      mensagem: err.message,
      numeroPedido: null 
    });
  } finally {
    client.release();
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 API rodando com sucesso na porta ${PORT}`);
});