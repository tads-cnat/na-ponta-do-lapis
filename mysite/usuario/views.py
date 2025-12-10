from django.shortcuts import render, redirect
from django.test import TestCase
from django.http import HttpResponse
from django.contrib.auth import get_user_model, authenticate, login as auth_login, logout as auth_logout
from django.contrib import messages
from django.db import IntegrityError


# Create your tests here.

Usuario = get_user_model()

def login_usuario(request):
    if request.method == "POST":
        email = request.POST.get("email")
        senha = request.POST.get("senha")
        print(email, senha)

        usuario = authenticate(request, email=email, password=senha)

        if usuario is not None:
            auth_login(request, usuario)
            return redirect("transacoes_index")
        else:
            messages.error(request, "Email ou senha inválidos.")
            return render(request, "login.html")
    
    return render(request, "login.html")

def cadastro_usuario(request):
    if request.method == "GET":
        return render(request, "cadastro.html")
    else:
        email = request.POST.get("email")
        nome = request.POST.get("nome")
        senha = request.POST.get("senha")
        confirma_senha = request.POST.get("confirma_senha")
        print(email, nome, senha, confirma_senha)

        if senha != confirma_senha:
            messages.error(request, "As senhas não coincidem.")
            return render(request, "cadastro.html")
        
        try:
            usuario = Usuario.objects.create_user(email=email, nome_completo=nome, password=senha)
            messages.success(request, "Usuário cadastrado com sucesso.")
            return redirect("login")
        except IntegrityError:
            messages.error(request, "Email já cadastrado.")    
            return render(request, "cadastro.html")

def logout_usuario(request):
    auth_logout(request)
    return redirect("login")


