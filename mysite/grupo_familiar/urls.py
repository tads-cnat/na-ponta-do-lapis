from django.urls import path
from grupo_familiar import views

app_name = 'familia'

urlpatterns = [
    path('', views.familia, name='familia')
]