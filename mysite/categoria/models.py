from django.db import models


# Create your models here.
class Marcador(models.Model):
    nome = models.CharField(max_length=70, verbose_name="Nome")
    cor = models.CharField(max_length=7, verbose_name="Cor")

    def __str__(self):
        return self.nome + " - " + self.cor