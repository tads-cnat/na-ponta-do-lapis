// Modal Manager para operações CRUD de contas
class ModalManager {
    constructor() {
        this.currentModal = null;
    }

    // Abrir modal de adicionar conta
    openAddAccountModal() {
        const modal = document.getElementById('addAccountModal');
        if (modal) {
            modal.style.display = 'flex';
            this.currentModal = modal;
        }
    }

    // Fechar modal de adicionar conta
    closeAddAccountModal() {
        const modal = document.getElementById('addAccountModal');
        if (modal) {
            modal.style.display = 'none';
            this.currentModal = null;
            this.resetForm('addAccountForm');
        }
    }

    // Resetar formulário
    resetForm(formId) {
        const form = document.getElementById(formId);
        if (form) {
            form.reset();
            // Remover classe de seleção do tipo de conta
            document.querySelectorAll('.account-type-btn').forEach(btn => {
                btn.classList.remove('!bg-red-500', '!border-red-500', '!text-white');
            });
        }
    }

    // Selecionar tipo de conta
    selectAccountType(button, type) {
        // Remove seleção anterior
        document.querySelectorAll('.account-type-btn').forEach(btn => {
            btn.classList.remove('!bg-red-500', '!border-red-500', '!text-white');
            btn.classList.add('border-2', 'border-gray-300', 'text-gray-800');
        });

        // Adiciona seleção ao botão clicado
        button.classList.remove('border-2', 'border-gray-300', 'text-gray-800');
        button.classList.add('!bg-red-500', '!border-red-500', '!text-white');

        // Armazena o tipo selecionado
        document.getElementById('selectedAccountType').value = type;
    }

    // Enviar formulário de adicionar conta
    async submitAddAccountForm(event) {
        event.preventDefault();

        const accountName = document.getElementById('accountName').value;
        const accountBalance = document.getElementById('accountBalance').value;
        const selectedType = document.getElementById('selectedAccountType').value;

        // Validações básicas
        if (!accountName || !accountBalance || !selectedType) {
            alert('Por favor, preencha todos os campos');
            return;
        }

        try {
            // Aqui será feita a requisição assíncrona quando o backend estiver pronto
            const response = await this.makeAsyncRequest('/api/contas/add/', {
                nome: accountName,
                saldo: parseFloat(accountBalance),
                tipo: selectedType
            });

            console.log('Conta adicionada:', response);
            this.closeAddAccountModal();
            // Aqui será feito o refresh da página ou atualização dos cartões
            // location.reload();
        } catch (error) {
            console.error('Erro ao adicionar conta:', error);
            alert('Erro ao adicionar conta. Tente novamente.');
        }
    }

    // Fazer requisição assíncrona genérica
    async makeAsyncRequest(url, data) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRFToken': this.getCookie('csrftoken')
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            throw error;
        }
    }

    // Obter CSRF token
    getCookie(name) {
        let cookieValue = null;
        if (document.cookie && document.cookie !== '') {
            const cookies = document.cookie.split(';');
            for (let i = 0; i < cookies.length; i++) {
                const cookie = cookies[i].trim();
                if (cookie.substring(0, name.length + 1) === (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }
}

// Inicializar manager quando o documento carregar
let modalManager;
document.addEventListener('DOMContentLoaded', function () {
    modalManager = new ModalManager();

    // Adicionar event listener ao botão de adicionar conta
    const addAccountBtn = document.querySelector('.addAccountBtn');
    if (addAccountBtn) {
        addAccountBtn.addEventListener('click', function (e) {
            e.preventDefault();
            modalManager.openAddAccountModal();
        });
    }

    // Fechar modal ao clicar no X
    const closeBtn = document.getElementById('closeAddAccountModal');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => modalManager.closeAddAccountModal());
    }

    // Fechar modal ao clicar fora dele
    const modal = document.getElementById('addAccountModal');
    if (modal) {
        modal.addEventListener('click', function (e) {
            if (e.target === modal) {
                modalManager.closeAddAccountModal();
            }
        });
    }

    // Formulário submit
    const form = document.getElementById('addAccountForm');
    if (form) {
        form.addEventListener('submit', (e) => modalManager.submitAddAccountForm(e));
    }

    // Botões de tipo de conta
    document.querySelectorAll('.account-type-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const type = this.getAttribute('data-type');
            modalManager.selectAccountType(this, type);
        });
    });

    // Botão cancelar
    const cancelBtn = document.getElementById('cancelAddAccountBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => modalManager.closeAddAccountModal());
    }
});
