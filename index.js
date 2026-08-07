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

// 4. Salvar Pedido e Itens no Aiven (Corrigido)
app.post('/pedidos', async (req, res) => {
  const client = await pool.connect();
  try {
    const { 
      cpf, 
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

    // 1. Insere o cabeçalho do pedido (Garantindo o uso do campo 'numero')
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
      RETURNING id;
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
    const pedidoIdNuvem = resPedido.rows[0].id;

    // 2. Insere os itens na tabela 'item_pedido'
    const queryItem = `
      INSERT INTO item_pedido (cpf, pedido_id, produto_id, descricao, quantidade, preco_venda, total_item)
      VALUES ($1, $2, $3, $4, $5, $6, $7);
    `;

    if (itens && Array.isArray(itens)) {
      for (const item of itens) {
        await client.query(queryItem, [
          item.cpf,
          pedidoIdNuvem, // Vincula a chave estrangeira gerada no Aiven
          item.produtoId,
          item.descricao,
          item.quantidade,
          item.precoVenda,
          item.totalItem
        ]);
      }
    }

    await client.query('COMMIT');

    res.status(201).json({ 
      sucesso: true, 
      mensagem: "Pedido e itens gravados no Aiven com sucesso!",
      idPedidoNuvem: pedidoIdNuvem
    });

  } catch (err) {
    await client.query('ROLLBACK');
    console.error("Erro ao salvar pedido no Aiven:", err.message);
    res.status(500).json({ 
      sucesso: false, 
      mensagem: err.message,
      idPedidoNuvem: null 
    });
  } finally {
    client.release();
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 API rodando com sucesso na porta ${PORT}`);
});