from django.contrib.auth.backends import ModelBackend
from django.contrib.auth import get_user_model

Usuario = get_user_model()

class EmailBackend(ModelBackend):
    """
    Backend de autenticação customizado que permite login via email.
    
    Como o model Usuario usa 'email' como USERNAME_FIELD, este backend
    trata autenticação por email em vez de username.
    """
    
    def authenticate(self, request, username=None, password=None, **kwargs):
        """
        Autentica o usuário usando email como campo de login.
        """
        try:
            # Trata 'username' como email
            user = Usuario.objects.get(email=username)
        except Usuario.DoesNotExist:
            return None
        
        # Verifica se a senha está correta
        if user.check_password(password) and self.user_can_authenticate(user):
            return user
        
        return None
    
    def get_user(self, user_id):
        """
        Retorna o usuário pelo ID.
        """
        try:
            return Usuario.objects.get(pk=user_id)
        except Usuario.DoesNotExist:
            return None
