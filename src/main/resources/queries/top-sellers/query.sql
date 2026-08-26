SELECT
  seller,
  totalAmount,
  totalOrders
FROM `rmfarma.ISAZ.get_top_sellers_by_period`(@cnpj, @startDate, @endDate)
ORDER BY totalAmount DESC
