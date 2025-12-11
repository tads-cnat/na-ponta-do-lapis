from django.urls import path
from . import views
from .views import CriarMarcadorView, ListarMarcadoresView

app_name = 'categoria'

urlpatterns = [
    path("categoria/", views.categoria, name="categoria"),
    path('marcador/criar/', CriarMarcadorView.as_view(), name='criar_marcador'),
    path("categoria/marcadores/", ListarMarcadoresView.as_view(), name="listar_marcadores"),
]
