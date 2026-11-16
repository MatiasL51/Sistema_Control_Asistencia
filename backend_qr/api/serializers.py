from rest_framework import serializers
from django.contrib.auth import get_user_model
from .models import QrUsageLog

User = get_user_model()

class UserCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['rut', 'password', 'first_name', 'last_name']
        extra_kwargs = {'password': {'write_only': True}}

    def create(self, validated_data):
        user = User.objects.create_user(
            rut=validated_data['rut'],
            password=validated_data['password'],
            first_name=validated_data.get('first_name', ''),
            last_name=validated_data.get('last_name', '')
        )
        return user

class QrUsageLogSerializer(serializers.ModelSerializer):
    class Meta:
        model = QrUsageLog
        fields = ['id', 'rut', 'time', 'status']