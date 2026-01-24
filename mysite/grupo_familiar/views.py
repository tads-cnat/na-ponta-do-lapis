from django.shortcuts import render
from django.views import View
from django.contrib.auth.mixins import LoginRequiredMixin, UserPassesTestMixin

from categoria.models import Marcador
from contas.services import ContaService
from transacoes.models import Transacao
from transacoes.services import TransacaoService as ts
from .services import FamiliaServices
from django.contrib import messages
from .models import Familia
from usuario import models
from usuario.models import Usuario
from django.contrib.auth.decorators import login_required, user_passes_test

# Create your views here.
def is_familiadmin(user):
    return user.papel == Usuario.Papel.ADMIN_FAMILIA

@login_required
def familia(request):
    user = request.user
    if user.id_familia:
        familia_obj = user.id_familia
        nome = familia_obj.nome
        membros = familia_obj.membros
        chefe = Usuario.objects.filter(id_familia=familia_obj, papel=Usuario.Papel.ADMIN_FAMILIA).first()
        return render(request, 'familia/familia_inicio.html', {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user})
    return render(request, 'familia/familia_inicio.html', {'familia': False, 'user': user})
class FamiliaView(View):
    def get(self, request):
        user = request.user
        if user.is_authenticated and user.id_familia:
            familia_obj = user.id_familia
            nome = familia_obj.nome
            membros = familia_obj.membros
            minhas_transacoes = Transacao.objects.filter(conta_financeira__usuario__id_familia=familia_obj)
            chefe = Usuario.objects.filter(id_familia=familia_obj, papel=Usuario.Papel.ADMIN_FAMILIA).first()
            contexto = {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user, 'minhas_transacoes': minhas_transacoes}
            return render(request, 'familia/familia_inicio.html', contexto)
        return render(request, 'familia/familia_inicio.html', {'familia': False, 'user': user})
    def post(self, request):
        user = request.user
        if user.is_authenticated and user.id_familia:
            familia_obj = user.id_familia
            nome = familia_obj.nome
            membros = familia_obj.membros
            chefe = Usuario.objects.filter(id_familia=familia_obj, papel=Usuario.Papel.ADMIN_FAMILIA).first()
            minhas_transacoes = Transacao.objects.filter(conta_financeira__usuario__id_familia=familia_obj)
            contexto = {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user, 'minhas_transacoes': minhas_transacoes}
            return render(request, 'familia/familia_inicio.html', contexto)
        return render(request, 'familia/familia_inicio.html', {'familia': False, 'user': user})

@login_required
def criarfamilia(request):
    if request.method == 'POST':
        nome = request.POST.get('nome')
        print(f"Nome: {nome}")
        if nome:
            try:
                familia = FamiliaServices.adicionarfamilia(nome)
                print(f"Familia criada: {familia}")
                FamiliaServices.tornar_adminFamilia(request.user, familia)
                print(f"User papel: {request.user.papel}, id_familia: {request.user.id_familia}")
                messages.success(request, "Família criada com sucesso!")
                user = request.user
                nome = familia.nome
                membros = familia.membros
                chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
                minhas_transacoes = Transacao.objects.filter(usuario__id_familia=familia)
                contexto = {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user, 'minhas_transacoes': minhas_transacoes}
                return render(request, 'familia/familia_inicio.html', contexto)
            except Exception as e:
                messages.error(request, f"Erro ao criar família: {str(e)}")
        else:
            messages.error(request, "Nome da família é obrigatório.")
    return render(request, 'familia/familia_inicio.html', {'familia': False, 'user' : request.user})
class CriarFamiliaView(View):
    def post(self, request):
        if not request.user.is_authenticated:
            messages.error(request, "Você precisa estar autenticado para criar uma família.")
            return render(request, 'familia/familia_inicio.html', {'familia': False, 'user': request.user})
        
        nome = request.POST.get('nome')
        print(f"Nome: {nome}")
        if nome:
            try:
                familia = FamiliaServices.adicionarfamilia(nome)
                print(f"Familia criada: {familia}")
                FamiliaServices.tornar_adminFamilia(request.user, familia)
                print(f"User papel: {request.user.papel}, id_familia: {request.user.id_familia}")
                messages.success(request, "Família criada com sucesso!")
                user = request.user
                nome = familia.nome
                membros = familia.membros
                chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
                minhas_transacoes = Transacao.objects.filter(conta_financeira__usuario__id_familia=familia)
                contexto = {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user, 'minhas_transacoes': minhas_transacoes}
                return render(request, 'familia/familia_inicio.html', contexto)
            except Exception as e:
                messages.error(request, f"Erro ao criar família: {str(e)}")
        else:
            messages.error(request, "Nome da família é obrigatório.")
        return render(request, 'familia/familia_inicio.html', {'familia': False, 'user' : request.user})
@login_required
@user_passes_test(is_familiadmin)
def adicionarmembro(request, id_familia):
    if request.method == 'POST':
        email = request.POST.get('email')
        if email:
            try:
                familia = Familia.objects.get(id=id_familia)
                FamiliaServices.adicionarmembro(email, familia)
                messages.success(request, "Membro adicionado com sucesso!")
                user = request.user
                nome = familia.nome
                membros = familia.membros
                chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
                return render(request, 'familia/familia_inicio.html', {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user})
            except Exception as e:
                messages.error(request, f"Erro ao adicionar membro: {str(e)}")
        else:
            messages.error(request, "Email é obrigatório para adicionar um membro.")
    user = request.user
    familia = Familia.objects.get(id=id_familia)
    nome = familia.nome
    membros = familia.membros
    chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
    return render(request, 'familia/familia_inicio.html', {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user})
class AdicionarMembroView(LoginRequiredMixin, UserPassesTestMixin, View):
    def test_func(self):
        return self.request.user.papel == Usuario.Papel.ADMIN_FAMILIA
    def post(self, request, id_familia):
        email = request.POST.get('email')
        if email:
            try:
                familia = Familia.objects.get(id=id_familia)
                FamiliaServices.adicionarmembro(email, familia)
                messages.success(request, "Membro adicionado com sucesso!")
                user = request.user
                nome = familia.nome
                membros = familia.membros
                minhas_transacoes = Transacao.objects.filter(conta_financeira__usuario__id_familia=familia)
                chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
                contexto = {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user, 'minhas_transacoes': minhas_transacoes}
                return render(request, 'familia/familia_inicio.html', contexto)
            except Exception as e:
                messages.error(request, f"Erro ao adicionar membro: {str(e)}")
        else:
            messages.error(request, "Email é obrigatório para adicionar um membro.")
    def get(self, request, id_familia):
        user = request.user
        familia = Familia.objects.get(id=id_familia)
        nome = familia.nome
        membros = familia.membros
        chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
        minhas_transacoes = Transacao.objects.filter(conta_financeira__usuario__id_familia=familia)
        contexto = {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user, 'minhas_transacoes': minhas_transacoes}
        return render(request, 'familia/familia_inicio.html', contexto)

@login_required
@user_passes_test(is_familiadmin)
def tirarmembro(request, email):
    if request.method == 'POST':
        try:
            familia = request.user.id_familia
            FamiliaServices.tirarmembro(email, familia)
            messages.success(request, "Membro removido com sucesso!")
            user = request.user
            nome = familia.nome
            membros = familia.membros
            chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
            return render(request, 'familia/familia_inicio.html', {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user})
        except Exception as e:
            messages.error(request, f"Erro ao remover membro: {str(e)}")
    user = request.user
    familia = user.id_familia
    nome = familia.nome
    membros = familia.membros
    chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
    return render(request, 'familia/familia_inicio.html', {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user})
class TirarMembroView(LoginRequiredMixin, UserPassesTestMixin, View):
    def test_func(self):
        return self.request.user.papel == Usuario.Papel.ADMIN_FAMILIA
    def post(self, request, email):
        try:
            familia = request.user.id_familia
            FamiliaServices.tirarmembro(email, familia)
            messages.success(request, "Membro removido com sucesso!")
            user = request.user
            nome = familia.nome
            membros = familia.membros
            chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
            minhas_transacoes = Transacao.objects.filter(conta_financeira__usuario__id_familia=familia)
            contexto = {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user, 'minhas_transacoes': minhas_transacoes}
            return render(request, 'familia/familia_inicio.html', contexto)
        except Exception as e:
            messages.error(request, f"Erro ao remover membro: {str(e)}")
    def get(self, request):
        user = request.user
        familia = user.id_familia
        nome = familia.nome
        membros = familia.membros
        chefe = Usuario.objects.filter(id_familia=familia, papel=Usuario.Papel.ADMIN_FAMILIA).first()
        minhas_transacoes = Transacao.objects.filter(conta_financeira__usuario__id_familia=familia)
        contexto = {'familia': True, 'nome': nome, 'membros': membros, 'chefe': chefe, 'user': user, 'minhas_transacoes': minhas_transacoes}
        return render(request, 'familia/familia_inicio.html', contexto)