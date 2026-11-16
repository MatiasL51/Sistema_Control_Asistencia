# views.py en Django
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
from .models import QrTokenRecord, User, QrUsageLog # Tu modelo de base de datos que almacena los tokens generados
from django.contrib.auth import authenticate
from django.contrib.auth import get_user_model

User = get_user_model()

class CreateUserView(APIView):
    def post(self, request):
        rut = request.data.get("rut")
        password = request.data.get("password")
        
        if not rut or not password:
            return Response({"success": False, "message": "Faltan datos"}, status=status.HTTP_400_BAD_REQUEST)
            
        if User.objects.filter(rut=rut).exists():
            return Response({"success": False, "message": "El usuario ya existe"}, status=status.HTTP_400_BAD_REQUEST)
            
        try:
            # 👈 Usamos create_user pasando únicamente el rut y la contraseña (y campos opcionales si los requiere)
            User.objects.create_user(
                rut=rut,
                password=password,
                first_name="Usuario",
                last_name="Sistema"
            )
            
            return Response({
                "success": True, 
                "message": "Usuario creado con éxito en MySQL"
            }, status=status.HTTP_201_CREATED)
            
        except Exception as e:
            print("ERROR AL CREAR USUARIO:", str(e))
            return Response({
                "success": False, 
                "message": f"Error interno: {str(e)}"
            }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        
class LoginView(APIView):
    def post(self, request):
        rut = request.data.get("rut")
        password = request.data.get("password")
        
        # Autentica usando el RUT como username (o ajusta según tu modelo)
        user = authenticate(username=rut, password=password)
        
        if user is not None:
            return Response({
        "success": True,
        "token": "tu_token_aqui", # o el mensaje que uses
        "message": "Login exitoso"
        }, status=status.HTTP_200_OK)
        
        return Response(
            {"error": "Credenciales inválidas"}, 
            status=status.HTTP_400_BAD_REQUEST
        )

class QrUsageLogListView(APIView):
    def get(self, request):
        try:
            # Obtenemos todos los registros de QR ordenados del más reciente al más antiguo
            records = QrTokenRecord.objects.all().order_by('-created_at')
            
            data = [
                {
                    "id": r.id,
                    "rut": r.user.username, # El username almacena el RUT del usuario
                    "token": r.token,
                    "is_used": r.is_used,
                    "created_at": str(r.created_at),
                    "expires_at": str(r.expires_at)
                }
                for r in records
            ]
            
            return Response(data, status=status.HTTP_200_OK)
            
        except Exception as e:
            return Response(
                {"error": str(e)}, 
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )

class ValidateQrView(APIView):
    def post(self, request):
        print("--- DEPURANDO VALIDADOR DE QR ---")
        print("REQUEST DATA:", request.data)
        
        # 1. Capturamos 'qr_token' (que es lo que manda Android) o 'rut' por compatibilidad
        raw_token = request.data.get("qr_token") or request.data.get("rut")
        
        if not raw_token:
            return Response({
                "success": False, 
                "message": "Falta el campo qr_token en la petición."
            }, status=status.HTTP_400_BAD_REQUEST)
            
        # 2. Si viene con el prefijo 'ACCESS_TOKEN_RUT:', extraemos solo el RUT
        token_str = str(raw_token)
        if "ACCESS_TOKEN_RUT:" in token_str:
            rut_limpio = token_str.split("ACCESS_TOKEN_RUT:")[1]
        else:
            rut_limpio = token_str
            
        try:
            # 3. Guardamos el registro exitosamente en MySQL
            QrUsageLog.objects.create(
                rut=rut_limpio,
                status="Usado Exitosamente"
            )
            
            return Response({
                "success": True,
                "message": f"QR validado correctamente para el RUT {rut_limpio}"
            }, status=status.HTTP_200_OK)
            
        except Exception as e:
            print("ERROR AL GUARDAR QR:", str(e))
            return Response({
                "success": False,
                "message": f"Error interno: {str(e)}"
            }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)