from django.db.models import Sum
from transacoes.models import Transacao


class DashboardService:

    def receita_usuario(self, usuario_id):
        # Soma todas as receitas realizadas das contas do usuário
        resultado = (
            Transacao.objects
            .filter(
                conta_financeira__usuario_id=usuario_id,
                tipo=Transacao.TipoTransacao.RECEITA,
                estado=Transacao.EstadoTransacao.REALIZADA,
            )
            .aggregate(total=Sum("valor"))
        )

        return resultado.get("total") or 0

    def despesa_usuario(self, usuario_id):
        resultado = (
            Transacao.objects
            .filter(
                conta_financeira__usuario_id=usuario_id,
                tipo=Transacao.TipoTransacao.DESPESA,
                estado=Transacao.EstadoTransacao.REALIZADA,
            )
            .aggregate(total=Sum("valor"))
        )

        return resultado.get("total") or 0
    
    def total(self, usuario_id):
        receita = self.receita_usuario(usuario_id)
        despesa = self.despesa_usuario(usuario_id)
        return receita + despesa
    
    def gastos_por_categoria(self, usuario_id):
        # Agrupa despesas por categoria
        resultado = (
            Transacao.objects
            .filter(
                conta_financeira__usuario_id=usuario_id,
                tipo=Transacao.TipoTransacao.DESPESA,
                estado=Transacao.EstadoTransacao.REALIZADA,
            )
            .values('categoria')
            .annotate(total=Sum('valor'))
            .order_by('-total')
        )
        return list(resultado)
    
    def historico_transacoes(self, usuario_id, limite=10):
        # Retorna as últimas transações do usuário
        transacoes = (
            Transacao.objects
            .filter(conta_financeira__usuario_id=usuario_id)
            .select_related('conta_financeira')
            .order_by('-data_hora')
            [:limite]
        )
        return transacoes
