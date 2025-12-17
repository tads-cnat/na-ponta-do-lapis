from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from mysite.decorators import papel_requerido
from .services import TransacaoService as ts
from contas.models import ContaFinanceira
from categoria.models import Marcador

# Create your views here.
#@login_required
#@papel_requerido('admin','usuario')
def transacoes_index(request):
    context = {
        'categorias':ts.obter_categorias,
        'estados':ts.obter_estados,
        'tipos':ts.obter_tipos,
        'contas': ContaFinanceira.objects.all,
        'marcadores':Marcador.objects.all,
        'minhas_transacoes':ts.obter_minhas_transacoes
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
    marcadores = request.POST.getlist('marcadores')

    ts.adicionar_transacao(
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

def editar_transacao(request, id):
    descricao =  request.POST.get('descricao')
    valor =  request.POST.get('valor')
    categoria =  request.POST.get('categoria')
    estado =  request.POST.get('estado')
    tipo =  request.POST.get('tipo')
    data_hora =  request.POST.get('data_hora')
    conta_financeira =  request.POST.get('conta_financeira')
    marcadores = request.POST.getlist('marcadores')
    print(marcadores)

    ts.editar_transacao(
        id_transacao= id,
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

def excluir_transacao(request, id):
    ts.excluir_transacao(id)
    return redirect(transacoes_index)

def filtrar_transacao(request):
    filtro_categoria = request.GET.get("categoria")
    filtro_tipo = request.GET.get("tipo")
    filtro_conta = request.GET.get("conta")

    if not (filtro_categoria or filtro_tipo or filtro_conta):
         return redirect(transacoes_index)

    filtro = ts.filtrar_transacao(filtro_categoria, filtro_tipo, filtro_conta)
    context = {
        'categorias':ts.obter_categorias,
        'estados':ts.obter_estados,
        'tipos':ts.obter_tipos,
        'contas': ContaFinanceira.objects.all,
        'marcadores':Marcador.objects.all,
        'minhas_transacoes': filtro
    }
    return render(request, "transacoes.html", context=context)
