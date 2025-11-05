from django.shortcuts import render

# Create your views here.
def config(request):
    return render(request, "config/config.html")