WITH vendas_por_periodo AS (
  SELECT
    CASE
      WHEN data_venda >= :startDate1 AND data_venda < :endDate1 THEN 'base'
      WHEN data_venda >= :startDate2 AND data_venda < :endDate2 THEN 'comparado'
    END AS periodo,
    SUM(produto_valor_total::numeric) AS faturamento,
    SUM(produto_quantidade::numeric) AS itens_vendidos,
    SUM(produto_valor_total::numeric) / NULLIF(COUNT(DISTINCT nota_fiscal), 0) AS ticket_medio
  FROM bq_licenciado_rel.venda
  WHERE
    data_venda ~ '^\d{4}-\d{2}-\d{2}'
    AND cnpj = :cnpj
    AND (
      (data_venda >= :startDate1 AND data_venda < :endDate1)
      OR (data_venda >= :startDate2 AND data_venda < :endDate2)
    )
  GROUP BY periodo
),
base AS (
  SELECT * FROM vendas_por_periodo WHERE periodo = 'base'
),
comparado AS (
  SELECT * FROM vendas_por_periodo WHERE periodo = 'comparado'
)
SELECT
  :startDate1 AS periodo_base,
  :startDate2 AS periodo_comparado,
  COALESCE(base.faturamento, 0) AS faturamento_base,
  COALESCE(comparado.faturamento, 0) AS faturamento_comparado,
  ROUND(
    (COALESCE(comparado.faturamento, 0) - COALESCE(base.faturamento, 0))
    / NULLIF(COALESCE(base.faturamento, 0), 0) * 100, 2
  ) AS variacao_faturamento,
  COALESCE(base.itens_vendidos, 0) AS itens_vendidos_base,
  COALESCE(comparado.itens_vendidos, 0) AS itens_vendidos_comparado,
  ROUND(
    (COALESCE(comparado.itens_vendidos, 0) - COALESCE(base.itens_vendidos, 0))
    / NULLIF(COALESCE(base.itens_vendidos, 0), 0) * 100, 2
  ) AS variacao_itens_vendidos,
  COALESCE(base.ticket_medio, 0) AS ticket_medio_base,
  COALESCE(comparado.ticket_medio, 0) AS ticket_medio_comparado,
  ROUND(
    (COALESCE(comparado.ticket_medio, 0) - COALESCE(base.ticket_medio, 0))
    / NULLIF(COALESCE(base.ticket_medio, 0), 0) * 100, 2
  ) AS variacao_ticket_medio
FROM base FULL OUTER JOIN comparado ON true

