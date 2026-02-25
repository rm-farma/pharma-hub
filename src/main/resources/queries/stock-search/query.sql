SELECT
  ean,
  apresentacao,
  fabricante,
  grupo_macro,
  saldo_estoque::numeric AS saldo_estoque,
  custo_medio::numeric AS custo_medio,
  custo_medio_total::numeric AS custo_medio_total,
  preco_venda::numeric AS preco_venda
FROM bq_licenciado_rel.estoque_atual
WHERE cnpj = :cnpj
  AND (ean ILIKE :searchTerm OR apresentacao ILIKE :searchTerm)
ORDER BY apresentacao ASC

