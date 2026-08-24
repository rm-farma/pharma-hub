SELECT *
FROM `rm-farma-dw-prod.licenciado.get_stock_without_sales`(@cnpj)
ORDER BY custo_medio_total DESC
