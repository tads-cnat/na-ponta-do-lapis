from django.views.generic import ListView
from transacoes.models import Transacao 
from django.shortcuts import render
import json
from django.http import JsonResponse
from django.views import View
from .services import MarcadorService
from .models import Marcador
from django.db.models import Sum



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
            usuario=request.user
)

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
# def categoria(request):
#     return render(request, "categoria/categoria.html")

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
    


class TransacaoIndexView(View):
    template_name = "categoria/categoria.html"

    def get(self, request, *args, **kwargs):
        # Transações do usuário
        transacoes = Transacao.objects.filter(
            conta_financeira__usuario=request.user
        ).order_by("categoria")

        # Dados do gráfico
        dados_grafico = (
            Transacao.objects
            .filter(conta_financeira__usuario=request.user)
            .values("categoria")
            .annotate(total=Sum("valor"))
            .order_by("categoria")
        )

        context = {
            "transacoes": transacoes,
            "labels": json.dumps([d["categoria"] for d in dados_grafico]),
            "valores": json.dumps([float(d["total"]) for d in dados_grafico]),
        }

        return render(request, self.template_name, context)
