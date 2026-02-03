from django.db import models
from django.core.exceptions import ValidationError
# Create your models here.

class Transacao(models.Model):
    #=== Classes internas para escolhas de campos ===#
    class Categoria(models.TextChoices):
        ALIMENTACAO = "alimentacao", "Alimentação"
        EDUCACAO = "educacao", "Educação"
        ENTRETENIMENTO = "entretenimento", "Entretenimento"
        FINANCEIRO = "financeiro", "Financeiro"
        IMPREVISTOS = "imprevistos", "Imprevistos"
        SAUDE = "saude", "Saúde"
        TRABALHO = "trabalho", "Trabalho"
        TRANSPORTE = "transporte", "Transporte"
        OUTROS = "outros", "Outros"

    class EstadoTransacao(models.TextChoices):
        REALIZADA = "realizada", "Realizada"
        PENDENTE = "pendente", "Pendente"

    class TipoTransacao(models.TextChoices):
        DESPESA = "despesa", "Despesa"
        RECEITA = "receita", "Receita"

    #=== Campos do modelo Transacao ===#
    descricao = models.CharField(max_length=50, null=False, blank=False, verbose_name="Descrição")
    valor = models.DecimalField(max_digits=12, decimal_places=2, null=False, blank=False, verbose_name="Valor")
    categoria = models.CharField(max_length=50, choices=Categoria, default=Categoria.OUTROS, null=False, blank=False, verbose_name="Categoria")
    estado = models.CharField(max_length=50, choices=EstadoTransacao, default=EstadoTransacao.REALIZADA, null=False, blank=False, verbose_name="Estado da Transação")
    tipo = models.CharField(max_length=50, choices=TipoTransacao, default=TipoTransacao.DESPESA, null=False, blank=False, verbose_name="Tipo da Transação")
    data_hora = models.DateTimeField(null=False, blank=False, verbose_name="Data")
    conta_financeira = models.ForeignKey('contas.ContaFinanceira', null=False, on_delete=models.CASCADE, related_name="transacoes", verbose_name="Conta Financeira")
    marcadores = models.ManyToManyField('categoria.Marcador', blank=True, related_name="transacoes", verbose_name="Marcadores")

    #=== Métodos do modelo Transacao ===#
    
    def clean(self):
        erros = {}
        if len(self.descricao.strip()) < 3:
            erros['descricao'] = 'A descrição deve ter pelo menos 3 caracteres.'
        if self.valor and self.valor <= 0:
            erros['valor'] = 'O valor da transação deve ser superior a 0.'
        if erros:
            raise ValidationError(erros)

    def __str__(self):
        return f"{self.descricao} - {self.valor} ({self.get_tipo_display()})"
    
    @property
    def get_valor_display(self):
        return f'{self.valor}'.replace('.',',')
    
    class Meta:
        verbose_name = 'Transação'
        verbose_name_plural = 'Transações'
