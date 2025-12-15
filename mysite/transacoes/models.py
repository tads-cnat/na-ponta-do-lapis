from django.db import models
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
        RECEITA = "receita", "Receita"
        DESPESA = "despesa", "Despesa"

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
    def __str__(self):
        return f"{self.descricao} - {self.valor} ({self.get_tipo_display()})"
    
    @property
    def get_valor_display(self):
        return f'{self.valor}'.replace('.',',')