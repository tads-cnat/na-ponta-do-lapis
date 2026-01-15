from django.shortcuts import render, redirect
from django.urls import reverse

class AuthenticationMiddleware:
    """
    Middleware para lidar com erros de autenticação de forma consistente.
    Redireciona usuários não autenticados para a página de login.
    """
    
    def __init__(self, get_response):
        self.get_response = get_response
        # URLs que requerem autenticação
        self.protected_urls = [
            '/contas/',
            '/transacoes/',
            '/categoria/',
            '/familia/',
            '/config/',
        ]
    
    def __call__(self, request):
        # Verificar se a URL requer autenticação
        if self._is_protected_url(request.path):
            # Se não está autenticado, redirecionar para login
            if not request.user.is_authenticated:
                # Se for uma requisição AJAX, retornar JSON
                if request.headers.get('X-Requested-With') == 'XMLHttpRequest':
                    from django.http import JsonResponse
                    return JsonResponse(
                        {
                            'error': 'Sessão expirada. Por favor, faça login novamente.',
                            'redirect': reverse('usuario:login')
                        },
                        status=401
                    )
                # Caso contrário, redirecionar para login
                return redirect('usuario:login')
        
        response = self.get_response(request)
        return response
    
    def _is_protected_url(self, path):
        """Verificar se a URL requer autenticação."""
        for protected_url in self.protected_urls:
            if path.startswith(protected_url):
                return True
        return False
