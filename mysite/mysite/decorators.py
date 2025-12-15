from django.core.exceptions import PermissionDenied
from django.shortcuts import redirect
from functools import wraps

def papel_requerido(*papeis_permitidos):

    def decorator(view_func):

        @wraps(view_func)
        def wrapper(request, *args, **kwargs):

            if not request.user.is_authenticated:
                return redirect('login')
            
            if request.user.papel not in papeis_permitidos:
                raise PermissionDenied("Você não tem permissão para acessar a página.")
            
            return view_func(request, *args, **kwargs)
        return wrapper
    return decorator