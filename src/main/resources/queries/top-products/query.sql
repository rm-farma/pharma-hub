SELECT
  productName,
  totalQuantity,
  totalAmount
FROM `rmfarma.ISAZ.get_top_products_by_period`(@cnpj, @startDate, @endDate)
ORDER BY totalQuantity DESC, totalAmount DESC
