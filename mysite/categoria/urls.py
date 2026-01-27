from django.urls import path
from . import views
from .views import CriarMarcadorView, ListarMarcadoresView, ExcluirMarcadorView, EditarMarcadorView, TransacaoIndexView

app_name = 'categoria'

urlpatterns = [
    # path("categoria/", views.categoria, name="categoria"),
    path('marcador/criar/', CriarMarcadorView.as_view(), name='criar_marcador'),
    path("categoria/marcadores/", ListarMarcadoresView.as_view(), name="listar_marcadores"),
    path("excluir-marcador/<int:id>/", ExcluirMarcadorView.as_view(), name="excluir_marcador"),
    path("editar-marcador/<int:id>/", EditarMarcadorView.as_view(), name="editar_marcador"),
    # path('categorias/', CategoriaIndex.as_view(), name='categoria_index'),
    path("categoria/", TransacaoIndexView.as_view(), name="categorias"),
]
