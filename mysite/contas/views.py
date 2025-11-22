from django.shortcuts import render

# Create your views here.

def index(request):
    return render(request, 'contas/index.html')

def visualizar_conta(request):
    return render(request, 'contas/visualizar_conta.html')

def add_conta(request):
    return render(request, 'contas/add_conta.html')

def editar_conta(request):
    return render(request, 'contas/editar_conta.html')

def excluir_conta(request):
    return render(request, 'contas/excluir_conta.html')