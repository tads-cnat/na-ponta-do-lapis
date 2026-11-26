import { CommonModule } from '@angular/common';
import {
  Component,
  ElementRef,
  HostListener,
  Injector,
  OnDestroy,
  ViewChild,
  afterNextRender,
  effect,
  inject,
  signal,
  untracked,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PrimeNGModuleModule } from '../../primeNg.module';
import { MascoteEstado, MascoteLoginComponent } from '../../../auth/components/mascote-login/mascote-login.component';
import { IntegracoesService } from '../../../service/integracoes.service';
import { IChaveIaResponse } from '../../../model/IIntegracoes.models';
import { ConsultorService, ConsultorStatus, MensagemConsultor } from '../../service/consultor.service';
import { GuiaCondutorService } from '../../service/guia-condutor.service';

const CHAT_STORAGE_KEY = 'npl_grafith_consultor_chat_v1';

const MENSAGEM_BOAS_VINDAS: MensagemConsultor = {
  role: 'assistant',
  content:
    'Olá! Eu sou o **Grafith**, seu consultor financeiro no Na Ponta do Lápis. Posso analisar suas transações, metas, assinaturas e parcelamentos em aberto. O que você gostaria de saber hoje?',
};

@Component({
  selector: 'app-consultor-ia',
  standalone: true,
  imports: [CommonModule, FormsModule, PrimeNGModuleModule, MascoteLoginComponent],
  template: `
    <!-- Mini foto circular do Grafith flutuando embaixo com destaque leve da marca -->
    @if (consultorService.exibirBotaoFlutuante() && !guiaCondutor.ativo()) {
    <button
      #fabGrafith
      type="button"
      aria-label="Abrir Consultor Grafith IA"
      pTooltip="Perguntar ao Grafith"
      tooltipPosition="left"
      (click)="consultorService.abrir()"
      [class.grafith-fab-oculto]="fabOculto()"
      class="grafith-fab fixed right-5 md:right-7 bottom-20 md:bottom-6 z-40 w-16 h-16 rounded-full bg-gradient-to-br from-[#F6EEFF] to-white border-2 border-[#7843CD] ring-4 ring-[#7843CD]/15 hover:ring-[#7843CD]/25 shadow-[0_8px_24px_rgba(120,67,205,0.28)] hover:shadow-[0_12px_28px_rgba(120,67,205,0.38)] hover:scale-105 flex items-center justify-center cursor-pointer p-0">
      <span class="w-full h-full rounded-full flex items-center justify-center overflow-hidden">
        <span class="scale-[0.9] translate-y-0.5 origin-center pointer-events-none">
          <app-mascote-login [compact]="true" estado="idle" />
        </span>
      </span>
      <span
        aria-hidden="true"
        class="absolute -top-0.5 -right-0.5 w-5 h-5 rounded-full bg-gradient-to-br from-[#EEC046] to-[#F59E0B] text-slate-950 flex items-center justify-center ring-2 ring-white shadow-sm">
        <i class="pi pi-sparkles text-[10px]"></i>
      </span>
    </button>
    }

    <!-- Modal customizado com Container Transform estilo iOS (expande e recolhe a partir do botão flutuante) -->
    @if (renderizarModal()) {
    <div
      class="fixed inset-0 z-[1100] flex items-center justify-center p-3 md:p-6"
      [class.pointer-events-none]="!consultorService.aberto()">
      <!-- Backdrop com blur sutil -->
      <div
        #backdropGrafith
        (click)="consultorService.fechar()"
        [class.opacity-0]="modalPreparando()"
        class="fixed inset-0 bg-slate-950/45 backdrop-blur-[2px]"></div>

      <!-- Janela do chat (morphing container) -->
      <div
        #modalGrafith
        role="dialog"
        aria-modal="true"
        aria-label="Grafith - Consultor Financeiro"
        [class.opacity-0]="modalPreparando()"
        style="width: min(92vw, 760px); height: min(85dvh, 680px); border-radius: 22px;"
        class="grafith-modal relative z-10 flex flex-col bg-white border border-slate-200/80 shadow-[0_24px_64px_rgba(15,23,42,0.28)] overflow-hidden">
        <div #conteudoGrafith class="flex flex-col flex-1 min-h-0 h-full w-full">
          <!-- Cabeçalho -->
          <div class="flex items-center justify-between gap-2 px-4 py-3.5 border-b border-slate-100 bg-white shrink-0">
            <div class="flex items-center gap-3 min-w-0">
              <div class="w-12 h-12 rounded-full bg-gradient-to-br from-[#F6EEFF] to-white border-2 border-[#7843CD]/40 flex items-center justify-center overflow-hidden shrink-0">
                <span class="scale-[0.74] translate-y-0.5 origin-center pointer-events-none">
                  <app-mascote-login [compact]="true" [eager]="saudando()" [estado]="estadoGrafith()" />
                </span>
              </div>
              <div class="min-w-0">
                <div class="font-extrabold text-base text-slate-900 truncate">Grafith • Consultor Financeiro</div>
                <div class="text-xs text-slate-500 truncate">Somente leitura • Análises baseadas nos seus dados reais</div>
              </div>
            </div>

            <div class="flex items-center gap-1 shrink-0">
              <button
                type="button"
                pTooltip="Configurações de IA"
                tooltipPosition="bottom"
                aria-label="Configurações de IA"
                (click)="mostrarConfigChave.set(!mostrarConfigChave())"
                [class]="mostrarConfigChave()
                  ? 'w-8 h-8 rounded-lg flex items-center justify-center text-[#7843CD] bg-purple-50 border-0 cursor-pointer'
                  : 'w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-700 hover:bg-slate-100 border-0 bg-transparent cursor-pointer'">
                <i class="pi pi-cog text-xs"></i>
              </button>

              @if (mensagens().length > 1) {
                <button
                  type="button"
                  pTooltip="Iniciar nova conversa"
                  tooltipPosition="bottom"
                  aria-label="Limpar conversa"
                  (click)="limparConversa()"
                  class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-rose-50 border-0 bg-transparent cursor-pointer">
                  <i class="pi pi-trash text-xs"></i>
                </button>
              }

              <button
                type="button"
                aria-label="Fechar consultor"
                (click)="consultorService.fechar()"
                class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-700 hover:bg-slate-100 border-0 bg-transparent cursor-pointer">
                <i class="pi pi-times text-xs"></i>
              </button>
            </div>
          </div>

          <!-- Painel de configuração de chave de IA (visível ao clicar na engrenagem) -->
          @if (mostrarConfigChave()) {
            <div class="mx-4 mt-2.5 flex flex-col gap-2 px-3 py-2.5 rounded-xl bg-slate-50 border border-slate-200/80 text-xs shrink-0">
              @if (chavesSalvas().length > 0) {
                <div class="flex items-center justify-between gap-2">
                  <span class="text-slate-600 font-semibold shrink-0">Chave IA ativa:</span>
                  <p-select
                    [(ngModel)]="chaveSelecionadaId"
                    [options]="chavesSalvas()"
                    optionLabel="apelido"
                    optionValue="id"
                    placeholder="Automática (padrão)"
                    [showClear]="true"
                    appendTo="body"
                    [overlayOptions]="{ baseZIndex: 1250 }"
                    styleClass="flex-1 text-xs" />
                </div>
              } @else {
                <div class="text-slate-600">
                  Nenhuma chave de IA salva ainda. Cadastre uma no seu perfil ou informe uma chave temporária abaixo.
                </div>
              }
              <div class="flex items-center justify-end">
                <button
                  type="button"
                  (click)="irParaConfiguracoesIa()"
                  class="inline-flex items-center gap-1.5 text-xs font-semibold text-[#7843CD] hover:text-[#6214CC] hover:underline bg-transparent border-0 p-0 cursor-pointer">
                  <i class="pi pi-sliders-h text-[11px]"></i>
                  Gerenciar chaves em Perfil &gt; IA
                </button>
              </div>
            </div>
          }

          <!-- Configuração rápida de chave avulsa somente quando não há nenhuma chave configurada -->
          @if (pedirChave()) {
            <div class="mx-4 mt-2.5 rounded-xl bg-amber-50 border border-amber-200 p-3 text-xs text-amber-950 flex flex-col gap-2 shrink-0">
              <div class="font-bold">Informe uma chave de IA para conversar com o Grafith</div>
              <p class="m-0 text-amber-800">
                Você pode cadastrar suas chaves no Perfil ou informar uma temporária abaixo para esta sessão.
              </p>
              <div class="flex gap-2">
                <p-select
                  [(ngModel)]="provedorAvulso"
                  [options]="provedores"
                  optionLabel="label"
                  optionValue="value"
                  appendTo="body"
                  [overlayOptions]="{ baseZIndex: 1250 }"
                  styleClass="w-32" />
                <input
                  type="password"
                  pInputText
                  [(ngModel)]="apiKeyAvulsa"
                  placeholder="Cole sua API Key"
                  class="flex-1 text-xs" />
              </div>
            </div>
          }

          <!-- Sugestões rápidas -->
          @if (mensagens().length <= 1) {
            <div class="px-4 pt-2.5 pb-1 flex flex-wrap gap-1.5 shrink-0">
              @for (sug of sugestoes; track sug) {
                <button
                  type="button"
                  (click)="enviarSugestao(sug)"
                  [disabled]="carregando()"
                  class="text-left text-xs px-3 py-1.5 rounded-full bg-purple-50 text-[#7843CD] hover:bg-purple-100 border border-purple-200/70 cursor-pointer font-medium transition-colors">
                  {{ sug }}
                </button>
              }
            </div>
          }

          <!-- Área de mensagens (barra de rolagem encostada na borda direita do modal) -->
          <div #scrollContainer class="grafith-scroll flex-1 min-h-0 overflow-y-auto px-4 py-3 flex flex-col gap-3">
            @for (msg of mensagens(); track $index) {
              <div [class]="msg.role === 'user' ? 'flex justify-end' : 'flex justify-start'">
                @if (msg.role === 'user') {
                  <div class="max-w-[85%] rounded-2xl rounded-br-sm px-3.5 py-2.5 text-sm bg-[#7843CD] text-white whitespace-pre-wrap">
                    {{ msg.content }}
                  </div>
                } @else {
                  <div class="max-w-[92%] rounded-2xl rounded-bl-sm px-3.5 py-2.5 text-sm bg-slate-100 text-slate-800 leading-relaxed">
                    <div class="grafith-md" [innerHTML]="formatarMarkdown(indiceDigitando() === $index ? textoDigitadoChat() : msg.content)"></div>
                    @if (indiceDigitando() === $index) {
                      <span class="inline-block w-1.5 h-3.5 mt-0.5 align-middle bg-[#7843CD] animate-pulse"></span>
                    }
                    @if ($index > 0 && indiceDigitando() !== $index) {
                      <div class="flex items-center justify-between gap-3 mt-2 pt-1.5 border-t border-slate-200/70">
                        <span class="text-[10px] text-slate-400">
                          {{ msg.provedorUsado ? 'Grafith via ' + msg.provedorUsado : 'Grafith IA' }}
                        </span>
                        <div class="flex items-center gap-1">
                          <button
                            type="button"
                            aria-label="Resposta útil"
                            pTooltip="Útil"
                            tooltipPosition="top"
                            (click)="avaliarMensagem($index, true)"
                            [class]="msg.feedbackUtil === true
                              ? 'w-6 h-6 rounded-md flex items-center justify-center bg-emerald-100 text-emerald-700 border-0 cursor-pointer'
                              : 'w-6 h-6 rounded-md flex items-center justify-center bg-transparent text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 border-0 cursor-pointer'">
                            <i class="pi text-[11px]" [class.pi-thumbs-up-fill]="msg.feedbackUtil === true" [class.pi-thumbs-up]="msg.feedbackUtil !== true"></i>
                          </button>
                          <button
                            type="button"
                            aria-label="Resposta não ajudou"
                            pTooltip="Não ajudou"
                            tooltipPosition="top"
                            (click)="avaliarMensagem($index, false)"
                            [class]="msg.feedbackUtil === false
                              ? 'w-6 h-6 rounded-md flex items-center justify-center bg-rose-100 text-rose-700 border-0 cursor-pointer'
                              : 'w-6 h-6 rounded-md flex items-center justify-center bg-transparent text-slate-400 hover:text-rose-600 hover:bg-rose-50 border-0 cursor-pointer'">
                            <i class="pi text-[11px]" [class.pi-thumbs-down-fill]="msg.feedbackUtil === false" [class.pi-thumbs-down]="msg.feedbackUtil !== false"></i>
                          </button>
                        </div>
                      </div>
                    }
                  </div>
                }
              </div>
            }
            @if (carregando()) {
              <div class="flex justify-start">
                <div class="rounded-2xl px-3.5 py-2.5 text-xs bg-slate-100 text-slate-500 flex items-center gap-2">
                  <i class="pi pi-spin pi-spinner text-[#7843CD]"></i>
                  <span>Grafith está analisando seus dados...</span>
                </div>
              </div>
            }
          </div>

          <!-- Input de pergunta -->
          <form (ngSubmit)="enviar()" class="flex items-center gap-2 px-4 py-3 border-t border-slate-100 bg-white shrink-0">
            <input
              type="text"
              pInputText
              [(ngModel)]="perguntaAtual"
              name="perguntaAtual"
              [disabled]="carregando()"
              maxlength="1000"
              placeholder="Pergunte sobre gastos, metas, assinaturas ou parcelas..."
              class="flex-1 text-sm" />
            <button
              type="submit"
              aria-label="Enviar pergunta"
              [disabled]="!perguntaAtual.trim() || carregando()"
              class="btn-npl-primary text-white w-10 h-10 rounded-xl flex items-center justify-center border-0 cursor-pointer disabled:opacity-50 shrink-0">
              <i class="pi pi-send text-sm"></i>
            </button>
          </form>
        </div>
      </div>
    </div>
    }
  `,
  styles: [`
    .grafith-fab {
      transition: transform 200ms cubic-bezier(0.22, 1, 0.36, 1),
                  box-shadow 200ms ease;
    }
    .grafith-fab-oculto {
      opacity: 0 !important;
      pointer-events: none !important;
      transform: none !important;
      transition: none !important;
    }
    .grafith-modal {
      transform-origin: center center;
      will-change: transform, border-radius, opacity;
    }
    .grafith-scroll {
      scrollbar-width: thin;
      scrollbar-color: #cbd5e1 transparent;
    }
    .grafith-scroll::-webkit-scrollbar {
      width: 6px;
    }
    .grafith-scroll::-webkit-scrollbar-track {
      background: transparent;
    }
    .grafith-scroll::-webkit-scrollbar-thumb {
      background-color: #cbd5e1;
      border-radius: 9999px;
    }
    .grafith-scroll::-webkit-scrollbar-thumb:hover {
      background-color: #94a3b8;
    }
    :host ::ng-deep .grafith-md ul {
      margin: 0.35rem 0;
      padding-left: 1.15rem;
      list-style-type: disc;
    }
    :host ::ng-deep .grafith-md li {
      margin: 0.15rem 0;
    }
    :host ::ng-deep .grafith-md p {
      margin: 0.25rem 0;
    }
    :host ::ng-deep .grafith-md p:first-child {
      margin-top: 0;
    }
    :host ::ng-deep .grafith-md p:last-child {
      margin-bottom: 0;
    }
  `],
})
export class ConsultorIaComponent implements OnDestroy {
  public consultorService = inject(ConsultorService);
  private integracoesService = inject(IntegracoesService);
  readonly guiaCondutor = inject(GuiaCondutorService);
  private injector = inject(Injector);
  private router = inject(Router);

  @ViewChild('scrollContainer') private scrollContainer?: ElementRef<HTMLDivElement>;
  @ViewChild('fabGrafith') private fabGrafith?: ElementRef<HTMLButtonElement>;
  @ViewChild('modalGrafith') private modalGrafith?: ElementRef<HTMLDivElement>;
  @ViewChild('backdropGrafith') private backdropGrafith?: ElementRef<HTMLDivElement>;
  @ViewChild('conteudoGrafith') private conteudoGrafith?: ElementRef<HTMLDivElement>;

  private animacoesAtivas: Animation[] = [];
  private cicloAnimacao = 0;
  private digitacaoTimer: ReturnType<typeof setInterval> | null = null;

  perguntaAtual = '';
  provedorAvulso = 'GEMINI';
  apiKeyAvulsa = '';
  chaveSelecionadaId: number | null = null;

  renderizarModal = signal(false);
  modalPreparando = signal(false);
  fabOculto = signal(false);

  carregando = signal(false);
  pedirChave = signal(false);
  mostrarConfigChave = signal(false);
  saudando = signal(false);
  estadoGrafith = signal<MascoteEstado>('idle');
  status = signal<ConsultorStatus | null>(null);
  chavesSalvas = signal<IChaveIaResponse[]>([]);
  mensagens = signal<MensagemConsultor[]>(this.carregarMensagensSalvas());
  indiceDigitando = signal<number | null>(null);
  textoDigitadoChat = signal('');

  readonly sugestoes = [
    'Onde gastei mais este mês?',
    'Como está o progresso das minhas metas?',
    'Quanto gasto por mês com assinaturas?',
    'Quando termino de pagar minhas parcelas?',
  ];

  readonly provedores = [
    { label: 'Gemini', value: 'GEMINI' },
    { label: 'Claude', value: 'CLAUDE' },
    { label: 'OpenAI', value: 'OPENAI' },
    { label: 'DeepSeek', value: 'DEEPSEEK' },
    { label: 'Groq', value: 'GROQ' },
  ];

  constructor() {
    effect(() => {
      const aberto = this.consultorService.aberto();
      untracked(() => {
        if (aberto) {
          this.iniciarAbertura();
        } else {
          this.iniciarFechamento();
        }
      });
    });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.consultorService.aberto()) {
      this.consultorService.fechar();
    }
  }

  ngOnDestroy(): void {
    this.cicloAnimacao++;
    this.cancelarAnimacoes();
    this.pararDigitacao();
  }

  private iniciarAbertura(): void {
    const ciclo = ++this.cicloAnimacao;
    this.cancelarAnimacoes();

    this.carregarContextoInicial();
    this.reagirAbertura();

    const pendente = this.consultorService.consumirPerguntaPendente();
    if (pendente) {
      setTimeout(() => this.enviarSugestao(pendente), 180);
    }

    if (!this.renderizarModal()) {
      this.modalPreparando.set(true);
      this.renderizarModal.set(true);
      afterNextRender(
        () => this.executarAnimacaoAbertura(ciclo),
        { injector: this.injector }
      );
    } else {
      // Usuário reabriu durante a animação de fechamento
      this.executarAnimacaoAbertura(ciclo);
    }
  }

  private executarAnimacaoAbertura(ciclo: number): void {
    if (ciclo !== this.cicloAnimacao || !this.consultorService.aberto()) return;

    const modalEl = this.modalGrafith?.nativeElement;
    const backdropEl = this.backdropGrafith?.nativeElement;
    const conteudoEl = this.conteudoGrafith?.nativeElement;

    if (!modalEl || !backdropEl || !conteudoEl) {
      requestAnimationFrame(() => {
        if (ciclo === this.cicloAnimacao && this.consultorService.aberto()) {
          this.modalPreparando.set(false);
          this.fabOculto.set(true);
        }
      });
      return;
    }

    this.cancelarAnimacoes();
    this.modalPreparando.set(false);
    this.fabOculto.set(true);

    const fabEl = this.fabGrafith?.nativeElement;
    const modalRect = modalEl.getBoundingClientRect();

    const animBackdrop = backdropEl.animate(
      [{ opacity: 0 }, { opacity: 1 }],
      { duration: 360, easing: 'cubic-bezier(0.32, 0.72, 0, 1)', fill: 'both' }
    );

    let animModal: Animation;

    if (fabEl && modalRect.width > 0 && modalRect.height > 0) {
      const fabRect = fabEl.getBoundingClientRect();
      const dx = fabRect.left + fabRect.width / 2 - (modalRect.left + modalRect.width / 2);
      const dy = fabRect.top + fabRect.height / 2 - (modalRect.top + modalRect.height / 2);
      const sx = Math.max(0.05, fabRect.width / modalRect.width);
      const sy = Math.max(0.05, fabRect.height / modalRect.height);
      const rx = Math.round(modalRect.width / 2);
      const ry = Math.round(modalRect.height / 2);

      animModal = modalEl.animate(
        [
          {
            offset: 0,
            transform: `translate3d(${dx}px, ${dy}px, 0) scale(${sx}, ${sy})`,
            borderRadius: `${rx}px / ${ry}px`,
            backgroundColor: '#F3E8FF',
            boxShadow: 'inset 0 0 0 24px rgba(120, 67, 205, 0.92), 0 10px 28px rgba(120, 67, 205, 0.35)',
            opacity: 0.96,
          },
          {
            offset: 0.42,
            backgroundColor: '#FFFFFF',
            opacity: 1,
          },
          {
            offset: 1,
            transform: 'translate3d(0, 0, 0) scale(1, 1)',
            borderRadius: '22px / 22px',
            backgroundColor: '#FFFFFF',
            boxShadow: 'inset 0 0 0 0px rgba(120, 67, 205, 0), 0 24px 64px rgba(15, 23, 42, 0.28)',
            opacity: 1,
          },
        ],
        {
          duration: 440,
          easing: 'cubic-bezier(0.32, 0.72, 0, 1)',
          fill: 'both',
        }
      );
    } else {
      animModal = modalEl.animate(
        [
          {
            transform: 'translate3d(0, 32px, 0) scale(0.92)',
            borderRadius: '26px',
            opacity: 0,
          },
          {
            transform: 'translate3d(0, 0, 0) scale(1)',
            borderRadius: '22px',
            opacity: 1,
          },
        ],
        {
          duration: 340,
          easing: 'cubic-bezier(0.32, 0.72, 0, 1)',
          fill: 'both',
        }
      );
    }

    const animConteudo = conteudoEl.animate(
      [
        { offset: 0, opacity: 0, transform: 'translate3d(0, 10px, 0)' },
        { offset: 0.3, opacity: 0, transform: 'translate3d(0, 10px, 0)' },
        { offset: 1, opacity: 1, transform: 'translate3d(0, 0, 0)' },
      ],
      {
        duration: 420,
        easing: 'cubic-bezier(0.22, 1, 0.36, 1)',
        fill: 'both',
      }
    );

    this.animacoesAtivas = [animBackdrop, animModal, animConteudo];

    animModal.onfinish = () => {
      if (ciclo !== this.cicloAnimacao) return;
      this.cancelarAnimacoes();
    };
  }

  private iniciarFechamento(): void {
    if (!this.renderizarModal()) return;

    const ciclo = ++this.cicloAnimacao;
    const modalEl = this.modalGrafith?.nativeElement;
    const backdropEl = this.backdropGrafith?.nativeElement;
    const conteudoEl = this.conteudoGrafith?.nativeElement;

    if (!modalEl || !backdropEl || !conteudoEl) {
      this.finalizarFechamento(ciclo);
      return;
    }

    this.cancelarAnimacoes();
    const fabEl = this.fabGrafith?.nativeElement;
    const modalRect = modalEl.getBoundingClientRect();

    const animBackdrop = backdropEl.animate(
      [{ opacity: 1 }, { opacity: 0 }],
      { duration: 300, easing: 'ease-out', fill: 'forwards' }
    );

    const animConteudo = conteudoEl.animate(
      [{ opacity: 1 }, { opacity: 0 }],
      { duration: 150, easing: 'ease-in', fill: 'forwards' }
    );

    let animModal: Animation;

    if (fabEl && modalRect.width > 0 && modalRect.height > 0) {
      const fabRect = fabEl.getBoundingClientRect();
      const dx = fabRect.left + fabRect.width / 2 - (modalRect.left + modalRect.width / 2);
      const dy = fabRect.top + fabRect.height / 2 - (modalRect.top + modalRect.height / 2);
      const sx = Math.max(0.05, fabRect.width / modalRect.width);
      const sy = Math.max(0.05, fabRect.height / modalRect.height);
      const rx = Math.round(modalRect.width / 2);
      const ry = Math.round(modalRect.height / 2);

      animModal = modalEl.animate(
        [
          {
            offset: 0,
            transform: 'translate3d(0, 0, 0) scale(1, 1)',
            borderRadius: '22px / 22px',
            backgroundColor: '#FFFFFF',
            boxShadow: 'inset 0 0 0 0px rgba(120, 67, 205, 0), 0 24px 64px rgba(15, 23, 42, 0.28)',
            opacity: 1,
          },
          {
            offset: 0.58,
            backgroundColor: '#F3E8FF',
            opacity: 1,
          },
          {
            offset: 1,
            transform: `translate3d(${dx}px, ${dy}px, 0) scale(${sx}, ${sy})`,
            borderRadius: `${rx}px / ${ry}px`,
            backgroundColor: '#F3E8FF',
            boxShadow: 'inset 0 0 0 24px rgba(120, 67, 205, 0.92), 0 10px 28px rgba(120, 67, 205, 0.35)',
            opacity: 0.94,
          },
        ],
        {
          duration: 360,
          easing: 'cubic-bezier(0.32, 0.72, 0, 1)',
          fill: 'forwards',
        }
      );
    } else {
      animModal = modalEl.animate(
        [
          { transform: 'translate3d(0, 0, 0) scale(1)', opacity: 1 },
          { transform: 'translate3d(0, 28px, 0) scale(0.92)', opacity: 0 },
        ],
        {
          duration: 260,
          easing: 'cubic-bezier(0.32, 0.72, 0, 1)',
          fill: 'forwards',
        }
      );
    }

    this.animacoesAtivas = [animBackdrop, animModal, animConteudo];

    animModal.onfinish = () => {
      if (ciclo !== this.cicloAnimacao) return;
      this.finalizarFechamento(ciclo);
    };
  }

  private finalizarFechamento(ciclo: number): void {
    if (ciclo !== this.cicloAnimacao) return;
    this.cancelarAnimacoes();
    this.renderizarModal.set(false);
    this.modalPreparando.set(false);
    this.fabOculto.set(false);

    const fabEl = this.fabGrafith?.nativeElement;
    if (fabEl) {
      fabEl.animate(
        [
          { transform: 'scale(0.92)' },
          { transform: 'scale(1.04)', offset: 0.55 },
          { transform: 'scale(1)' },
        ],
        { duration: 220, easing: 'cubic-bezier(0.22, 1, 0.36, 1)' }
      );
    }
  }

  private cancelarAnimacoes(): void {
    for (const anim of this.animacoesAtivas) {
      try {
        anim.cancel();
      } catch {}
    }
    this.animacoesAtivas = [];
  }

  private reagirAbertura(): void {
    if (this.carregando()) return;
    this.saudando.set(true);
    this.estadoGrafith.set('success');
    setTimeout(() => {
      this.saudando.set(false);
      if (!this.carregando()) {
        this.estadoGrafith.set('idle');
      }
    }, 950);
  }

  limparConversa(): void {
    this.pararDigitacao();
    this.mensagens.set([MENSAGEM_BOAS_VINDAS]);
    this.salvarMensagens([MENSAGEM_BOAS_VINDAS]);
    this.estadoGrafith.set('idle');
  }

  enviarSugestao(texto: string): void {
    this.perguntaAtual = texto;
    this.enviar();
  }

  enviar(): void {
    const pergunta = this.perguntaAtual.trim();
    if (!pergunta || this.carregando()) return;

    const historico = this.mensagens()
      .slice(-8)
      .map((m) => ({ role: m.role, content: m.content }));

    this.adicionarMensagem({ role: 'user', content: pergunta });
    this.perguntaAtual = '';
    this.carregando.set(true);
    this.estadoGrafith.set('loading');
    this.rolarParaFim();

    this.consultorService
      .perguntar({
        pergunta,
        historico,
        chaveIaId: this.chaveSelecionadaId ?? undefined,
        provedor: !this.chaveSelecionadaId && this.apiKeyAvulsa.trim() ? this.provedorAvulso : undefined,
        apiKey: !this.chaveSelecionadaId && this.apiKeyAvulsa.trim() ? this.apiKeyAvulsa.trim() : undefined,
        manterNaSessao: !this.chaveSelecionadaId && !!this.apiKeyAvulsa.trim(),
      })
      .subscribe({
        next: (res) => {
          this.carregando.set(false);
          this.pedirChave.set(false);
          this.estadoGrafith.set('success');
          this.adicionarMensagem({
            role: 'assistant',
            content: res.resposta,
            provedorUsado: res.provedorUsado,
            perguntaOrigem: pergunta,
          });
          this.iniciarDigitacaoResposta(this.mensagens().length - 1, res.resposta);
          setTimeout(() => {
            if (!this.carregando()) this.estadoGrafith.set('idle');
          }, 2200);
        },
        error: (err) => {
          this.carregando.set(false);
          this.estadoGrafith.set('error');
          if (err?.status === 428) {
            this.pedirChave.set(true);
          }
          const msgErro =
            err?.error?.mensagem ||
            err?.error?.error ||
            'Não consegui responder agora. Verifique sua chave de IA ou tente novamente em instantes.';
          this.adicionarMensagem({ role: 'assistant', content: msgErro });
          this.rolarParaFim();
        },
      });
  }

  avaliarMensagem(indice: number, util: boolean): void {
    const lista = this.mensagens();
    const alvo = lista[indice];
    if (!alvo || alvo.role !== 'assistant') return;

    this.mensagens.update((msgs) => {
      const copia = msgs.map((m, i) => (i === indice ? { ...m, feedbackUtil: util } : m));
      this.salvarMensagens(copia);
      return copia;
    });

    this.consultorService
      .enviarFeedback({
        feedbackId: alvo.feedbackId,
        util,
        provedor: alvo.provedorUsado,
        perguntaResumo: alvo.perguntaOrigem,
        respostaResumo: alvo.content,
      })
      .subscribe({
        next: (res) => {
          this.mensagens.update((msgs) => {
            const copia = msgs.map((m, i) =>
              i === indice ? { ...m, feedbackId: res.id, feedbackUtil: res.util } : m
            );
            this.salvarMensagens(copia);
            return copia;
          });
        },
        error: () => {},
      });
  }

  formatarMarkdown(texto: string): string {
    const escaped = (texto || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

    const comNegrito = escaped.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
    const linhas = comNegrito.split('\n');
    const saida: string[] = [];
    let emLista = false;

    for (const linha of linhas) {
      const trim = linha.trim();
      const itemLista = /^[-*•]\s+(.+)$/.exec(trim);
      if (itemLista) {
        if (!emLista) {
          saida.push('<ul>');
          emLista = true;
        }
        saida.push(`<li>${itemLista[1]}</li>`);
      } else {
        if (emLista) {
          saida.push('</ul>');
          emLista = false;
        }
        if (trim.length > 0) {
          saida.push(`<p>${trim}</p>`);
        }
      }
    }
    if (emLista) {
      saida.push('</ul>');
    }
    return saida.join('');
  }

  private carregarContextoInicial(): void {
    this.consultorService.status().subscribe({
      next: (st) => {
        this.status.set(st);
        this.pedirChave.set(!st.pronto);
      },
      error: () => {},
    });

    this.integracoesService.listarChavesIa().subscribe({
      next: (chaves) => {
        this.chavesSalvas.set(chaves || []);
        if (!this.chaveSelecionadaId && chaves?.length) {
          const padrao = chaves.find((c) => c.padrao);
          if (padrao) {
            this.chaveSelecionadaId = padrao.id;
          }
        }
      },
      error: () => {},
    });

    this.rolarParaFim();
  }

  private adicionarMensagem(msg: MensagemConsultor): void {
    this.mensagens.update((lista) => {
      const nova = [...lista, msg];
      this.salvarMensagens(nova);
      return nova;
    });
  }

  irParaConfiguracoesIa(): void {
    this.mostrarConfigChave.set(false);
    this.consultorService.fechar();
    this.router.navigate(['/app/perfil'], { queryParams: { aba: 'ia' } });
  }

  private carregarMensagensSalvas(): MensagemConsultor[] {
    try {
      const raw = sessionStorage.getItem(CHAT_STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed) && parsed.length > 0) {
          return parsed;
        }
      }
    } catch {}
    return [MENSAGEM_BOAS_VINDAS];
  }

  private salvarMensagens(lista: MensagemConsultor[]): void {
    try {
      sessionStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify(lista.slice(-30)));
    } catch {}
  }

  private rolarParaFim(): void {
    setTimeout(() => {
      const el = this.scrollContainer?.nativeElement;
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    }, 60);
  }

  private iniciarDigitacaoResposta(indice: number, textoCompleto: string): void {
    this.pararDigitacao();
    if (
      typeof window !== 'undefined' &&
      window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
    ) {
      this.rolarParaFim();
      return;
    }

    let pos = 0;
    let ticks = 0;
    this.indiceDigitando.set(indice);
    this.textoDigitadoChat.set('');
    this.rolarParaFim();

    this.digitacaoTimer = setInterval(() => {
      pos += 2;
      ticks += 1;
      if (pos >= textoCompleto.length) {
        this.textoDigitadoChat.set(textoCompleto);
        this.pararDigitacao();
        this.rolarParaFim();
        return;
      }
      this.textoDigitadoChat.set(textoCompleto.slice(0, pos));
      if (ticks % 10 === 0) {
        this.rolarParaFim();
      }
    }, 14);
  }

  private pararDigitacao(): void {
    if (this.digitacaoTimer) {
      clearInterval(this.digitacaoTimer);
      this.digitacaoTimer = null;
    }
    this.indiceDigitando.set(null);
    this.textoDigitadoChat.set('');
  }
}
