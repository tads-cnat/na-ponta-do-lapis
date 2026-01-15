from django.contrib import admin
from django.urls import path
from contas import views

app_name = 'contas'

urlpatterns = [
    path('', views.index, name='index'),
    path('visualizar/<int:conta_id>/', views.visualizar_conta, name='visualizar_conta'),
    path('add_conta/', views.add_conta, name='add_conta'),
    path('editar/<int:conta_id>/', views.editar_conta, name='editar_conta'),
    path('excluir/<int:conta_id>/', views.excluir_conta, name='excluir_conta'),
    path('api/obter/<int:conta_id>/', views.obter_conta_json, name='obter_conta_json'),
]