from django.db.models import Model, CharField, FloatField, ForeignKey
from usuario.models import Usuario

# Create your models here.

class ContaFinanceira(Model):
    TIPOS_CONTA = (('CREDITO', 'Crédito'), ('CREDITO/DEBITO', 'Crédito/Débito'), ('DEBITO', 'Débito'))
    nome = CharField('Nome da Conta', max_length=100, blank=False, null=False)
    saldo = FloatField('Saldo da Conta', default=0, blank=False, null=False)
    tipo = CharField('Tipo da Conta', max_length=14, choices=TIPOS_CONTA, default='CREDITO', blank=False, null=False)
    id_usuario = ForeignKey(Usuario, "Usuário da Conta", max_length=254, blank=False, null=False, related_name="contas_financeiras")

    def __str__(self):
        return f"{self.nome} - {self.saldo} - {self.tipo}"