from .models import ContaFinanceira
from transacoes.models import Transacao
from django.core.exceptions import ValidationError

class ContaService:
    
    @staticmethod
    def obter_conta_por_id(conta_id):
        try:
            return ContaFinanceira.objects.get(pk=conta_id)

        except ContaFinanceira.DoesNotExist:
            return None

    @staticmethod
    def VisualizarContaService(conta_id):
        pass

    @staticmethod
    def AddContaService(nome, saldo, tipo):
        conta = ContaFinanceira(nome=nome, saldo=saldo, tipo=tipo)

        try:
            conta.full_clean()

        except ValidationError as erro:
            raise erro
        
        conta.save()
        return conta
    
    @staticmethod
    def EditarContaService(conta_id, nome=None, saldo=None, tipo=None):
        conta = ContaService.obter_conta_por_id(conta_id)

        if not conta:
            return None
        
        if nome is not None:
            conta.nome = nome

        if saldo is not None:
            conta.saldo = saldo

        if tipo is not None:
            conta.tipo = tipo

        try:
            conta.full_clean()

        except ValidationError as erro:
            raise erro
        
        conta.save()
        return conta
    
    @staticmethod
    def ExcluirContaService(conta_id):
        conta = ContaService.obter_conta_por_id(conta_id)

        if not conta:
            return False
        
        conta.delete()
        return True