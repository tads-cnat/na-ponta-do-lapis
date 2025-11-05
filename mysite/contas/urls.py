from django.contrib import admin
from django.urls import path
from contas import views

urlpatterns = [
    path('', views.index, name='contas'),
]
