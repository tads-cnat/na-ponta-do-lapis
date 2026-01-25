from django.urls import path
from transacoes.views  import TransacaoIndex, TransacaoSalvar, TransacaoEditar, TransacaoExcluir, TransacaoFiltrar 

app_name= "transacoes"

urlpatterns = [
    path("", TransacaoIndex.as_view(), name="index" ),
    path('adicionar_transacao/', TransacaoSalvar.as_view(), name='adicionar'),
    path('editar_transacao/<int:id>', TransacaoEditar.as_view(), name="editar"),
    path('excluir_transacao/<int:id>', TransacaoExcluir.as_view(), name="excluir"),
    path('filtro/', TransacaoFiltrar.as_view(), name='filtrar')

]
