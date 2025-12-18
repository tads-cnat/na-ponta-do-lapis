from django.shortcuts import render, redirect
from django.test import TestCase
from django.http import HttpResponse
from django.contrib.auth import get_user_model, authenticate, login as auth_login, logout as auth_logout
from django.contrib import messages
from django.db import IntegrityError


# Create your tests here.

Usuario = get_user_model()

def login_usuario(request):
    # Se usuário já está logado, redireciona para contas
    if request.user.is_authenticated:
        return redirect("contas:index")
    
    if request.method == "POST":
        email = request.POST.get("email", "").strip()
        senha = request.POST.get("senha", "").strip()

        # Validação básica
        if not email or not senha:
            messages.error(request, "Email e senha são obrigatórios.")
            return render(request, "login.html")
        
        # Authenticate using the USERNAME_FIELD (email is USERNAME_FIELD in custom user)
        # Django's default `authenticate` expects the username kwarg name to be `username`.
        usuario = authenticate(request, username=email, password=senha)
        if usuario is not None:
            auth_login(request, usuario)
            return redirect("contas:index")
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
            return redirect("usuario:login")
        except IntegrityError:
            messages.error(request, "Email já cadastrado.")    
            return render(request, "cadastro.html")

def logout_usuario(request):
    auth_logout(request)
    return redirect("usuario:login")


