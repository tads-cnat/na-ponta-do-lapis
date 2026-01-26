from contas.models import ContaFinanceira
from transacoes.models import Transacao
from .models import Familia
from usuario.models import Usuario
from django.db.models import Sum

class FamiliaServices:
    @staticmethod
    def adicionarfamilia(nome):
        familia = Familia(
            nome = nome
        )
        familia.save()
        return familia
    
    @staticmethod
    def adicionarmembro(email, familia):
        try:
            user = Usuario.objects.get(email=email)
        except Usuario.DoesNotExist:
            raise ValueError(f"Usuário com email '{email}' não encontrado.")
        
        if user.id_familia:
            raise ValueError("Este usuário já pertence a uma família.")
        
        user.id_familia = familia
        user.save()
    
    @staticmethod
    def tornar_adminFamilia(user, familia):
        user.tornar_adminFamilia()
        user.id_familia = familia
        user.save()
    
    @staticmethod
    def tirarmembro(email, familia):
        user = Usuario.objects.get(email=email)
        if user and user.id_familia == familia:
            user.id_familia = None
            user.papel = Usuario.Papel.USUARIO
            user.save()
    @staticmethod
    def receita_familia(familia_id):
        # Soma todas as receitas realizadas das contas do usuário
        resultado = (
            Transacao.objects
            .filter(
                conta_financeira__usuario__id_familia=familia_id,
                tipo=Transacao.TipoTransacao.RECEITA,
                estado=Transacao.EstadoTransacao.REALIZADA,
            )
            .aggregate(total=Sum("valor"))
        )

        return resultado.get("total") or 0

    @staticmethod
    def despesa_familia(familia_id):
        resultado = (
            Transacao.objects
            .filter(
                conta_financeira__usuario__id_familia=familia_id,
                tipo=Transacao.TipoTransacao.DESPESA,
                estado=Transacao.EstadoTransacao.REALIZADA,
            )
            .aggregate(total=Sum("valor"))
        )

        return resultado.get("total") or 0

    @staticmethod
    def total(familia_id):
        receita = FamiliaServices.receita_familia(familia_id)
        despesa = FamiliaServices.despesa_familia(familia_id)
        return receita - despesa

    @staticmethod
    def saldo_membro(usuario_id):
        """Calcula o saldo total de um membro (soma todas as contas)"""
        contas = ContaFinanceira.objects.filter(usuario_id=usuario_id)
        total_saldo = sum(conta.saldo for conta in contas)
        return total_saldo

    @staticmethod
    def saldo_por_transacoes_membro(usuario_id):
        """Calcula o saldo de um membro baseado em suas transações"""
        receitas = (
            Transacao.objects
            .filter(
                conta_financeira__usuario_id=usuario_id,
                tipo=Transacao.TipoTransacao.RECEITA,
                estado=Transacao.EstadoTransacao.REALIZADA,
            )
            .aggregate(total=Sum("valor"))
        )
        
        despesas = (
            Transacao.objects
            .filter(
                conta_financeira__usuario_id=usuario_id,
                tipo=Transacao.TipoTransacao.DESPESA,
                estado=Transacao.EstadoTransacao.REALIZADA,
            )
            .aggregate(total=Sum("valor"))
        )
        
        receita_total = receitas.get("total") or 0
        despesa_total = despesas.get("total") or 0
        return receita_total - despesa_total

    @staticmethod
    def relatorio_saldos_familia(familia_id):
        """Retorna um dicionário com o saldo de cada membro da família"""
        familia = Familia.objects.get(id=familia_id)
        membros = Usuario.objects.filter(id_familia=familia)
        relatorio = []
        for membro in membros:
            saldo_contas = FamiliaServices.saldo_membro(membro.id)
            saldo_transacoes = FamiliaServices.saldo_por_transacoes_membro(membro.id)
            
            relatorio.append({
                'usuario': membro,
                'nome': membro.nome_completo,
                'email': membro.email,
                'saldo_contas': saldo_contas,
                'saldo_transacoes': saldo_transacoes,
                'papel': membro.get_papel_display()
            })
        
        return relatorio
