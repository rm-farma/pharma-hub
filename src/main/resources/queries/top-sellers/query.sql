SELECT
  seller,
  totalAmount,
  totalOrders
FROM `rm-farma-dw-prod.licenciado.get_top_sellers_by_period`(@cnpj, @startDate, @endDate)
ORDER BY totalAmount DESC
