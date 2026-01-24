/**
 * Modal Manager para operações CRUD de contas
 * Gerencia abertura/fechamento de modais e submissão de formulários
 */

console.log('modals.js carregado com sucesso');

class ModalManager {
    constructor() {
        this.currentAccountId = null;
    }

    // ==================== MODAL ADICIONAR CONTA ====================
    openAddAccountModal() {
        const toggle = document.getElementById('addAccountModalToggle');
        if (toggle) toggle.checked = true;
        this.resetFormAdd();
    }

    closeAddAccountModal() {
        const toggle = document.getElementById('addAccountModalToggle');
        if (toggle) toggle.checked = false;
        this.resetFormAdd();
    }

    resetFormAdd() {
        const form = document.getElementById('addAccountForm');
        if (form) {
            form.reset();
            document.querySelectorAll('.account-type-btn').forEach(btn => {
                btn.classList.remove('scale-105');
                btn.style.borderColor = btn.dataset.type === 'DEBITO' ? '#1f2937' : '#d1d5db';
                btn.parentElement.querySelector('.selected-label').classList.add('invisible');
            });
            document.getElementById('selectedAccountType').value = '';
        }
    }

    // ==================== MODAL EDITAR CONTA ====================
    openEditAccountModal(accountId) {
        this.currentAccountId = accountId;
        const toggle = document.getElementById('editAccountModalToggle');
        if (toggle) toggle.checked = true;

        // Definir action do formulário
        const form = document.getElementById('editAccountForm');
        if (form) {
            form.action = `/contas/editar/${accountId}/`;
        }

        // Carregar dados da conta via API
        this.loadAccountData(accountId);
    }

    loadAccountData(accountId) {
        console.log('Carregando dados da conta:', accountId);

        // Buscar dados da conta via API JSON
        fetch(`/contas/api/obter/${accountId}/`)
            .then(response => {
                console.log('Response status:', response.status);
                return response.json();
            })
            .then(data => {
                console.log('Dados recebidos:', data);

                if (data.sucesso) {
                    console.log('Preenchendo formulário com:', data);

                    // Preencher formulário com dados da conta
                    document.getElementById('editAccountName').value = data.nome;
                    document.getElementById('editAccountBalance').value = data.saldo.toFixed(2);

                    // Selecionar tipo de conta
                    const typeButtons = document.querySelectorAll('.edit-account-type-btn');
                    typeButtons.forEach(btn => {
                        btn.classList.remove('scale-105');
                        btn.style.borderColor = btn.getAttribute('data-type') === 'DEBITO' ? '#1f2937' : '#d1d5db';
                        btn.parentElement.querySelector('.selected-label').classList.add('invisible');
                        if (btn.getAttribute('data-type') === data.tipo) {
                            btn.classList.add('scale-105');
                            btn.style.borderColor = '#000000';
                            btn.parentElement.querySelector('.selected-label').classList.remove('invisible');
                        }
                    });
                    document.getElementById('selectedEditAccountType').value = data.tipo;
                    console.log('Formulário preenchido com sucesso');
                } else {
                    console.error('Erro da API:', data.erro);
                    alert('Erro ao carregar dados da conta: ' + data.erro);
                }
            })
            .catch(error => {
                console.error('Erro na requisição fetch:', error);
                alert('Erro ao carregar dados da conta: ' + error.message);
            });
    }

    closeEditAccountModal() {
        const toggle = document.getElementById('editAccountModalToggle');
        if (toggle) toggle.checked = false;
        this.resetFormEdit();
        this.currentAccountId = null;
    }

    resetFormEdit() {
        const form = document.getElementById('editAccountForm');
        if (form) {
            form.reset();
            document.querySelectorAll('.edit-account-type-btn').forEach(btn => {
                btn.classList.remove('scale-105');
                btn.style.borderColor = btn.dataset.type === 'DEBITO' ? '#1f2937' : '#d1d5db';
                btn.parentElement.querySelector('.selected-label').classList.add('invisible');
            });
            document.getElementById('selectedEditAccountType').value = '';
        }
    }

    // ==================== MODAL EXCLUIR CONTA ====================
    openDeleteAccountModal(accountId) {
        this.currentAccountId = accountId;
        const toggle = document.getElementById('deleteAccountModalToggle');
        if (toggle) toggle.checked = true;
    }

    closeDeleteAccountModal() {
        const toggle = document.getElementById('deleteAccountModalToggle');
        if (toggle) toggle.checked = false;
        this.currentAccountId = null;
    }

    confirmDeleteAccount() {
        console.log('confirmDeleteAccount chamado');
        console.log('currentAccountId:', this.currentAccountId);

        if (!this.currentAccountId) {
            alert('Nenhuma conta selecionada');
            return;
        }

        // Obter CSRF token
        const csrfToken = document.querySelector('[name=csrfmiddlewaretoken]')?.value ||
            document.querySelector('[name="csrfmiddlewaretoken"]')?.value;

        console.log('CSRF Token:', csrfToken);
        console.log('Enviando DELETE para:', `/contas/excluir/${this.currentAccountId}/`);

        // Enviar POST fetch para excluir
        fetch(`/contas/excluir/${this.currentAccountId}/`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRFToken': csrfToken
            },
            body: JSON.stringify({})
        })
            .then(response => {
                console.log('Response status:', response.status);
                return response.json();
            })
            .then(data => {
                console.log('Response data:', data);
                this.closeDeleteAccountModal();

                if (data.sucesso) {
                    // Criar alerta de sucesso
                    const alertDiv = document.createElement('div');
                    alertDiv.className = 'alert alert-success shadow-lg fixed top-8 left-8 right-8 z-50';
                    alertDiv.innerHTML = `
                    <div class="flex items-start gap-4">
                        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current flex-shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <div class="flex-grow">
                            <h3 class="font-bold">Sucesso</h3>
                            <div class="text-sm">${data.mensagem}</div>
                        </div>
                    </div>
                `;
                    document.body.appendChild(alertDiv);

                    // Auto-dismiss após 5 segundos
                    setTimeout(() => {
                        alertDiv.style.transition = 'opacity 0.3s ease-out';
                        alertDiv.style.opacity = '0';
                        setTimeout(() => alertDiv.remove(), 300);
                    }, 5000);

                    // Recarregar página após 500ms
                    setTimeout(() => location.reload(), 500);
                } else {
                    // Criar alerta de erro
                    const alertDiv = document.createElement('div');
                    alertDiv.className = 'alert alert-error shadow-lg fixed top-8 left-8 right-8 z-50';
                    alertDiv.innerHTML = `
                    <div class="flex items-start gap-4">
                        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current flex-shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l-2-2m0 0l-2-2m2 2l2-2m-2 2l-2 2m2-2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <div class="flex-grow">
                            <h3 class="font-bold">Erro</h3>
                            <div class="text-sm">${data.erro}</div>
                        </div>
                    </div>
                `;
                    document.body.appendChild(alertDiv);

                    // Auto-dismiss após 5 segundos
                    setTimeout(() => {
                        alertDiv.style.transition = 'opacity 0.3s ease-out';
                        alertDiv.style.opacity = '0';
                        setTimeout(() => alertDiv.remove(), 300);
                    }, 5000);
                }
            })
            .catch(error => {
                console.error('Erro ao excluir conta:', error);
                this.closeDeleteAccountModal();
                alert('Erro ao excluir a conta. Tente novamente.');
            });
    }

    // ==================== MODAL VISUALIZAR CONTA ====================
    openViewAccountModal(accountId) {
        console.log('Abrindo modal de visualizar conta:', accountId);
        this.currentAccountId = accountId;
        const toggle = document.getElementById('viewAccountModalToggle');
        if (toggle) toggle.checked = true;

        // Carregar dados da conta e transações
        this.loadAccountTransactions(accountId);
    }

    loadAccountTransactions(accountId) {
        console.log('Carregando transações para conta:', accountId);

        // Buscar dados da conta e transações via API JSON
        fetch(`/contas/api/transacoes/${accountId}/`)
            .then(response => {
                console.log('Response status:', response.status);
                return response.json();
            })
            .then(data => {
                console.log('Dados de transações recebidos:', data);

                if (data.sucesso) {
                    console.log('Preenchendo modal com dados da conta');

                    // Preencher dados da conta - verificar se elementos existem
                    const nameEl = document.getElementById('viewAccountName');
                    const balanceEl = document.getElementById('viewAccountBalance');
                    const typeEl = document.getElementById('viewAccountType');

                    if (!nameEl || !balanceEl || !typeEl) {
                        console.error('Elementos do modal não encontrados!');
                        console.error('viewAccountName:', nameEl);
                        console.error('viewAccountBalance:', balanceEl);
                        console.error('viewAccountType:', typeEl);
                        alert('Erro ao exibir dados da conta: elementos não encontrados');
                        return;
                    }

                    nameEl.textContent = data.conta.nome;
                    balanceEl.textContent = 'R$ ' + parseFloat(data.conta.saldo).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
                    typeEl.textContent = data.conta.tipo;

                    console.log('data.tem_transacoes:', data.tem_transacoes);
                    console.log('data.transacoes:', data.transacoes);
                    console.log('data.transacoes.length:', data.transacoes.length);

                    // Verificar se há transações
                    if (data.tem_transacoes && data.transacoes.length > 0) {
                        console.log('Renderizando tabela de transações');
                        this.renderTransactionsTable(data.transacoes);
                    } else {
                        console.log('Sem transações - mostrando mensagem');
                        this.showNoTransactionsMessage();
                    }
                } else {
                    console.error('Erro da API:', data.erro);
                    alert('Erro ao carregar transações: ' + data.erro);
                }
            })
            .catch(error => {
                console.error('Erro na requisição fetch de transações:', error);
                alert('Erro ao carregar transações: ' + error.message);
            });
    }

    renderTransactionsTable(transacoes) {
        console.log('Renderizando transações:', transacoes.length);

        const container = document.getElementById('viewTransactionsContainer');

        // Template HTML da tabela
        let html = `
            <div class="mb-4">
                <h3 class="text-lg font-bold text-black bg-white p-3 rounded-lg">Histórico (${transacoes.length} transações)</h3>
            </div>
            <div class="overflow-x-auto mb-6">
                <table class="w-full bg-white rounded-lg overflow-hidden">
                    <thead class="bg-gray-400 text-black">
                        <tr>
                            <th class="px-4 py-3 text-left font-semibold">Estado</th>
                            <th class="px-4 py-3 text-left font-semibold">Data/Hora</th>
                            <th class="px-4 py-3 text-left font-semibold">Valor</th>
                            <th class="px-4 py-3 text-left font-semibold">Descrição</th>
                            <th class="px-4 py-3 text-left font-semibold">Tipo</th>
                            <th class="px-4 py-3 text-left font-semibold">Categoria</th>
                            <th class="px-4 py-3 text-left font-semibold">Conta</th>
                        </tr>
                    </thead>
                    <tbody class="text-black">
        `;

        // Adicionar linhas de transações
        transacoes.forEach((transacao, index) => {
            html += `
                <tr class="border-b border-gray-200 hover:bg-gray-50">
                    <td class="px-4 py-3">${transacao.estado}</td>
                    <td class="px-4 py-3">${transacao.data_hora}</td>
                    <td class="px-4 py-3 font-semibold">${transacao.valor}</td>
                    <td class="px-4 py-3">${transacao.descricao}</td>
                    <td class="px-4 py-3"><span class="bg-gray-200 text-gray-800 px-2 py-1 rounded text-sm font-semibold">${transacao.tipo}</span></td>
                    <td class="px-4 py-3"><span class="bg-gray-200 text-gray-800 px-2 py-1 rounded text-sm font-semibold">${transacao.categoria}</span></td>
                    <td class="px-4 py-3">${transacao.conta}</td>
                </tr>
            `;
        });

        html += `
                    </tbody>
                </table>
            </div>
        `;

        container.innerHTML = html;
    }

    showNoTransactionsMessage() {
        console.log('Mostrando mensagem de sem transações');

        const container = document.getElementById('viewTransactionsContainer');

        // Renderizar mensagem de sem transações no container
        container.innerHTML = `
            <div class="text-center py-12">
                <div class="text-6xl mb-4">📭</div>
                <h3 class="text-2xl font-bold text-gray-700 mb-2">Sem Transações</h3>
                <p class="text-gray-600 mb-6">Esta conta ainda não possui transações registradas.</p>
                <a href="/transacoes/"
                    class="px-6 py-3 bg-green-400 text-green-900 font-semibold rounded-lg hover:bg-green-500 inline-block">
                    Ir para Transações
                </a>
            </div>
        `;
    }

    closeViewAccountModal() {
        const toggle = document.getElementById('viewAccountModalToggle');
        if (toggle) toggle.checked = false;
        this.currentAccountId = null;
    }

    // ==================== SELEÇÃO DE TIPO ====================
    selectAccountType(button, type) {
        document.querySelectorAll('.account-type-btn').forEach(btn => {
            btn.classList.remove('!bg-red-500', '!border-red-500', '!text-white');
            btn.classList.add('border-2', 'border-gray-300', 'text-gray-800');
        });
        button.classList.remove('border-2', 'border-gray-300', 'text-gray-800');
        button.classList.add('!bg-red-500', '!border-red-500', '!text-white');
        document.getElementById('selectedAccountType').value = type;
    }

    selectEditAccountType(button, type) {
        document.querySelectorAll('.edit-account-type-btn').forEach(btn => {
            btn.classList.remove('!bg-red-500', '!border-red-500', '!text-white');
        });
        button.classList.add('!bg-red-500', '!border-red-500', '!text-white');
        document.getElementById('selectedEditAccountType').value = type;
    }
}

// Inicializar quando o documento carregar
let modalManager;
document.addEventListener('DOMContentLoaded', function () {
    console.log('DOMContentLoaded - Inicializando modals');
    modalManager = new ModalManager();

    // ==================== MODAL ADICIONAR ====================
    const addAccountBtn = document.querySelector('.addAccountBtn');
    if (addAccountBtn) {
        addAccountBtn.addEventListener('click', function (e) {
            e.preventDefault();
            modalManager.openAddAccountModal();
        });
    }

    const addForm = document.getElementById('addAccountForm');
    if (addForm) {
        addForm.addEventListener('submit', function (e) {
            // Form será enviado naturalmente via POST para /contas/add_conta/
        });
    }

    document.querySelectorAll('.account-type-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const type = this.getAttribute('data-type');
            modalManager.selectAccountType(this, type);
        });
    });

    const addToggle = document.getElementById('addAccountModalToggle');
    if (addToggle) {
        addToggle.addEventListener('change', function () {
            if (!this.checked) {
                modalManager.resetFormAdd();
            }
        });
    }

    // ==================== MODAL EDITAR ====================
    const editAccountBtn = document.querySelector('.editAccountBtn');
    if (editAccountBtn) {
        editAccountBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const accountId = this.getAttribute('data-conta-id');
            if (accountId) {
                modalManager.openEditAccountModal(accountId);
            } else {
                alert('Selecione uma conta para editar');
            }
        });
    }

    const editForm = document.getElementById('editAccountForm');
    if (editForm) {
        editForm.addEventListener('submit', function (e) {
            // Form será enviado naturalmente para a action definida em openEditAccountModal
        });
    }

    document.querySelectorAll('.edit-account-type-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const type = this.getAttribute('data-type');
            modalManager.selectEditAccountType(this, type);
        });
    });

    const editToggle = document.getElementById('editAccountModalToggle');
    if (editToggle) {
        editToggle.addEventListener('change', function () {
            if (!this.checked) {
                modalManager.closeEditAccountModal();
            }
        });
    }

    // ==================== MODAL EXCLUIR ====================
    const deleteAccountBtn = document.querySelector('.deleteAccountBtn');
    if (deleteAccountBtn) {
        deleteAccountBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const accountId = this.getAttribute('data-conta-id');
            if (accountId) {
                modalManager.openDeleteAccountModal(accountId);
            } else {
                alert('Selecione uma conta para excluir');
            }
        });
    }

    const confirmDeleteBtn = document.getElementById('confirmDeleteAccountBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', function () {
            modalManager.confirmDeleteAccount();
        });
    }

    const deleteToggle = document.getElementById('deleteAccountModalToggle');
    if (deleteToggle) {
        deleteToggle.addEventListener('change', function () {
            if (!this.checked) {
                modalManager.closeDeleteAccountModal();
            }
        });
    }

    // ==================== MODAL VISUALIZAR ====================
    console.log('Buscando botões viewAccountBtn');
    const viewAccountBtns = document.querySelectorAll('.viewAccountBtn');
    console.log('Encontrados ' + viewAccountBtns.length + ' botões visualizar');

    if (viewAccountBtns.length > 0) {
        viewAccountBtns.forEach((btn, index) => {
            console.log('Adicionando listener ao botão ' + index);
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                const accountId = this.getAttribute('data-conta-id');
                console.log('Clicado botão visualizar, accountId:', accountId);
                if (accountId && accountId !== '0') {
                    modalManager.openViewAccountModal(accountId);
                } else {
                    alert('Selecione uma conta para visualizar');
                }
            });
        });
    }

    const viewToggle = document.getElementById('viewAccountModalToggle');
    if (viewToggle) {
        viewToggle.addEventListener('change', function () {
            if (!this.checked) {
                modalManager.closeViewAccountModal();
            }
        });
    }

    // ==================== MODAL SEM TRANSAÇÕES ====================
    const noTransactionsToggle = document.getElementById('noTransactionsModalToggle');
    if (noTransactionsToggle) {
        noTransactionsToggle.addEventListener('change', function () {
            if (!this.checked) {
                // Modal fechado
            }
        });
    }
});
