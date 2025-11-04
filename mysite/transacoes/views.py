from django.shortcuts import render

# Create your views here.
def transacoes_index(request):
    return render(request, "transacoes.html")