from django.contrib import admin
from django.urls import path
from contas import views

app_name = 'contas'

urlpatterns = [
    path('', views.index, name='index'),
    path('visualizar_conta/', views.visualizar_conta, name='visualizar_conta'),
    path('add_conta/', views.add_conta, name='add_conta'),
    path('editar_conta/', views.editar_conta, name='editar_conta'),
    path('excluir_conta/', views.excluir_conta, name='excluir_conta'),
]
