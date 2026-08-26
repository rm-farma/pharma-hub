SELECT
  ean,
  apresentacao,
  faturamento_total,
  quantidade_vendida,
  num_transacoes,
  percentual_individual,
  percentual_acumulado,
  classe_abc,
  saldo_estoque,
  preco_venda,
  custo_medio
FROM `rmfarma.ISAZ.get_abc_curve_products`(@cnpj, @startDate, @endDate, @classeAbc)
ORDER BY faturamento_total DESC
