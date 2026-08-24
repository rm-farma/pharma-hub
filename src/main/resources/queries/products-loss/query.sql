SELECT *
FROM `rm-farma-dw-prod.licenciado.get_products_loss_by_period`(@cnpj, @startDate, @endDate)
ORDER BY (custo - faturamento) DESC
