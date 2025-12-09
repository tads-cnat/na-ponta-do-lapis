from django.urls import path
from transacoes.views  import transacoes_index, adicionar_transacao_view

urlpatterns = [
    path("", transacoes_index, name="transacoes_index" ),
    path('adicionar_transacao/', adicionar_transacao_view, name='adicionar_transacao')
]
