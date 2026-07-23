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

// 0. Rota de teste simples (Não usa o banco Aiven)
app.get('/teste', (req, res) => {
  res.send("API Node está viva!");
});

// 1. Buscar Grupos
app.get('/grupos', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM grupo');
    res.json(rows);
  } catch (err) {
    console.error("Erro ao buscar grupos:", err.message);
    res.status(500).json({ error: err.message });
  }
});

// 2. Buscar Produtos
app.get('/produtos', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM produto');
    res.json(rows);
  } catch (err) {
    console.error("Erro ao buscar produtos:", err.message);
    res.status(500).json({ error: err.message });
  }
});

// 3. Buscar Pagamentos
app.get('/pagamentos', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM pagamento');
    res.json(rows);
  } catch (err) {
    console.error("Erro ao buscar pagamentos:", err.message);
    res.status(500).json({ error: err.message });
  }
});

// 4. Salvar Pedido
app.post('/pedidos', async (req, res) => {
  try {
    const pedido = req.body;
    // Ajuste as colunas conforme a sua tabela de pedidos no Aiven
    await pool.query(
      'INSERT INTO pedido (codigo, data, total) VALUES ($1, $2, $3)', 
      [pedido.codigo, pedido.data, pedido.total]
    );
    res.status(201).json({ status: "Pedido recebido com sucesso" });
  } catch (err) {
    console.error("Erro ao salvar pedido:", err.message);
    res.status(500).json({ error: err.message });
  }
});

// Inicia o Servidor
const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 API rodando com sucesso na porta ${PORT}`);
});
