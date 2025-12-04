from django.urls import path
from config import views

app_name = 'config'

urlpatterns = [
    path("", views.config, name="config"),
]
