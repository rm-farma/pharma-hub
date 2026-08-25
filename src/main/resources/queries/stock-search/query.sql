SELECT
  ean,
  apresentacao,
  fabricante,
  grupo_macro,
  saldo_estoque,
  custo_medio,
  custo_medio_total,
  preco_venda
FROM `rm-farma-dw-prod.licenciado.get_stock_by_search`(@cnpj, @searchTerm)
ORDER BY apresentacao ASC
