SELECT
  ean,
  productName,
  notaFiscal,
  dataVenda,
  totalQuantity,
  faturamento,
  custo
FROM `rmfarma.ISAZ.get_items_sold_below_cost`(@cnpj, @startDate, @endDate)
ORDER BY dataVenda DESC
