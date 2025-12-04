from django.shortcuts import render
from .services import FamiliaServices
from .models import Familia
from .models import Usuario
from django.contrib.auth.decorators import login_required, user_passes_test

# Create your views here.
def is_familiadmin(user):
    return user.papel == Usuario.Papel.ADMIN_FAMILIA

def familia(request):
    return render(request, 'familia/familia_inicio.html', {'familia': False})

@login_required
def criarfamilia(request):
    nome = request.POST.get('nome')
    FamiliaServices.adicionarfamilia(nome)
    user = request.user
    user.tornar_adminFamilia()
    return render(request, 'familia/familia_inicio.html', {'familia' : True, 'nome' : nome})

@login_required
@user_passes_test(is_familiadmin)
def adicionarmembro(request, id):
    email = request.POST.get('email')
    FamiliaServices.adicionarmembro(email, id)
    familia = Familia.objects.get(id=id)
    membros = familia.membros
    return render(request, 'familia/familia_inicio.html', {'familia' : True, 'membros' : membros})