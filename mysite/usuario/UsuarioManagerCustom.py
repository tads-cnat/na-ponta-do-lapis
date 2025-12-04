from django.contrib.auth.models import BaseUserManager

class UsuarioManagerCustom(BaseUserManager):
    def create_user(self, email, nome_completo, password=None, **extra_fields):
        if not email:
            raise ValueError('O email é obrigatório')
        email = self.normalize_email(email)
        user = self.model(email=email, nome_completo=nome_completo, **extra_fields)
        if not user.username:
            user.username = email
        
        user.set_password(password)
        user.save(using=self._db)
        return user
    
    def create_superuser(self, email, nome_completo, password=None, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)
        extra_fields.setdefault('is_active', True)
        extra_fields.setdefault('papel', 'admin')

        if extra_fields.get('is_staff') is not True:
            raise ValueError('Superuser must have is_staff=True.')
        if extra_fields.get('is_superuser') is not True:
            raise ValueError('Superuser must have is_superuser=True.')

        return self.create_user(email, nome_completo, password, **extra_fields)