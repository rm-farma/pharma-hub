SELECT
  ean,
  productName,
  totalQuantity,
  faturamento,
  custo
FROM `rmfarma.ISAZ.get_products_loss_by_period`(@cnpj, @startDate, @endDate)
ORDER BY (custo - faturamento) DESC
