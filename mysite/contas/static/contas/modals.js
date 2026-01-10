/**
 * Modal Manager para operações CRUD de contas
 * Gerencia abertura/fechamento de modais e submissão de formulários
 */

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
                btn.classList.remove('!bg-red-500', '!border-red-500', '!text-white');
                btn.classList.add('border-2', 'border-gray-300', 'text-gray-800');
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
        // Buscar dados da conta via API JSON
        fetch(`/contas/api/obter/${accountId}/`)
            .then(response => response.json())
            .then(data => {
                if (data.sucesso) {
                    // Preencher formulário com dados da conta
                    document.getElementById('editAccountName').value = data.nome;
                    document.getElementById('editAccountBalance').value = data.saldo.toFixed(2);

                    // Selecionar tipo de conta
                    const typeButtons = document.querySelectorAll('.edit-account-type-btn');
                    typeButtons.forEach(btn => {
                        btn.classList.remove('!bg-red-500', '!border-red-500', '!text-white');
                        if (btn.getAttribute('data-type') === data.tipo) {
                            btn.classList.add('!bg-red-500', '!border-red-500', '!text-white');
                        }
                    });
                    document.getElementById('selectedEditAccountType').value = data.tipo;
                } else {
                    alert('Erro ao carregar dados da conta: ' + data.erro);
                }
            })
            .catch(error => {
                console.error('Erro ao carregar conta:', error);
                alert('Erro ao carregar dados da conta');
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
                btn.classList.remove('!bg-red-500', '!border-red-500', '!text-white');
                btn.classList.add('border-2', 'border-gray-300');
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
        this.currentAccountId = accountId;
        const toggle = document.getElementById('viewAccountModalToggle');
        if (toggle) toggle.checked = true;
        // Aqui você pode carregar dados da conta se necessário
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
    const viewAccountBtn = document.querySelector('.viewAccountBtn');
    if (viewAccountBtn) {
        viewAccountBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const accountId = this.getAttribute('data-conta-id');
            if (accountId) {
                modalManager.openViewAccountModal(accountId);
            } else {
                alert('Selecione uma conta para visualizar');
            }
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
});
