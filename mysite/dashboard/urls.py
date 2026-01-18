from django.contrib import admin
from django.urls import include, path
from . import views

app_name = "dashboard"

urlpatterns = [
    path('',views.dashboard_view, name="dashboard"),
]