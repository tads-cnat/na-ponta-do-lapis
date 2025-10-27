from django.shortcuts import render
from django.test import TestCase

# Create your tests here.

def login(request):
    return render(request, "login.html")

def cadastro(request):
    return render(request, "cadastro.html")
