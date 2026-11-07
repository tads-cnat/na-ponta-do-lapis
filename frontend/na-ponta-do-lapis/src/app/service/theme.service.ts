import { Injectable, computed, effect, signal } from '@angular/core';

export type TemaApp = 'claro' | 'escuro';

const CHAVE_STORAGE = 'npl-tema';
const CLASSE_ESCURO = 'app-dark';

/** Paleta usada pelos gráficos (Chart.js não enxerga CSS variables). */
export interface PaletaGrafico {
  texto: string;
  textoSuave: string;
  grade: string;
  superficie: string;
  borda: string;
  tooltipTitulo: string;
  tooltipCorpo: string;
}

const PALETA_CLARA: PaletaGrafico = {
  texto: '#475569',
  textoSuave: '#94a3b8',
  grade: 'rgba(226,232,240,0.7)',
  superficie: '#ffffff',
  borda: '#e2e8f0',
  tooltipTitulo: '#0f172a',
  tooltipCorpo: '#64748b',
};

const PALETA_ESCURA: PaletaGrafico = {
  texto: '#d4d4d8',
  textoSuave: '#a1a1aa',
  grade: 'rgba(63,63,70,0.6)',
  superficie: '#18181b',
  borda: '#3f3f46',
  tooltipTitulo: '#fafafa',
  tooltipCorpo: '#a1a1aa',
};

/**
 * Estado global da "cor da aplicação".
 *
 * - `preferencia` é o que o usuário escolheu (persistido no localStorage).
 * - O tema escuro só é aplicado enquanto a área interna (MainLayout) está
 *   ativa; landing, login e cadastro continuam claros.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly preferencia = signal<TemaApp>(this.lerPreferencia());
  private readonly areaInternaAtiva = signal(false);

  /** true quando o tema escuro está efetivamente aplicado. */
  readonly escuro = computed(() => this.areaInternaAtiva() && this.preferencia() === 'escuro');

  /** Preferência do usuário, independente de a área interna estar ativa. */
  readonly tema = this.preferencia.asReadonly();

  readonly paletaGrafico = computed<PaletaGrafico>(() =>
    this.escuro() ? PALETA_ESCURA : PALETA_CLARA,
  );

  constructor() {
    effect(() => {
      document.documentElement.classList.toggle(CLASSE_ESCURO, this.escuro());
    });
  }

  alternar(): void {
    this.definir(this.preferencia() === 'escuro' ? 'claro' : 'escuro');
  }

  definir(tema: TemaApp): void {
    this.preferencia.set(tema);
    try {
      localStorage.setItem(CHAVE_STORAGE, tema);
    } catch {
      /* storage indisponível (modo privado, etc.): mantém só em memória */
    }
  }

  /** Chamado pelo MainLayout ao entrar na área interna. */
  ativarAreaInterna(): void {
    this.areaInternaAtiva.set(true);
  }

  /** Chamado pelo MainLayout ao sair da área interna. */
  desativarAreaInterna(): void {
    this.areaInternaAtiva.set(false);
  }

  private lerPreferencia(): TemaApp {
    try {
      return localStorage.getItem(CHAVE_STORAGE) === 'escuro' ? 'escuro' : 'claro';
    } catch {
      return 'claro';
    }
  }
}
