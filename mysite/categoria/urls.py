from django.urls import path
from . import views

app_name = 'categoria'

urlpatterns = [
    path("categoria/", views.categoria, name="categoria"),
]
