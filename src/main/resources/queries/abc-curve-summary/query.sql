SELECT
  total_produtos,
  total_produtos_a,
  total_produtos_b,
  total_produtos_c,
  faturamento_total,
  faturamento_a,
  faturamento_b,
  faturamento_c
FROM `rm-farma-dw-prod.licenciado.get_abc_curve_summary`(@cnpj, @startDate, @endDate)
