from django.urls import path
from config.views  import config

urlpatterns = [
    path("", config, name="configuracao"),
]
