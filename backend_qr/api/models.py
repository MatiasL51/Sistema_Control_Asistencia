from django.contrib.auth.models import AbstractUser, BaseUserManager
from django.db import models
from django.conf import settings

class CustomUserManager(BaseUserManager):
    def create_user(self, rut, password=None, **extra_fields):
        if not rut:
            raise ValueError('El RUT es obligatorio')
        
        # Opcional: aseguramos valores por defecto para evitar errores si REQUIRED_FIELDS los pide
        extra_fields.setdefault('first_name', 'Usuario')
        extra_fields.setdefault('last_name', 'Sistema')
        
        user = self.model(rut=rut, **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, rut, password=None, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)
        return self.create_user(rut, password, **extra_fields)

# 2. El modelo User configurado con el CustomUserManager
class User(AbstractUser):
    username = None  # Eliminamos el campo username por completo
    rut = models.CharField(max_length=12, unique=True, null=False)
    
    USERNAME_FIELD = 'rut'  # Django usará el RUT para autenticar
    REQUIRED_FIELDS = []    # Sin campos obligatorios extra para la creación

    # Asociamos el gestor personalizado
    objects = CustomUserManager()

    def __str__(self):
        return f"{self.rut}"

# Modelo para almacenar el historial de códigos QR escaneados/usados
class QrUsageLog(models.Model):
    rut = models.CharField(max_length=12)
    time = models.DateTimeField(auto_now_add=True)
    status = models.CharField(max_length=50, default="Usado Exitosamente")

    def __str__(self):
        return f"RUT: {self.rut} | {self.status}"

class QrTokenRecord(models.Model):
    # Relación con el usuario del sistema
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    token = models.CharField(max_length=190, unique=True)  # Ajustado a 190 para evitar error 1071 en MySQL
    is_used = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    expires_at = models.DateTimeField()

    def __str__(self):
        return f"QR Token - Usuario ID: {self.user_id} | Usado: {self.is_used}"