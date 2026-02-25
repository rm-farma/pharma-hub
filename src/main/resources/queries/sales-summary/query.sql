SELECT
  COALESCE(SUM(produto_valor_total::numeric), 0) AS total_amount,
  COUNT(DISTINCT nota_fiscal) AS total_orders
FROM bq_licenciado_rel.venda
WHERE data_venda >= :startDate
  AND data_venda < :endDate
  AND cnpj = :cnpj

