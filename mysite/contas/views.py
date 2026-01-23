from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.http import Http404, JsonResponse
from django.core.exceptions import ValidationError
from django.views.decorators.http import require_http_methods
from django.views.decorators.cache import never_cache
from .services import ContaService

# Create your views here.

@login_required(login_url='login')
def index(request):
    """
    View principal que exibe as contas do usuário logado.
    Redireciona para login se o usuário não estiver autenticado.
    """
    try:
        # Obter contas do usuário logado
        contas = ContaService.obter_contas_usuario(request.user)
        
        # Determinar o layout baseado na quantidade de contas
        quantidade_contas = len(contas)
        
        contexto = {
            'contas': contas,
            'quantidade_contas': quantidade_contas,
            'tem_contas': quantidade_contas > 0,
            'uma_conta': quantidade_contas == 1,
            'duas_contas': quantidade_contas == 2,
            'tres_ou_mais': quantidade_contas >= 3,
        }
        
        return render(request, 'contas/index.html', contexto)
    except Exception as e:
        messages.error(request, f'Erro ao carregar contas: {str(e)}')
        return redirect('login')

@login_required(login_url='usuario:login')
def visualizar_conta(request, conta_id):
    """
    View para visualizar uma conta específica.
    Apenas o proprietário da conta pode visualizá-la.
    """
    try:
        conta = ContaService.obter_conta_por_id(conta_id)
        
        if not conta:
            messages.error(request, 'Conta não encontrada.')
            raise Http404('Conta não encontrada')
        
        # Verificar se o usuário é o proprietário
        if conta.usuario != request.user:
            messages.error(request, 'Você não tem permissão para acessar essa conta.')
            return redirect('contas:index')
        
        contexto = {'conta': conta}
        return render(request, 'contas/visualizar_conta.html', contexto)
    except Http404:
        raise
    except Exception as e:
        messages.error(request, f'Erro ao visualizar conta: {str(e)}')
        return redirect('contas:index')

@login_required(login_url='login')
def add_conta(request):
    """
    View para adicionar uma nova conta financeira.
    Captura erros de validação e os exibe ao usuário.
    """
    try:
        if request.method == 'POST':
            nome = request.POST.get('nome')
            saldo = request.POST.get('saldo')
            tipo = request.POST.get('tipo')

            # Validação básica
            if not nome or not saldo or not tipo:
                messages.error(request, 'Por favor, preencha todos os campos obrigatórios.')
                return redirect('contas:index')

            # Passar o usuário logado para o serviço
            conta = ContaService.AddContaService(nome, saldo, tipo, usuario=request.user)

            if conta:
                messages.success(request, f'Conta "{conta.nome}" criada com sucesso!')
                return redirect('contas:index')
        
        return redirect('contas:index')
    except ValidationError as e:
        # Capturar erros de validação do modelo
        if hasattr(e, 'message_dict'):
            # Erros de campo específicos
            for campo, erros in e.message_dict.items():
                for erro in erros:
                    messages.error(request, f'{campo.upper()}: {erro}')
        else:
            # Erros genéricos
            for erro in e.messages:
                messages.error(request, erro)
        return redirect('contas:index')
    except Exception as e:
        messages.error(request, f'Erro ao criar conta: {str(e)}')
        return redirect('contas:index')

@login_required(login_url='login')
def editar_conta(request, conta_id):
    """
    View para editar uma conta específica.
    Apenas o proprietário da conta pode editá-la.
    """
    try:
        conta = ContaService.obter_conta_por_id(conta_id)
        
        if not conta:
            messages.error(request, 'Conta não encontrada.')
            raise Http404('Conta não encontrada')
        
        # Verificar se o usuário é o proprietário
        if conta.usuario != request.user:
            messages.error(request, 'Você não tem permissão para editar essa conta.')
            return redirect('contas:index')
        
        if request.method == 'POST':
            nome = request.POST.get('nome')
            saldo = request.POST.get('saldo')
            tipo = request.POST.get('tipo')

            # Validação básica
            if not nome or not saldo or not tipo:
                messages.error(request, 'Por favor, preencha todos os campos obrigatórios.')
                return redirect('contas:index')

            conta = ContaService.EditarContaService(conta_id, nome, saldo, tipo)
            
            if conta:
                messages.success(request, f'Conta "{conta.nome}" atualizada com sucesso!')
                return redirect('contas:index')
        
        contexto = {'conta': conta}
        return render(request, 'contas/editar_conta.html', contexto)
    except ValidationError as e:
        # Capturar erros de validação do modelo
        if hasattr(e, 'message_dict'):
            # Erros de campo específicos
            for campo, erros in e.message_dict.items():
                for erro in erros:
                    messages.error(request, f'{campo.upper()}: {erro}')
        else:
            # Erros genéricos
            for erro in e.messages:
                messages.error(request, erro)
        return redirect('contas:index')
    except Http404:
        raise
    except Exception as e:
        messages.error(request, f'Erro ao editar conta: {str(e)}')
        return redirect('contas:index')

@login_required(login_url='login')
@require_http_methods(['POST'])
@never_cache
def excluir_conta(request, conta_id):
    """
    View para excluir uma conta específica via POST.
    Apenas o proprietário da conta pode excluí-la.
    Retorna JSON com resultado da operação.
    """
    try:
        conta = ContaService.obter_conta_por_id(conta_id)
        
        if not conta:
            return JsonResponse({'sucesso': False, 'erro': 'Conta não encontrada'}, status=404)
        
        # Verificar se o usuário é o proprietário
        if conta.usuario != request.user:
            return JsonResponse({'sucesso': False, 'erro': 'Sem permissão para excluir essa conta'}, status=403)
        
        # Excluir a conta
        nome_conta = conta.nome
        ContaService.ExcluirContaService(conta_id)
        
        return JsonResponse({
            'sucesso': True,
            'mensagem': f'Conta "{nome_conta}" excluída com sucesso!',
            'nome_conta': nome_conta
        })
    except Exception as e:
        return JsonResponse({'sucesso': False, 'erro': str(e)}, status=500)

@login_required(login_url='usuario:login')
def obter_conta_json(request, conta_id):
    """
    API que retorna dados da conta em JSON.
    Usado pelo JavaScript para preencher os formulários de edição.
    """
    try:
        conta = ContaService.obter_conta_por_id(conta_id)
        
        if not conta:
            return JsonResponse({'sucesso': False, 'erro': 'Conta não encontrada'}, status=404)
        
        # Verificar se o usuário é o proprietário
        if conta.usuario != request.user:
            return JsonResponse({'sucesso': False, 'erro': 'Permissão negada'}, status=403)
        
        # Retornar dados da conta em JSON
        return JsonResponse({
            'id': conta.id,
            'nome': conta.nome,
            'saldo': float(conta.saldo),
            'tipo': conta.tipo,
            'sucesso': True
        })
    except Exception as e:
        return JsonResponse({'sucesso': False, 'erro': str(e)}, status=500)

@login_required(login_url='usuario:login')
def obter_transacoes_conta(request, conta_id):
    """
    API que retorna as transações de uma conta em JSON.
    Usado pelo modal de visualização de conta.
    """
    try:
        conta = ContaService.obter_conta_por_id(conta_id)
        
        if not conta:
            return JsonResponse({'sucesso': False, 'erro': 'Conta não encontrada'}, status=404)
        
        # Verificar se o usuário é o proprietário
        if conta.usuario != request.user:
            return JsonResponse({'sucesso': False, 'erro': 'Permissão negada'}, status=403)
        
        # Obter transações da conta (tenta importar do serviço de contas)
        transacoes_qs = ContaService.VisualizarContaService(conta_id)
        
        # Converter QuerySet para lista de dicionários
        transacoes = []
        if transacoes_qs:
            for transacao in transacoes_qs:
                transacoes.append({
                    'id': transacao.id,
                    'estado': transacao.get_estado_display(),
                    'data_hora': transacao.data_hora.strftime('%d/%m/%Y %H:%M') if transacao.data_hora else '-',
                    'valor': f"R$ {float(transacao.valor):.2f}".replace('.', ','),
                    'descricao': transacao.descricao,
                    'tipo': transacao.get_tipo_display(),
                    'categoria': transacao.get_categoria_display(),
                    'conta': transacao.conta_financeira.nome if transacao.conta_financeira else '-',
                })
        
        tem_transacoes = len(transacoes) > 0
        
        # Montar resposta com dados da conta e transações
        return JsonResponse({
            'sucesso': True,
            'conta': {
                'id': conta.id,
                'nome': conta.nome,
                'saldo': float(conta.saldo),
                'tipo': conta.tipo,
            },
            'transacoes': transacoes,
            'tem_transacoes': tem_transacoes
        })
    except Exception as e:
        return JsonResponse({'sucesso': False, 'erro': str(e)}, status=500)