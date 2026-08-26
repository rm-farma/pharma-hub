SELECT
  totalAmount,
  cmv,
  totalOrders
FROM `rmfarma.ISAZ.get_sales_overview`(@cnpj, @startDate, @endDate)
