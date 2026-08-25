SELECT
  periodoBase,
  periodoComparado,
  faturamentoBase,
  faturamentoComparado,
  variacaoFaturamento,
  itensVendidosBase,
  itensVendidosComparado,
  variacaoItensVendidos,
  ticketMedioBase,
  ticketMedioComparado,
  variacaoTicketMedio
FROM `rm-farma-dw-prod.licenciado.compare_sales_periods`(@cnpj, @startDate1, @endDate1, @startDate2, @endDate2)
