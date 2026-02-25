WITH vendas_agregadas AS (
  SELECT
    v.ean,
    COALESCE(SUM(v.produto_valor_total::numeric), 0) AS faturamento_total
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
    va.faturamento_total,
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
    fp.faturamento_total,
    CASE
      WHEN fp.percentual_acumulado <= 80 THEN 'A'
      WHEN fp.percentual_acumulado <= 95 THEN 'B'
      ELSE 'C'
    END AS classe_abc
  FROM faturamento_com_percentuais fp
)
SELECT
  COUNT(*) AS total_produtos,
  COUNT(*) FILTER (WHERE classe_abc = 'A') AS total_produtos_a,
  COUNT(*) FILTER (WHERE classe_abc = 'B') AS total_produtos_b,
  COUNT(*) FILTER (WHERE classe_abc = 'C') AS total_produtos_c,
  COALESCE(SUM(faturamento_total), 0) AS faturamento_total,
  COALESCE(SUM(faturamento_total) FILTER (WHERE classe_abc = 'A'), 0) AS faturamento_a,
  COALESCE(SUM(faturamento_total) FILTER (WHERE classe_abc = 'B'), 0) AS faturamento_b,
  COALESCE(SUM(faturamento_total) FILTER (WHERE classe_abc = 'C'), 0) AS faturamento_c
FROM produtos_classificados

