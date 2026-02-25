SELECT
  apresentacao AS product_name,
  COALESCE(SUM(produto_quantidade::numeric), 0) AS total_quantity,
  COALESCE(SUM(produto_valor_total::numeric), 0) AS total_amount
FROM bq_licenciado_rel.venda
WHERE data_venda >= :startDate
  AND data_venda < :endDate
  AND cnpj = :cnpj
GROUP BY apresentacao
ORDER BY total_quantity DESC, total_amount DESC
LIMIT :limit

