from django.db import models
from grupo_familiar.models import Familia

# Create your models here.

class Usuario(models.Model):

    class Papel(models.TextChoices):
        ADMIN = "admin", "Administrador"
        ADMIN_FAMILIA = "adminFamilia", "Administrador da Família"
        USUARIO = "usuario", "Usuário"

    nome_completo = models.CharField(max_length=254, null=False, blank=False, verbose_name="Nome Completo")
    email = models.EmailField(max_length=254, null=False, blank=False, unique=True, verbose_name="Email")
    senha = models.CharField(max_length=254, null=False, blank=False, verbose_name="Senha")
    foto_perfil = models.ImageField(upload_to="fotos_perfil/", null=True, blank=True, verbose_name="Foto de Perfil")
    papel = models.CharField(max_length=50, choices=Papel.choices, default=Papel.USUARIO, null=False, blank=False, verbose_name="Papel do Usuário")
    id_familia = models.ForeignKey(Familia, null=True, on_delete=models.SET_NULL, related_name="usuarios", verbose_name="Família")

    def __str__(self):
        return self.nome_completo
    
    @property
    def is_admin(self):
        return self.papel == self.Papel.ADMIN
    
    @property
    def is_adminFamilia(self):
        return self.papel == self.Papel.ADMIN_FAMILIA
    
    def tornar_admin(self):
        self.papel = self.Papel.ADMIN
        self.save()
    
    def tornar_adminFamilia(self):
        self.papel = self.Papel.ADMIN_FAMILIA
        self.save()
    
    def tornar_usuario(self):
        self.papel = self.Papel.USUARIO
        self.save()

