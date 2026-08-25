SELECT
  totalAmount,
  totalOrders
FROM `rm-farma-dw-prod.licenciado.get_sales_summary_by_period`(@cnpj, @startDate, @endDate)
