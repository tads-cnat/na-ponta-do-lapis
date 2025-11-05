from django.urls import path
from grupo_familiar import views
urlpatterns = [
    path('',views.index,name='home_familia')
]