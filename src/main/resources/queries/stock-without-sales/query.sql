SELECT
  e.ean,
  e.apresentacao,
  e.fabricante,
  e.grupo_macro,
  e.saldo_estoque::numeric AS saldo_estoque,
  e.custo_medio_total::numeric AS custo_medio_total,
  e.preco_venda::numeric AS preco_venda
FROM bq_licenciado_rel.estoque_atual e
WHERE e.saldo_estoque::numeric > 0
  AND e.cnpj = :cnpj
  AND NOT EXISTS (
    SELECT 1
    FROM bq_licenciado_rel.venda v
    WHERE v.cnpj = e.cnpj
      AND v.ean = e.ean
      AND v.produto_valor_total::numeric > 0
  )
ORDER BY e.custo_medio_total::numeric DESC
LIMIT :limit

