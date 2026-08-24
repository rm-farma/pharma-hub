SELECT *
FROM `rm-farma-dw-prod.licenciado.get_items_sold_below_cost`(@cnpj, @startDate, @endDate)
ORDER BY dataVenda DESC
