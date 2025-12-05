from .models import Familia
from .models import Usuario

class FamiliaServices:
    @staticmethod
    def adicionarfamilia(nome):
        return Familia.objects.create(nome = nome)
    
    @staticmethod
    def adicionarmembro(email, id_familia):
        user = Usuario.objects.filter(email).first()
        if user:
            user.id_familia = id_familia
            user.save()
    
    @staticmethod
    def tornar_adminFamilia(user, id_familia):
        user.papel = Usuario.Papel.ADMIN_FAMILIA
        user.id_familia = id_familia
        user.save()

