from django.urls import path
from transacoes.views  import transacoes_index, adicionar_transacao_view,excluir_transacao , filtrar_transacao

urlpatterns = [
    path("", transacoes_index, name="transacoes_index" ),
    path('adicionar_transacao/', adicionar_transacao_view, name='adicionar_transacao'),
    path('excluir_transacao/<int:id>', excluir_transacao, name="excluir_transacao"),
    path('filtro/', filtrar_transacao, name='filtrar_transacao')

]
