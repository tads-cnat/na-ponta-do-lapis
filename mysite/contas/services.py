from .models import ContaFinanceira
from usuario.models import Usuario
from transacoes.models import Transacao
from django.core.exceptions import ValidationError
from datetime import datetime

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

    @staticmethod
    def obter_contas_sessao(request):
        """
        Retorna todas as contas da sessão de um usuário anônimo.
        """
        contas = request.session.get("contas", [])
        return contas

    @staticmethod
    def salvar_conta_sessao(request, nome, saldo, tipo):
        """
        Salva uma conta na sessão para usuário anônimo.
        """
        contas = request.session.get("contas", [])
        contador = request.session.get("contador_contas_id", 0)

        contador += 1
        request.session["contador_contas_id"] = contador

        # Validação básica
        if len(nome.strip()) < 3:
            raise ValidationError({'nome': 'O nome deve ter pelo menos 3 caracteres.'})
        if float(saldo) < 0:
            raise ValidationError({'saldo': 'O saldo não pode ser negativo.'})
        if tipo not in ['CREDITO', 'DEBITO', 'CREDITO_DEBITO']:
            raise ValidationError({'tipo': 'Tipo de conta inválido.'})

        conta = {
            "id": contador,
            "nome": nome,
            "saldo": float(saldo),
            "tipo": tipo,
        }

        contas.append(conta)
        request.session["contas"] = contas
        request.session.modified = True

        return conta

    @staticmethod
    def editar_conta_sessao(request, conta_id, nome=None, saldo=None, tipo=None):
        """
        Edita uma conta da sessão para usuário anônimo.
        """
        contas = request.session.get("contas", [])
        
        conta_encontrada = None
        for conta in contas:
            if conta["id"] == conta_id:
                conta_encontrada = conta
                break

        if not conta_encontrada:
            raise ValidationError({'conta': 'Conta não encontrada na sessão.'})

        if nome is not None:
            if len(nome.strip()) < 3:
                raise ValidationError({'nome': 'O nome deve ter pelo menos 3 caracteres.'})
            conta_encontrada["nome"] = nome

        if saldo is not None:
            if float(saldo) < 0:
                raise ValidationError({'saldo': 'O saldo não pode ser negativo.'})
            conta_encontrada["saldo"] = float(saldo)

        if tipo is not None:
            if tipo not in ['CREDITO', 'DEBITO', 'CREDITO_DEBITO']:
                raise ValidationError({'tipo': 'Tipo de conta inválido.'})
            conta_encontrada["tipo"] = tipo

        request.session["contas"] = contas
        request.session.modified = True

        return conta_encontrada

    @staticmethod
    def excluir_conta_sessao(request, conta_id):
        """
        Exclui uma conta da sessão para usuário anônimo.
        """
        contas = request.session.get("contas", [])
        
        contas_filtradas = [conta for conta in contas if conta["id"] != conta_id]

        if len(contas_filtradas) == len(contas):
            raise ValidationError({'conta': 'Conta não encontrada na sessão.'})

        request.session["contas"] = contas_filtradas
        request.session.modified = True

        return True