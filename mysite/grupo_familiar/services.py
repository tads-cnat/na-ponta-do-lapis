from .models import Familia
from .models import Usuario

class FamiliaServices:
    @staticmethod
    def adicionarfamilia(nome):
        Familia.objects.create(nome = nome)
    @staticmethod
    def adicionarmembro(email, id_familia):
        user = Usuario.objects.filter(email)
        if user:
            user.id_familia = id_familia



