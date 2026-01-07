from django.core.exceptions import ValidationError
from .models import Transacao
from contas.models import ContaFinanceira
from categoria.models import Marcador

class TransacaoService:

    @staticmethod
    def adicionar_transacao(descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids = None ):
        try:
            conta = ContaFinanceira.objects.get(id=conta_financeira_id)

            transacao = Transacao(
                descricao = descricao,
                valor= valor,
                categoria= categoria,
                estado = estado,
                tipo = tipo,
                data_hora = data_hora,
                conta_financeira = conta
            )
            transacao.full_clean()
            transacao.save()

            if marcadores_ids:
                marcadores = Marcador.objects.filter(id__in=marcadores_ids)

                if marcadores.count() > 3:
                    raise ValidationError({'marcadores':'So é permitido no maximo 3 marcadores por transação.'})
                
                if marcadores.count() != len(marcadores_ids):
                    raise ValidationError({'marcadores':'1 ou mais marcadores não foram estão invalidos'})
                transacao.marcadores.set(marcadores)

            return transacao
        
        except ContaFinanceira.DoesNotExist:
            raise ValidationError({'conta_financeira':'Conta Financeira Inexistente.'})
  
        

    @staticmethod
    def editar_transacao(id_transacao, descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids):

        try:
            transacao = TransacaoService.obter_transacoes_id(id_transacao)
            conta =  ContaFinanceira.objects.get(id=conta_financeira_id)

            transacao.descricao = descricao
            transacao.valor = valor
            transacao.categoria = categoria
            transacao.estado = estado
            transacao.tipo = tipo
            transacao.data_hora = data_hora
            transacao.conta_financeira = conta
            
            transacao.full_clean()
            transacao.save()

            if marcadores_ids is not None:
                marcadores = Marcador.objects.filter(id__in=marcadores_ids)

                if marcadores.count() > 3:
                    raise ValidationError({'marcadores':'So é permitido no máximo 3 marcadores por transação.'})
                
                if marcadores.count() != len(marcadores_ids):
                    raise ValidationError({'marcadores':'1 ou mais marcadores não foram estão invalidos'})
                transacao.marcadores.set(marcadores)
      
            return transacao
        
        except Transacao.DoesNotExist:
            raise ValidationError({'transacao':'Transação não encontrada'})
        
        except ContaFinanceira.DoesNotExist:
            raise ValidationError({'conta_financeira':"Conta Financeira não encontrada"})
    
    @staticmethod
    def excluir_transacao(id_transacao):
        try:
           TransacaoService.obter_transacoes_id(id_transacao).delete()
        except Transacao.DoesNotExist:
            raise ValidationError({'transacao':'Transação não encontrada'})
        
    @staticmethod
    def obter_minhas_transacoes():
        return Transacao.objects.all()
    
    def obter_transacoes_id(transacao_id):
        return Transacao.objects.get(id=transacao_id)
    
    @staticmethod
    def filtrar_transacao(busca, categoria, tipo, conta):
        transacoes = TransacaoService.obter_minhas_transacoes()
        if busca:
            transacoes = transacoes.filter(descricao__icontains=busca)
        if categoria:
            transacoes = transacoes.filter(categoria=categoria)
        if tipo:
            transacoes = transacoes.filter(tipo=tipo)
        if conta:
            transacoes = transacoes.filter(conta_financeira=conta)

        return transacoes
        
    @staticmethod
    def obter_categorias():
        return Transacao.Categoria.choices
    
    @staticmethod
    def obter_estados():
        return Transacao.EstadoTransacao.choices
    
    @staticmethod
    def obter_tipos():
        return Transacao.TipoTransacao.choices
    