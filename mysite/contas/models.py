from django.db.models import Model, CharField, FloatField

# Create your models here.

class Conta_Financeira(Model):
    tipos_conta = [('CREDITO', 'Crédito'), ('CREDITO/DEBITO', 'Crédito/Débito'), ('DEBITO', 'Débito')]
    nome = CharField('Nome da Conta', max_length=100, blank=False, null=False)
    saldo = FloatField('Saldo da Conta', blank=False, null=False)
    tipo = CharField('Tipo da Conta', max_length=14, choices=tipos_conta, blank=False, null=False)

    def __str__(self):
        return f"{self.nome} - {self.saldo} - {self.tipo}"