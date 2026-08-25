SELECT
  ean,
  productName,
  totalQuantity,
  totalAmount,
  totalOrders
FROM `rm-farma-dw-prod.licenciado.get_top_products_by_category`(@cnpj, @categoria, @startDate, @endDate)
ORDER BY totalQuantity DESC, totalAmount DESC
