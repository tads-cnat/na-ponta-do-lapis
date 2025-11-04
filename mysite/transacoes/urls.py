from django.urls import path
from transacoes.views  import transacoes_index

urlpatterns = [
    path("", transacoes_index, name="transacoes_index" ),
]
