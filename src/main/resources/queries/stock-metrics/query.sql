SELECT
  ie.cnpj,
  ie.grupo_economico,
  COALESCE(ea.total_custo_estoque, 0) AS total_custo_estoque,
  ie.total_itens_alta_rotatividade
FROM bq_licenciado_rel.item_estoque ie
LEFT JOIN (
  SELECT
    cnpj,
    SUM(custo_medio_total::numeric) AS total_custo_estoque
  FROM bq_licenciado_rel.estoque_atual
  WHERE saldo_estoque::numeric > 0
  GROUP BY cnpj
) ea ON ea.cnpj = ie.cnpj
WHERE ie.cnpj = :cnpj
LIMIT 1

