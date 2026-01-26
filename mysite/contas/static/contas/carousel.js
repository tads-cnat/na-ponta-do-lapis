/**
 * Carousel Manager - Gerencia navegação entre contas com suporte a 3+ cards
 * Navegação: Direita (→) = próxima conta (mais recente)
 *           Esquerda (←) = conta anterior (mais antiga)
 * Loop automático: primeira → esquerda → última; última → direita → primeira
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
        this.updateCrudButtons();

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

    /**
     * Vai para a conta anterior (esquerda = mais antiga)
     * Com loop automático: primeira → esquerda → última
     */
    prevCard() {
        this.currentIndex = (this.currentIndex - 1 + this.cards.length) % this.cards.length;
        this.updateDisplay();
        this.updateCrudButtons();
    }

    /**
     * Vai para a próxima conta (direita = mais recente)
     * Com loop automático: última → direita → primeira
     */
    nextCard() {
        this.currentIndex = (this.currentIndex + 1) % this.cards.length;
        this.updateDisplay();
        this.updateCrudButtons();
    }

    updateDisplay() {
        // Mostrar card atual + preview dos adjacentes com animação fluida
        this.cards.forEach((card, index) => {
            // Calcular offset do card em relação ao card atual
            let offset = index - this.currentIndex;

            // Ajustar offset para loop circular
            if (offset > this.cards.length / 2) {
                offset -= this.cards.length;
            } else if (offset < -(this.cards.length / 2)) {
                offset += this.cards.length;
            }

            let newOpacity, newZIndex, newTransform;

            if (offset === 0) {
                // Card atual - no centro, visível
                newOpacity = '1';
                newZIndex = '20';
                newTransform = 'scale(1) translateX(0)';
            } else if (offset === 1) {
                // Card próximo - à direita, semi-visível
                newOpacity = '0.6';
                newZIndex = '10';
                newTransform = 'scale(0.9) translateX(250px)';
            } else if (offset === -1) {
                // Card anterior - à esquerda, semi-visível
                newOpacity = '0.6';
                newZIndex = '10';
                newTransform = 'scale(0.9) translateX(-250px)';
            } else {
                // Cards muito distantes - invisíveis
                newOpacity = '0';
                newZIndex = '-1';
                newTransform = 'scale(0.9) translateX(0)';
                card.style.pointerEvents = 'none';
            }

            // Aplicar estilos
            card.style.opacity = newOpacity;
            card.style.zIndex = newZIndex;
            card.style.transform = newTransform;
        });

        // Atualizar indicadores (dots) - mostram qual conta está selecionada
        this.indicators.forEach((indicator, index) => {
            if (index === this.currentIndex) {
                // Indicador ativo (conta selecionada)
                indicator.classList.remove('bg-gray-400');
                indicator.classList.add('bg-gray-900', 'w-3', 'h-3');
            } else {
                // Indicador inativo
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

    getCurrentAccountName() {
        if (this.cards.length > this.currentIndex) {
            const card = this.cards[this.currentIndex];
            const nameElement = card.querySelector('[data-conta-nome]');
            if (nameElement) {
                return nameElement.getAttribute('data-conta-nome');
            }
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

        // Ativar botões se há conta
        if (contaId) {
            document.querySelectorAll('.viewAccountBtn, .editAccountBtn, .deleteAccountBtn').forEach(btn => {
                btn.removeAttribute('disabled');
                btn.classList.remove('btn-disabled', 'opacity-50', 'border-gray-300', 'text-gray-400');
                btn.classList.add('btn-ghost', 'border-gray-600', 'hover:border-black', 'hover:bg-white', 'hover:text-black', 'text-gray-600');
            });
        }
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
                const contaNome = this.getCurrentAccountName();
                if (contaId && modalManager) {
                    modalManager.openDeleteAccountModal(contaId, contaNome);
                }
            });
        }

        // Botão Adicionar (pode haver múltiplos - um na seção vazia e outro na grid)
        const addBtns = document.querySelectorAll('.addAccountBtn');
        addBtns.forEach(addBtn => {
            addBtn.addEventListener('click', (e) => {
                e.preventDefault();
                if (modalManager) {
                    modalManager.openAddAccountModal();
                }
            });
        });
    }
}

// Inicializar carousel quando o documento carregar
let carouselManager;
document.addEventListener('DOMContentLoaded', function () {
    carouselManager = new CarouselManager();
});
