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
    def editar_transacao(id_transacao, descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids):
        transacao = Transacao.objects.get(id=id_transacao)

        transacao.descricao = descricao
        transacao.valor = valor
        transacao.categoria = categoria
        transacao.estado = estado
        transacao.tipo = tipo
        transacao.data_hora = data_hora
        transacao.conta_financeira = ContaFinanceira.objects.get(id=conta_financeira_id)
        transacao.marcadores.set(Marcador.objects.filter(id__in=marcadores_ids))
        transacao.save()

        return transacao

    
    @staticmethod
    def excluir_transacao(id_transacao):
        try:
            Transacao.objects.get(id=id_transacao).delete()
        except Exception as e:
            raise Exception("{e}")
        
    @staticmethod
    def filtrar_transacao( categoria,tipo, conta):
        transacoes = TransacaoService.obter_minhas_transacoes()
        if categoria:
            transacoes = transacoes.filter(categoria=categoria)
        if tipo:
            transacoes = transacoes.filter(tipo=tipo)
        if conta:
            transacoes = transacoes.filter(conta_financeira=conta)

        return transacoes
        
    @staticmethod
    def obter_minhas_transacoes():
        return Transacao.objects.all()
    
    @staticmethod
    def obter_transacoes_categoria(categoria):
        return Transacao.objects.filter(categoria=categoria)
    
    @staticmethod
    def obter_categorias():
        return Transacao.Categoria.choices
    
    @staticmethod
    def obter_estados():
        return Transacao.EstadoTransacao.choices
    
    @staticmethod
    def obter_tipos():
        return Transacao.TipoTransacao.choices
    