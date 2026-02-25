WITH estoque_parado AS (
  SELECT
    e.ean,
    e.apresentacao,
    e.fabricante,
    e.grupo_macro,
    e.saldo_estoque::numeric AS saldo_estoque,
    e.custo_medio_total::numeric AS custo_medio_total,
    e.preco_venda::numeric AS preco_venda
  FROM bq_licenciado_rel.estoque_atual e
  WHERE e.cnpj = :cnpj
    AND e.saldo_estoque::numeric > 0
    AND NOT EXISTS (
      SELECT 1
      FROM bq_licenciado_rel.venda v
      WHERE v.cnpj = e.cnpj
        AND v.ean = e.ean
        AND v.produto_valor_total::numeric > 0
    )
),
resumo AS (
  SELECT
    COUNT(*)::text AS total_skus,
    COALESCE(SUM(saldo_estoque), 0)::text AS total_unidades,
    COALESCE(SUM(custo_medio_total), 0)::text AS valor_total_custo,
    COALESCE(SUM(saldo_estoque * preco_venda), 0)::text AS valor_total_venda
  FROM estoque_parado
)
SELECT
  ep.ean,
  ep.apresentacao,
  ep.fabricante,
  ep.grupo_macro,
  ep.saldo_estoque,
  ep.custo_medio_total,
  ep.preco_venda,
  r.total_skus,
  r.total_unidades,
  r.valor_total_custo,
  r.valor_total_venda
FROM estoque_parado ep
CROSS JOIN resumo r
ORDER BY ep.custo_medio_total DESC
LIMIT :limit

