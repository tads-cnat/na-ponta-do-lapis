from django.contrib import admin
from django.urls import include, path
from . import views

urlpatterns = [
    path('',views.login, name="login"),
    path('cadastro/',views.cadastro, name="cadastro")
]
