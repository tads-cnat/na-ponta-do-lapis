from django.shortcuts import render
import json
from django.http import JsonResponse
from django.views import View
from .services import MarcadorService


class CriarMarcadorView(View):
    def post(self, request, *args, **kwargs):
        try:
            body = json.loads(request.body.decode("utf-8"))
        except json.JSONDecodeError:
            return JsonResponse({"erro": "JSON inválido."}, status=400)

        nome = body.get("nome")
        cor = body.get("cor")

        try:
            marcador = MarcadorService.criar_marcador(nome, cor)
        except ValueError as e:
            return JsonResponse({"erro": str(e)}, status=400)

        return JsonResponse({
            "id": marcador.id,
            "nome": marcador.nome,
            "cor": marcador.cor,
        }, status=201)


class ListarMarcadoresView(View):
    def get(self, request, *args, **kwargs):
        marcadores = MarcadorService.listar_marcadores()
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