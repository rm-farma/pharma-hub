SELECT *
FROM `rm-farma-dw-prod.licenciado.get_idle_stock`(@cnpj)
ORDER BY custo_medio_total DESC
