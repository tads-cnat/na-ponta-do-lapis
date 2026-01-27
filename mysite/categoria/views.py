from django.views.generic import ListView
from transacoes.models import Transacao 
from django.shortcuts import render
import json
from django.http import JsonResponse
from django.views import View
from .services import MarcadorService
from .models import Marcador


class CriarMarcadorView(View):
    def post(self, request, *args, **kwargs):
        try:
            body = json.loads(request.body.decode("utf-8"))
        except json.JSONDecodeError:
            return JsonResponse({"erro": "JSON inválido."}, status=400)

        nome = body.get("nome")
        cor = body.get("cor")

        try:
            marcador = MarcadorService.criar_marcador(
            nome=nome,
            cor=cor,
            usuario=request.user)

        except ValueError as e:
            return JsonResponse({"erro": str(e)}, status=400)

        return JsonResponse({
            "nome": marcador.nome,
            "cor": marcador.cor,
        }, status=201)


class ListarMarcadoresView(View):
    def get(self, request, *args, **kwargs):
        marcadores = MarcadorService.listar_marcadores(request.user)
        return JsonResponse(marcadores, safe=False)



# Create your views here.
def categoria(request):
    return render(request, "categoria/categoria.html")

class ExcluirMarcadorView(View):
    def delete(self, request, id, *args, **kwargs):
        try:
            MarcadorService.excluir_marcador(id)
            return JsonResponse({"success": True})
        except ValueError as e:
            return JsonResponse({"erro": str(e)}, status=404)
        

class EditarMarcadorView(View):
    def put(self, request, id, *args, **kwargs):
        try:
            body = json.loads(request.body.decode("utf-8"))
            nome = body.get("nome")

            marcador = MarcadorService.editar_marcador(id, nome)

            return JsonResponse({
                "id": marcador.id,
                "nome": marcador.nome,
                "cor": marcador.cor
            })
        except ValueError as e:
            return JsonResponse({"erro": str(e)}, status=400)



class CategoriaIndex(View): # Ou o nome da sua View de marcadores
    def get(self, request):
        if request.user.is_authenticated:
            # Pegamos todas as transações do usuário
            from transacoes.services import TransacaoService as ts
            transacoes = ts.obter_minhas_transacoes(request.user).order_by('categoria')
            # Pegamos os marcadores
            marcadores = Marcador.objects.filter(usuario=request.user) # Ajuste conforme seu model
        else:
            transacoes = request.session.get("transacoes", [])
            marcadores = [] # Ou lógica de marcadores na sessão

        context = {
            'minhas_transacoes': transacoes,
            'marcadores': marcadores,
        }
        # Aqui você usa o seu template de marcadores/categorias
        return render(request, "categoria/categoria.html", context)
    


class TransacaoIndexView(ListView):
    model = Transacao
    template_name = "categoria/categoria.html"
    context_object_name = "transacoes"