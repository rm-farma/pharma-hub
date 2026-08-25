SELECT
  cnpj,
  grupo_economico,
  total_custo_estoque,
  total_itens_alta_rotatividade
FROM `rm-farma-dw-prod.licenciado.get_stock_metrics`(@cnpj)
