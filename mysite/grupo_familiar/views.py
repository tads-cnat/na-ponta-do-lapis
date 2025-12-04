from django.shortcuts import render

# Create your views here.
def familia(request):
    return render(request, 'familia/familia.html')