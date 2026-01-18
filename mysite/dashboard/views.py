from django.shortcuts import render
import json

from .service import DashboardService


def dashboard_view(request):
    dashboard_service = DashboardService()

    total_receita = 0
    total_despesa = 0
    total = 0
    gastos_por_categoria = []
    historico = []
    
    if request.user.is_authenticated:
        total_receita = dashboard_service.receita_usuario(request.user.id)
        total_despesa = dashboard_service.despesa_usuario(request.user.id)
        total = dashboard_service.total(request.user.id)
        gastos_por_categoria = dashboard_service.gastos_por_categoria(request.user.id)
        historico = dashboard_service.historico_transacoes(request.user.id, limite=10)

    # Preparar dados para o gráfico
    categorias = [item['categoria'] for item in gastos_por_categoria]
    valores = [float(item['total']) for item in gastos_por_categoria]
    
    context = {
        "total_receita": total_receita,
        "total_despesa": total_despesa,
        "total": total,
        "categorias_json": json.dumps(categorias),
        "valores_json": json.dumps(valores),
        "gastos_por_categoria": gastos_por_categoria,
        "historico": historico,
    }

    return render(request, 'dashboard_i.html', context)