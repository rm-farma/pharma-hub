SELECT
  productName,
  totalQuantity,
  totalAmount
FROM `rm-farma-dw-prod.licenciado.get_top_products_by_period`(@cnpj, @startDate, @endDate)
ORDER BY totalQuantity DESC, totalAmount DESC
