SELECT *
FROM `rm-farma-dw-prod.licenciado.get_abc_curve_products`(@cnpj, @startDate, @endDate, @classeAbc)
ORDER BY faturamento_total DESC
