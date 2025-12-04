from django.db import models
from django.contrib.auth.models import AbstractUser
from .UsuarioManagerCustom import UsuarioManagerCustom

#from grupo_familiar.models import Familia

# Create your models here.

class Usuario(AbstractUser):

    class Papel(models.TextChoices):
        ADMIN = "admin", "Administrador"
        ADMIN_FAMILIA = "adminFamilia", "Administrador da Família"
        USUARIO = "usuario", "Usuário"

    username = models.CharField(max_length=254, unique=True, blank=True)
    nome_completo = models.CharField(max_length=254, null=False, blank=False, verbose_name="Nome Completo")
    email = models.EmailField(unique=True, null=False, blank=False, verbose_name="Email")
    foto_perfil = models.ImageField(upload_to="fotos_perfil/", null=True, blank=True, verbose_name="Foto de Perfil")
    papel = models.CharField(max_length=50, choices=Papel.choices, default=Papel.USUARIO, null=False, blank=False, verbose_name="Papel do Usuário")
    id_familia = models.ForeignKey('grupo_familiar.Familia', null=True, on_delete=models.SET_NULL, related_name="usuarios", verbose_name="Família")

    REQUIRED_FIELDS = ['nome_completo']
    EMAIL_FIELD = 'email'
    USERNAME_FIELD = 'email'

    objects = UsuarioManagerCustom()

    def save(self, *args, **kwargs):
        if not self.username:
            self.username = self.email
        super().save(*args, **kwargs)

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

