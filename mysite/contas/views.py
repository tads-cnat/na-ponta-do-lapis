from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.http import Http404
from .services import ContaService

# Create your views here.

#@login_required(login_url='usuario:login')
def index(request):
    """
    View principal que exibe as contas do usuário logado.
    Redireciona para login se o usuário não estiver autenticado.
    
    print(f"DEBUG: Acessando index - Usuário autenticado: {request.user.is_authenticated}")
    print(f"DEBUG: Usuário: {request.user}")
    print(f"DEBUG: ID do usuário: {request.user.id if request.user.is_authenticated else 'Não autenticado'}")
    
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
        
        
    except Exception as e:
        print(f"DEBUG: Erro ao carregar contas: {str(e)}")
        messages.error(request, f'Erro ao carregar contas: {str(e)}')
        return redirect('usuario:login')
    """
    return render(request, 'contas/index.html', contexto)

#@login_required(login_url='usuario:login')
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
        if conta.id_usuario != request.user:
            messages.error(request, 'Você não tem permissão para acessar essa conta.')
            return redirect('contas:index')
        
        # Buscar transações da conta usando o serviço
        transacoes = ContaService.VisualizarContaService(conta_id)

        contexto = {
            'conta': conta,
            'transacoes': transacoes
        }
        return render(request, 'contas/visualizar_conta.html', contexto)
    
    except Http404:
        raise
    except Exception as e:
        messages.error(request, f'Erro ao visualizar conta: {str(e)}')
        return redirect('contas:index')


#@login_required(login_url='usuario:login')
def add_conta(request):
    """
    View para adicionar uma nova conta financeira.
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
    except Exception as e:
        messages.error(request, f'Erro ao criar conta: {str(e)}')
        return redirect('contas:index')

#@login_required(login_url='usuario:login')
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
        if conta.id_usuario != request.user:
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
    except Http404:
        raise
    except Exception as e:
        messages.error(request, f'Erro ao editar conta: {str(e)}')
        return redirect('contas:index')

@login_required(login_url='usuario:login')
def excluir_conta(request, conta_id):
    """
    View para excluir uma conta específica.
    Apenas o proprietário da conta pode excluí-la.
    """
    try:
        conta = ContaService.obter_conta_por_id(conta_id)
        
        if not conta:
            messages.error(request, 'Conta não encontrada.')
            raise Http404('Conta não encontrada')
        
        # Verificar se o usuário é o proprietário
        if conta.id_usuario != request.user:
            messages.error(request, 'Você não tem permissão para excluir essa conta.')
            return redirect('contas:index')
        
        # Excluir apenas se for POST
        if request.method == 'POST':
            nome_conta = conta.nome
            ContaService.ExcluirContaService(conta_id)
            messages.success(request, f'Conta "{nome_conta}" excluída com sucesso!')
            return redirect('contas:index')
        
        # GET - mostrar página de confirmação
        contexto = {'conta': conta}
        return render(request, 'contas/excluir_conta.html', contexto)
    except Http404:
        raise
    except Exception as e:
        messages.error(request, f'Erro ao excluir conta: {str(e)}')
        return redirect('contas:index')
