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
    }

    closeEditAccountModal() {
        const toggle = document.getElementById('editAccountModalToggle');
        if (toggle) toggle.checked = false;
        this.currentAccountId = null;
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
        if (!this.currentAccountId) {
            alert('Nenhuma conta selecionada');
            return;
        }
        // Redirecionar para a view de exclusão
        window.location.href = `/contas/excluir/${this.currentAccountId}/`;
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
