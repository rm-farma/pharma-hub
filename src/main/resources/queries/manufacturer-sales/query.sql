SELECT
  ean,
  productName,
  totalQuantity,
  totalAmount,
  totalOrders
FROM `rm-farma-dw-prod.licenciado.get_manufacturer_sales_by_period`(@cnpj, @manufacturerPattern, @startDate, @endDate)
ORDER BY totalAmount DESC
