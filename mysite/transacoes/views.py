from django.shortcuts import render, redirect
from .services import TransacaoService as TS
from contas.models import ContaFinanceira
from categoria.models import Marcador

# Create your views here.
def transacoes_index(request):
    context = {
        'categorias':TS.obter_categorias,
        'estados':TS.obter_estados,
        'tipos':TS.obter_tipos,
        'contas': ContaFinanceira.objects.all,
        'marcadores':Marcador.objects.all,
    }
    return render(request, "transacoes.html", context=context)

def adicionar_transacao_view(request):
    descricao =  request.POST.get('descricao')
    valor =  request.POST.get('valor')
    categoria =  request.POST.get('categoria')
    estado =  request.POST.get('estado')
    tipo =  request.POST.get('tipo')
    data_hora =  request.POST.get('data_hora')
    conta_financeira =  request.POST.get('conta_financeira')
    marcadores = request.POST.get('marcadores')

    TS.adicionar_transacao(
        descricao = descricao,
        valor= valor,
        categoria= categoria,
        estado = estado,
        tipo = tipo,
        data_hora = data_hora,
        conta_financeira_id = conta_financeira,
        marcadores_ids = marcadores

    )
    return redirect(transacoes_index)