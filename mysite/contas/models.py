from django.db.models import Model, CharField, FloatField, ForeignKey, CASCADE
from django.core.exceptions import ValidationError
from usuario.models import Usuario

# Create your models here.

class ContaFinanceira(Model):
    TIPOS_CONTA = (('CREDITO', 'Crédito'), ('CREDITO/DEBITO', 'Crédito/Débito'), ('DEBITO', 'Débito'))
    nome = CharField('Nome da Conta', max_length=100, blank=False, null=False)
    saldo = FloatField('Saldo da Conta', default=0.0, blank=False, null=False)
    tipo = CharField('Tipo da Conta', max_length=14, choices=TIPOS_CONTA, default='', blank=False, null=False)
    id_usuario = ForeignKey(Usuario, on_delete=CASCADE, blank=False, null=False, related_name="contas_financeiras", verbose_name='Usuário da Conta')

    class Meta:
        verbose_name = 'Conta Financeira'
        verbose_name_plural = 'Contas Financeiras'

    def clean(self):
        erros = {}

        try:
            super().clean()

        except ValidationError as e:
            erros.update(e.message_dict)

        if len(self.nome) < 3:
            erros['nome'] = 'O nome da conta deve ter pelo menos 3 caracteres.'
        
        if not self.saldo:
            erros['saldo'] = 'O saldo da conta é obrigatório.'
        
        #=== NOTA - Com essa validação nao esta sendo possivel criar uma conta pelo django admin, é necessario uma correção ===#
        # if self.tipo not in self.tipo.choices:           
        #     erros['tipo'] = 'O tipo da conta deve ser Crédito, Crédito e Débito ou Débito'
        #============================================================
        
        if not self.tipo:
            erros['tipo'] = 'O tipo da conta é obrigatório.'
        
        if erros:
            raise ValidationError(erros)

    def __str__(self):
        return f"{self.nome} - {self.saldo} - {self.tipo}"