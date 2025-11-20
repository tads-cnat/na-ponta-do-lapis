from django.db import models
from usuario.models import Usuario

# Create your models here.

class ContaFinanceira(models.Model):
    TIPOS_CONTA = (('CREDITO', 'Crédito'), ('CREDITO/DEBITO', 'Crédito/Débito'), ('DEBITO', 'Débito'))
    nome = models.CharField(max_length=100, blank=False, null=False, verbose_name='Nome da Conta')
    saldo = models.FloatField(default=0, blank=False, null=False, verbose_name='Saldo da Conta')
    tipo = models.CharField(max_length=14, choices=TIPOS_CONTA, default='CREDITO', blank=False, null=False, verbose_name='Tipo da Conta')
    id_usuario = models.ForeignKey(Usuario, max_length=254, on_delete=models.CASCADE, blank=False, null=False, related_name="contas_financeiras", verbose_name="Usuário da Conta")

    def set_nome(self, novo_nome):
        if (not isinstance(novo_nome, str) or novo_nome == ""):
            raise ValueError("O nome deve ser uma string e não vazio")
        self.nome = novo_nome

    def set_tipo(self, novo_tipo):
        if (not isinstance(novo_tipo, str) or novo_tipo == "" and novo_tipo != 'CREDITO' and novo_tipo != 'CREDITO/DEBITO' and novo_tipo != 'DEBITO'):
            raise ValueError("O tipo da conta deve ser um string não vazia e de um tipo válido")
        self.tipo = novo_tipo

    def set_saldo(self, novo_saldo):
        if (not isinstance(novo_saldo, float)):
            raise ValueError("O saldo deve ser um valor real")
        self.saldo = novo_saldo

    def __str__(self):
        return f"{self.nome} - {self.saldo} - {self.tipo}"