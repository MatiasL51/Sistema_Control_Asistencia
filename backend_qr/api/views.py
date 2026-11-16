from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status, generics
from django.contrib.auth import get_user_model, authenticate
from .models import QrUsageLog
from .serializers import UserCreateSerializer, QrUsageLogSerializer

User = get_user_model()

class LoginView(APIView):
    def post(self, request):
        rut = request.data.get("rut")
        password = request.data.get("password")
        
        # Autenticar utilizando el RUT
        user = authenticate(request, username=rut, password=password)
        
        if user is not None:
            return Response({
                "token": "token_ejemplo_seguro_123",
                "is_admin": user.is_staff or user.is_superuser,
                "message": "Login exitoso"
            }, status=status.HTTP_200_OK)
        
        return Response({"error": "Credenciales inválidas"}, status=status.HTTP_400_BAD_REQUEST)

class CreateUserView(generics.CreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserCreateSerializer

class QrUsageLogListView(generics.ListCreateAPIView):
    queryset = QrUsageLog.objects.all().order_by('-time')
    serializer_class = QrUsageLogSerializer