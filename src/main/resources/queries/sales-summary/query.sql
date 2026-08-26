SELECT
  totalAmount,
  totalOrders
FROM `rmfarma.ISAZ.get_sales_summary_by_period`(@cnpj, @startDate, @endDate)
