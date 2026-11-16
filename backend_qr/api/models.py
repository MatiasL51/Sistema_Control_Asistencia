from django.contrib.auth.models import AbstractUser
from django.db import models

class User(AbstractUser):
    username = None # Eliminamos el campo username predeterminado
    rut = models.CharField(max_length=12, unique=True, null=False)
    
    USERNAME_FIELD = 'rut'  # Django usará el RUT para autenticar
    REQUIRED_FIELDS = ['first_name', 'last_name']

    def __str__(self):
        return f"{self.rut} - {self.first_name}"

# Modelo para almacenar el historial de códigos QR escaneados/usados
class QrUsageLog(models.Model):
    rut = models.CharField(max_length=12)
    time = models.DateTimeField(auto_now_add=True)
    status = models.CharField(max_length=50, default="Usado Exitosamente")

    def __str__(self):
        return f"RUT: {self.rut} - {self.status}"