// 4. Salvar Pedido, Cliente e Itens no Aiven (Unificado + Log de Diagnóstico)
app.post('/pedidos', async (req, res) => {
  // --- IMPRIME O QUE CHEGOU DO ANDROID NO PAINEL DO RENDER ---
  console.log("=== REQUISIÇÃO RECEBIDA DO ANDROID ===");
  console.log(JSON.stringify(req.body, null, 2));
  console.log("======================================");

  const client = await pool.connect();
  try {
    const { 
      cpf, 
      nome, 
      cliente_nome, 
      clienteNome, 
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
          codigo, nome, endereco, cpf, bairro, estado, municipio, cep, email, niver, telefone_1, obs
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
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
          obs = EXCLUDED.obs;
      `;

      const nomeFinal = nome || cliente_nome || clienteNome || req.body.cliente || 'Não informado';

      const valoresCliente = [
        codigo_cliente || '0001',
        nomeFinal,
        enderecoEntrega || req.body.endereco || '',
        cpf,
        bairroEntrega || req.body.bairro || '',
        req.body.estado || 'RJ',
        req.body.municipio || 'RIO DE JANEIRO',
        cep_Entrega || req.body.cep || '',
        req.body.email || '', 
        req.body.niver || '',
        telefoneEntrega || req.body.telefone_1 || '',
        obs || null
      ];

      await client.query(queryCliente, valoresCliente);
      console.log(`[CLIENTE] CPF ${cpf} (${nomeFinal}) processado.`);
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
      totalPedido ? totalPedido : 0, // Fallback se vier algo zerado
      enderecoEntrega, 
      bairroEntrega, 
      telefoneEntrega, 
      obs, 
      cep_Entrega
    ];

    // Ajuste dos valores reais do pedido
    const resPedido = await client.query(queryPedido, [
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
    ]);
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
      mensagem: "Cliente, Pedido e Itens gravados no Banco de dados Aiven com sucesso!",
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