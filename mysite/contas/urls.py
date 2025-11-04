from django.contrib import admin
from django.urls import path
from contas import views

app_name = 'contas'

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', views.index, name='index'),
    path('contas/', views.contas, name="contas"),
]
