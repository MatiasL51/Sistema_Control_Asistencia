from django.urls import path
from .views import LoginView, CreateUserView, QrUsageLogListView, ValidateQrView

urlpatterns = [
    path('login/', LoginView.as_view(), name='api-login'),
    path('create-user/', CreateUserView.as_view(), name='api-create-user'),
    path('qr-logs/', QrUsageLogListView.as_view(), name='api-qr-logs'),
    path('validate-qr/', ValidateQrView.as_view(), name='api-validate-qr'),
]