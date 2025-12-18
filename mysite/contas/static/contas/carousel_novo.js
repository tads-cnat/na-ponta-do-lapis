/**
 * Carousel Manager - Gerencia navegação entre contas
 */
class CarouselManager {
    constructor() {
        this.currentIndex = 0;
        this.cards = [];
        this.indicators = [];
        this.init();
    }

    init() {
        // Obter todos os cards do carousel
        this.cards = document.querySelectorAll('.carousel-card');
        this.indicators = document.querySelectorAll('.carousel-indicator');

        if (this.cards.length === 0) {
            console.warn('Nenhum card de carousel encontrado');
            return;
        }

        // Mostrar primeiro card
        this.updateDisplay();

        // Setup botões de navegação
        this.setupNavigation();

        // Setup indicadores clicáveis
        this.setupIndicators();

        // Setup botões CRUD
        this.setupCrudButtons();
    }

    setupNavigation() {
        const prevBtn = document.getElementById('carouselPrev');
        const nextBtn = document.getElementById('carouselNext');

        if (prevBtn) {
            prevBtn.addEventListener('click', () => this.prevCard());
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => this.nextCard());
        }
    }

    setupIndicators() {
        this.indicators.forEach((indicator, index) => {
            indicator.addEventListener('click', () => {
                this.currentIndex = index;
                this.updateDisplay();
                this.updateCrudButtons();
            });
        });
    }

    prevCard() {
        this.currentIndex = (this.currentIndex - 1 + this.cards.length) % this.cards.length;
        this.updateDisplay();
        this.updateCrudButtons();
    }

    nextCard() {
        this.currentIndex = (this.currentIndex + 1) % this.cards.length;
        this.updateDisplay();
        this.updateCrudButtons();
    }

    updateDisplay() {
        // Atualizar visibilidade e z-index dos cards
        this.cards.forEach((card, index) => {
            const offset = (index - this.currentIndex + this.cards.length) % this.cards.length;

            if (offset === 0) {
                // Card atual - no centro
                card.style.opacity = '1';
                card.style.zIndex = '20';
                card.style.transform = 'scale(1) translateX(0)';
            } else if (offset === 1) {
                // Card próximo - à direita
                card.style.opacity = '0.6';
                card.style.zIndex = '10';
                card.style.transform = 'scale(0.9) translateX(250px)';
            } else if (offset === this.cards.length - 1) {
                // Card anterior - à esquerda
                card.style.opacity = '0.6';
                card.style.zIndex = '10';
                card.style.transform = 'scale(0.9) translateX(-250px)';
            } else {
                // Cards muito distantes - invisíveis
                card.style.opacity = '0';
                card.style.zIndex = '-1';
                card.style.transform = 'scale(0.9) translateX(0)';
            }
        });

        // Atualizar indicadores
        this.indicators.forEach((indicator, index) => {
            if (index === this.currentIndex) {
                indicator.classList.remove('bg-gray-400');
                indicator.classList.add('bg-gray-900', 'w-3', 'h-3');
            } else {
                indicator.classList.remove('bg-gray-900', 'w-3', 'h-3');
                indicator.classList.add('bg-gray-400');
            }
        });
    }

    getCurrentAccountId() {
        if (this.cards.length > this.currentIndex) {
            const card = this.cards[this.currentIndex];
            const id = card.getAttribute('data-conta-id');
            return id;
        }
        return null;
    }

    updateCrudButtons() {
        const contaId = this.getCurrentAccountId();

        // Atualizar data-conta-id em todos os botões CRUD
        document.querySelectorAll('[data-conta-id]').forEach(btn => {
            if (btn.classList && (
                btn.classList.contains('viewAccountBtn') ||
                btn.classList.contains('editAccountBtn') ||
                btn.classList.contains('deleteAccountBtn')
            )) {
                btn.setAttribute('data-conta-id', contaId);
            }
        });
    }

    setupCrudButtons() {
        // Botão Visualizar
        const viewBtn = document.querySelector('.viewAccountBtn');
        if (viewBtn) {
            viewBtn.addEventListener('click', (e) => {
                e.preventDefault();
                if (viewBtn.hasAttribute('disabled')) return;

                const contaId = this.getCurrentAccountId();
                if (contaId && modalManager) {
                    modalManager.openViewAccountModal(contaId);
                }
            });
        }

        // Botão Editar
        const editBtn = document.querySelector('.editAccountBtn');
        if (editBtn) {
            editBtn.addEventListener('click', (e) => {
                e.preventDefault();
                if (editBtn.hasAttribute('disabled')) return;

                const contaId = this.getCurrentAccountId();
                if (contaId && modalManager) {
                    modalManager.openEditAccountModal(contaId);
                }
            });
        }

        // Botão Excluir
        const deleteBtn = document.querySelector('.deleteAccountBtn');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', (e) => {
                e.preventDefault();
                if (deleteBtn.hasAttribute('disabled')) return;

                const contaId = this.getCurrentAccountId();
                if (contaId && modalManager) {
                    modalManager.openDeleteAccountModal(contaId);
                }
            });
        }

        // Botão Adicionar
        const addBtn = document.querySelector('.addAccountBtn');
        if (addBtn) {
            addBtn.addEventListener('click', (e) => {
                e.preventDefault();
                if (modalManager) {
                    modalManager.openAddAccountModal();
                }
            });
        }
    }
}

// Inicializar carousel quando o documento carregar
let carouselManager;
document.addEventListener('DOMContentLoaded', function () {
    carouselManager = new CarouselManager();
});
