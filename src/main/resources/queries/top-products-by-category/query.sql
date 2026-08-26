SELECT
  ean,
  productName,
  totalQuantity,
  totalAmount,
  totalOrders
FROM `rmfarma.ISAZ.get_top_products_by_category`(@cnpj, @categoria, @startDate, @endDate)
ORDER BY totalQuantity DESC, totalAmount DESC
