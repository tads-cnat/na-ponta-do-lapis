from django.contrib import admin
from django.urls import include, path
from . import views

urlpatterns = [
    path('',views.login_usuario, name="login"),
    path('cadastro/',views.cadastro_usuario, name="cadastro"),
    path('logout/',views.logout_usuario, name="logout"),
]