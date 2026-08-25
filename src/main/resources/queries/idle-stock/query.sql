SELECT
  ean,
  apresentacao,
  fabricante,
  grupo_macro,
  saldo_estoque,
  custo_medio_total,
  preco_venda,
  total_skus,
  total_unidades,
  valor_total_custo,
  valor_total_venda
FROM `rm-farma-dw-prod.licenciado.get_idle_stock`(@cnpj)
ORDER BY custo_medio_total DESC
