from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from .services import ContaService

# Create your views here.

def index(request):
    return render(request, 'contas/index.html')

@login_required
def visualizar_conta(request, conta_id):
    return render(request, 'contas/visualizar_conta.html')

@login_required
def add_conta(request):
    nome = request.POST.get('nome')
    saldo = request.POST.get('saldo')
    tipo = request.POST.get('tipo')

    conta = ContaService.AddContaService(nome, saldo, tipo)

    contexto = {
        'conta' : conta,
    }

    if conta:
        return render(request, 'contas/add_conta.html', contexto)
    
    return redirect('index')

@login_required
def editar_conta(request):
    nome = request.POST.get('nome')
    saldo = request.POST.get('saldo')
    tipo = request.POST.get('tipo')

    conta = ContaService.EditarContaService(nome, saldo, tipo)

    contexto = {
        'conta' : conta,
    }

    if conta:
        return render(request, 'contas/editar_conta.html', contexto)
    
    return redirect('index')

@login_required
def excluir_conta(request, conta_id):
    conta = ContaService.ExcluirContaService(conta_id)

    contexto = {
        'conta' : conta,
    }
    
    return redirect('index')