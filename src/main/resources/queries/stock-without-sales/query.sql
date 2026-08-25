SELECT
  ean,
  apresentacao,
  fabricante,
  grupo_macro,
  saldo_estoque,
  custo_medio_total,
  preco_venda
FROM `rm-farma-dw-prod.licenciado.get_stock_without_sales`(@cnpj)
ORDER BY custo_medio_total DESC
