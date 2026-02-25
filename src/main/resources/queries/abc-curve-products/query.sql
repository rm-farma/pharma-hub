WITH vendas_agregadas AS (
  SELECT
    v.ean,
    MIN(v.apresentacao) AS apresentacao,
    COALESCE(SUM(v.produto_valor_total::numeric), 0) AS faturamento_total,
    COALESCE(SUM(v.produto_quantidade::numeric), 0) AS quantidade_vendida,
    COUNT(DISTINCT v.nota_fiscal) AS num_transacoes
  FROM bq_licenciado_rel.venda v
  INNER JOIN bq_licenciado_rel.estoque_atual e
    ON v.ean = e.ean AND e.cnpj = v.cnpj AND e.saldo_estoque::numeric > 0
  WHERE v.cnpj = :cnpj
    AND v.data_venda >= :startDate
    AND v.data_venda < :endDate
  GROUP BY v.ean
  HAVING COALESCE(SUM(v.produto_valor_total::numeric), 0) > 0
),
faturamento_com_percentuais AS (
  SELECT
    va.ean,
    va.apresentacao,
    va.faturamento_total,
    va.quantidade_vendida,
    va.num_transacoes,
    ROUND(
      (va.faturamento_total / NULLIF(SUM(va.faturamento_total) OVER (), 0) * 100)::numeric, 2
    ) AS percentual_individual,
    ROUND(
      (SUM(va.faturamento_total) OVER (
        ORDER BY va.faturamento_total DESC
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
      ) / NULLIF(SUM(va.faturamento_total) OVER (), 0) * 100)::numeric, 2
    ) AS percentual_acumulado
  FROM vendas_agregadas va
),
produtos_classificados AS (
  SELECT
    fp.ean,
    fp.apresentacao,
    fp.faturamento_total,
    fp.quantidade_vendida,
    fp.num_transacoes,
    fp.percentual_individual,
    fp.percentual_acumulado,
    CASE
      WHEN fp.percentual_acumulado <= 80 THEN 'A'
      WHEN fp.percentual_acumulado <= 95 THEN 'B'
      ELSE 'C'
    END AS classe_abc
  FROM faturamento_com_percentuais fp
),
produtos_com_estoque AS (
  SELECT
    pc.ean,
    pc.apresentacao,
    pc.faturamento_total,
    pc.quantidade_vendida,
    pc.num_transacoes,
    pc.percentual_individual,
    pc.percentual_acumulado,
    pc.classe_abc,
    e.saldo_estoque::numeric AS saldo_estoque,
    e.preco_venda::numeric AS preco_venda,
    e.custo_medio::numeric AS custo_medio
  FROM produtos_classificados pc
  LEFT JOIN bq_licenciado_rel.estoque_atual e
    ON pc.ean = e.ean AND e.cnpj = :cnpj
)
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
FROM produtos_com_estoque
WHERE (:classeAbc::text IS NULL OR classe_abc = :classeAbc)
ORDER BY faturamento_total DESC

