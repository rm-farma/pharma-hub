SELECT *
FROM `rm-farma-dw-prod.licenciado.get_stock_by_search`(@cnpj, @searchTerm)
ORDER BY apresentacao ASC
