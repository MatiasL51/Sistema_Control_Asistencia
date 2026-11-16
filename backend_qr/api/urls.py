from django.urls import path
from .views import LoginView, CreateUserView, QrUsageLogListView

urlpatterns = [
    path('login/', LoginView.as_view(), name='api-login'),
    path('users/create/', CreateUserView.as_view(), name='api-create-user'),
    path('qr-logs/', QrUsageLogListView.as_view(), name='api-qr-logs'),
]