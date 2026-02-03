from django.shortcuts import render, redirect
from django.http import JsonResponse
from django.core.exceptions import ValidationError
from django.contrib import messages
from django.views.generic import View
from datetime import datetime
from .services import TransacaoService as ts
from contas.services import ContaService
from categoria.services import MarcadorService

class TransacaoIndex(View):

    def get(self, request):
        if request.user.is_authenticated:

            page = request.GET.get("page")
            transacoes = ts.obter_minhas_transacoes(request.user, numero_pag=page)
            contas = ContaService.obter_contas_usuario(request.user)
            marcadores = MarcadorService.listar_marcadores(request.user)
        else:
            transacoes = request.session.get("transacoes",[])
            for t in transacoes:
                t["data_hora"] = datetime.strptime(
                    t["data_hora"],
                    "%Y-%m-%d %H:%M:%S"
                )
            contas = None
            marcadores = None

        context = {
            'categorias':ts.obter_categorias,
            'tipos':ts.obter_tipos,
            'contas': contas,
            'marcadores':marcadores,
            'minhas_transacoes': transacoes
        }

        return render(request, "transacoes.html", context=context)

class TransacaoSalvar(View):

    def post(self, request):
        descricao =  request.POST.get('descricao')
        valor =  request.POST.get('valor')
        categoria =  request.POST.get('categoria')
        estado =  request.POST.get('estado')
        tipo =  request.POST.get('tipo')
        data_hora =  request.POST.get('data_hora')
        conta_financeira =  request.POST.get('conta_financeira')
        marcadores = request.POST.getlist('marcadores')

        if request.user.is_authenticated:
            try:
                t = ts.salvar_transacao_db(
                        descricao = descricao,
                        valor= valor,
                        categoria= categoria,
                        estado = estado,
                        tipo = tipo,
                        data_hora = data_hora,
                        conta_financeira_id = conta_financeira,
                        marcadores_ids = marcadores
                )

            except ValidationError as e:
                messages.error(request,e)
                return redirect('transacoes:index')

            else:
                messages.success(request, "Transação salva com sucesso." )

                return JsonResponse({
                    "success": True,
                    "message": "Transação salva com sucesso.",
                    "id": t.id
                })
        else:
            ts.salvar_transacao_sessao(
                request = request,
                descricao = descricao,
                valor= valor,
                categoria= categoria,
                estado = estado,
                tipo = tipo,
                data_hora = data_hora,
                conta_financeira_id = conta_financeira,
                marcadores_ids = marcadores
            )
            return redirect('transacoes:index')
        
class TransacaoEditar(View):

    def post(self, request, id):
        descricao =  request.POST.get('descricao')
        valor =  request.POST.get('valor')
        categoria =  request.POST.get('categoria')
        estado =  request.POST.get('estado')
        tipo =  request.POST.get('tipo')
        data_hora =  request.POST.get('data_hora')
        conta_financeira =  request.POST.get('conta_financeira')
        marcadores = request.POST.getlist('marcadores')

        if request.user.is_authenticated:
            ts.editar_transacao_db(
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
        else:
             ts.editar_transacao_sessao(
                request=request,
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

        return redirect('transacoes:index')

class TransacaoExcluir(View):

    def post(self, request, id):
        if request.user.is_authenticated:
            ts.excluir_transacao_db(id)
        else:
            ts.excluir_transacao_sessao(request, id)
        return redirect('transacoes:index')
    
class TransacaoFiltrar(View):
    
    def get(self, request):
        filtro_categoria = request.GET.get("categoria")
        filtro_tipo = request.GET.get("tipo")
        filtro_conta = request.GET.get("conta")
        filtro_busca = request.GET.get("busca")
        filtro_data_inicio = request.GET.get("data_inicio")
        filtro_data_fim = request.GET.get("data_fim")

        usuario = request.user

        if not (filtro_busca or filtro_categoria or filtro_tipo or filtro_conta or filtro_data_inicio or filtro_data_fim):
            return redirect('transacoes:index')

        filtro = ts.filtrar_transacao(request, usuario, filtro_busca, filtro_categoria, filtro_tipo, filtro_conta, filtro_data_inicio, filtro_data_fim)

        context = {
            'categorias':ts.obter_categorias,
            'tipos':ts.obter_tipos,
            'contas': ContaService.obter_contas_usuario(request.user),
            'marcadores':MarcadorService.listar_marcadores(request.user),
            'minhas_transacoes': filtro
        }
        return render(request, "transacoes.html", context=context)
    

class TransacaoOrdenar(View):
    
    def get(self, request):
        if request.user.is_authenticated:
            order = request.GET.get("order")
            direcao= request.GET.get("direcao")
            transacoes = ts.obter_minhas_transacoes(request.user, order, direcao)
            contas = ContaService.obter_contas_usuario(request.user)
            marcadores = MarcadorService.listar_marcadores(request.user)

        else:
            contas = None
            marcadores = None
            transacoes = request.session.get("transacoes",[])
            for t in transacoes:
                t["data_hora"] = datetime.strptime(
                    t["data_hora"],
                    "%Y-%m-%d %H:%M:%S"
                )

        context = {
            'categorias':ts.obter_categorias,
            'tipos':ts.obter_tipos,
            'contas': contas,
            'marcadores':marcadores,
            'minhas_transacoes': transacoes
        }
          

        return render(request, "tabela_parcial.html", context=context)


