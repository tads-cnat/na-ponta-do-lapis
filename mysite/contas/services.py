from .models import ContaFinanceira
from usuario.models import Usuario
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
    def obter_contas_usuario(usuario):
        """
        Retorna todas as contas financeiras de um usuário específico.
        """
        try:
            return ContaFinanceira.objects.filter(usuario=usuario).order_by('id')
        except:
            return []

    @staticmethod
    def VisualizarContaService(conta_id):
        conta = ContaService.obter_conta_por_id(conta_id)
        
        if conta == None:
            return conta
        
        transacoes = Transacao.objects.filter(conta_financeira=conta).order_by('-data_hora')
        return transacoes


    @staticmethod
    def AddContaService(nome, saldo, tipo, usuario=None):
        conta = ContaFinanceira(nome=nome, saldo=saldo, tipo=tipo, usuario=usuario)

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