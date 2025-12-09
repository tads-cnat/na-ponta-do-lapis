from .models import Transacao
from contas.models import ContaFinanceira
from categoria.models import Marcador

class TransacaoService:

    @staticmethod
    def adicionar_transacao(descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids = None ):
        try:
            transacao = Transacao(
                descricao = descricao,
                valor= valor,
                categoria= categoria,
                estado = estado,
                tipo = tipo,
                data_hora = data_hora,
                conta_financeira = ContaFinanceira.objects.get(id=conta_financeira_id)
            )
            transacao.save()

            if marcadores_ids:
                marcadores = Marcador.objects.filter(id__in=marcadores_ids)
                transacao.marcadores.add(*marcadores)
                transacao.save()

        except Exception as erro:
            raise Exception(f"erro: {erro}") 
    
    @staticmethod
    def obter_categorias():
        return Transacao.Categoria.choices
    
    @staticmethod
    def obter_estados():
        return Transacao.EstadoTransacao.choices
    
    @staticmethod
    def obter_tipos():
        return Transacao.TipoTransacao.choices