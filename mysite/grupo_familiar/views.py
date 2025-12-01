from django.shortcuts import render
from .services import FamiliaServices
from .models import Familia
from .models import Usuario

# Create your views here.
def familia(request):
    return render(request, 'familia/familia_inicio.html', {'familia': False})
def criarfamilia(request):
    nome = request.POST.get('nome')
    FamiliaServices.adicionarfamilia(nome)
    return render(request, 'familia/familia_inicio.html', {'familia' : True, 'nome' : nome})
def adicionarmembro(request, id):
    email = request.POST.get('email')
    FamiliaServices.adicionarmembro(email, id)
    familia = Familia.objects.get(id=id)
    membros = familia.membros
    return render(request, 'familia/familia_inicio.html', {'familia' : True, 'membros' : membros})