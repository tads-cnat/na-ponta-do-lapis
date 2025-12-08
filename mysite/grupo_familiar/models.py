from django.db import models
class Familia(models.Model):
    nome = models.CharField(max_length=100, blank=False, null=False)

    @property
    def membros(self):
        return Usuario.objects.filter(id_familia=self).exclude(papel='adminFamilia')
    
    @property
    def chefes(self):
        return Usuario.objects.filter(id_familia=self, papel='adminFamilia')

    def __str__(self):
        return f"{self.nome}"
