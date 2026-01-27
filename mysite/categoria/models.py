from django.db import models
from django.conf import settings

class Marcador(models.Model):
    nome = models.CharField(max_length=70)
    cor = models.CharField(max_length=7)
    usuario = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="marcadores"
    )

    def __str__(self):
        return self.nome + " - " + self.cor