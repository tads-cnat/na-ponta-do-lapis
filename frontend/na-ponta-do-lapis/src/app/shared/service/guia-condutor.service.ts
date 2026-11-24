import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/service/auth.service';
import { MascoteEstado } from '../../auth/components/mascote-login/mascote-login.component';
import { SidebarService } from '../../service/sidebar-service.service';
import { CapacitorService } from './capacitor.service';

export interface PassoGuiaCondutor {
  id: string;
  titulo: string;
  descricao: string;
  rota: string;
  target?: string;
  mascoteEstado: MascoteEstado;
}

@Injectable({
  providedIn: 'root',
})
export class GuiaCondutorService {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly sidebarService = inject(SidebarService);
  private readonly capacitorService = inject(CapacitorService);

  private onboardingPersistido = false;

  readonly passos: PassoGuiaCondutor[] = [
    {
      id: 'boas-vindas',
      titulo: 'Olá! Eu sou o Lápis 👋',
      descricao:
        'Bem-vindo ao Na Ponta do Lápis! Vou fazer um tour rápido para te mostrar onde controlar suas contas, lançamentos, metas e família.',
      rota: '/app/dashboard',
      mascoteEstado: 'success',
    },
    {
      id: 'dashboard',
      titulo: 'Dashboard — Visão Geral',
      descricao:
        'Aqui você acompanha seu saldo total, receitas, despesas do mês, gráficos por categoria e o resumo das suas metas.',
      rota: '/app/dashboard',
      target: 'menu-dashboard',
      mascoteEstado: 'checking',
    },
    {
      id: 'contas',
      titulo: 'Contas e Cartões',
      descricao:
        'Cadastre suas contas bancárias e cartões de crédito ou conecte pelo Open Finance para importar saldos e extratos.',
      rota: '/app/contas',
      target: 'menu-conta',
      mascoteEstado: 'idle',
    },
    {
      id: 'transacoes',
      titulo: 'Movimentações e IA',
      descricao:
        'Gerencie receitas, despesas e parcelamentos, filtre por marcadores e importe faturas em PDF com ajuda da Inteligência Artificial.',
      rota: '/app/transacoes',
      target: 'menu-transacao',
      mascoteEstado: 'checking',
    },
    {
      id: 'nova-transacao',
      titulo: 'Atalho: Nova Transação',
      descricao:
        'Sempre que quiser registrar um gasto ou recebimento na hora, use este botão de acesso rápido!',
      rota: '/app/transacoes',
      target: 'nova-transacao',
      mascoteEstado: 'success',
    },
    {
      id: 'metas',
      titulo: 'Metas Financeiras',
      descricao:
        'Crie objetivos com prazo e valor, faça aportes e receba conquistas conforme avança até bater sua meta.',
      rota: '/app/metas',
      target: 'menu-meta',
      mascoteEstado: 'success',
    },
    {
      id: 'perfil-familia',
      titulo: 'Família, Open Finance e Perfil',
      descricao:
        'Convide familiares em Família para compartilhar o orçamento e configure tema, moeda, Open Finance e IA no Perfil. Você pode rever este guia lá quando quiser!',
      rota: '/app/perfil',
      target: 'header-perfil',
      mascoteEstado: 'idle',
    },
  ];

  readonly ativo = signal(false);
  readonly indiceAtual = signal(0);
  readonly nomeUsuario = signal('');

  readonly passoAtual = computed(() => this.passos[this.indiceAtual()] ?? this.passos[0]);
  readonly totalPassos = this.passos.length;
  readonly ehUltimoPasso = computed(() => this.indiceAtual() >= this.passos.length - 1);

  marcarPersistido(jaConcluido: boolean): void {
    this.onboardingPersistido = jaConcluido;
  }

  iniciar(nome?: string | null): void {
    const primeiroNome = (nome ?? '').trim().split(/\s+/)[0] || '';
    if (primeiroNome) {
      this.nomeUsuario.set(primeiroNome);
    }
    this.indiceAtual.set(0);
    this.ativo.set(true);
    this.fecharMenuMobileSeAberto();
    void this.navegarParaPasso(0);
  }

  avancar(): void {
    if (this.ehUltimoPasso()) {
      this.concluir();
      return;
    }
    const proximo = this.indiceAtual() + 1;
    this.indiceAtual.set(proximo);
    this.capacitorService.vibrarImpactoLeve();
    void this.navegarParaPasso(proximo);
  }

  voltar(): void {
    if (this.indiceAtual() <= 0) return;
    const anterior = this.indiceAtual() - 1;
    this.indiceAtual.set(anterior);
    this.capacitorService.vibrarImpactoLeve();
    void this.navegarParaPasso(anterior);
  }

  irPara(indice: number): void {
    if (indice < 0 || indice >= this.passos.length) return;
    this.indiceAtual.set(indice);
    this.capacitorService.vibrarImpactoLeve();
    void this.navegarParaPasso(indice);
  }

  pular(): void {
    this.encerrar(false);
  }

  concluir(): void {
    this.encerrar(true);
  }

  private encerrar(comSucesso: boolean): void {
    this.ativo.set(false);
    if (comSucesso) {
      this.capacitorService.vibrarSucesso();
    }
    if (!this.onboardingPersistido) {
      this.onboardingPersistido = true;
      this.authService.concluirOnboarding().subscribe({
        error: () => {
          this.onboardingPersistido = false;
        },
      });
    }
  }

  private async navegarParaPasso(indice: number): Promise<void> {
    this.fecharMenuMobileSeAberto();
    const passo = this.passos[indice];
    if (!passo) return;
    if (!this.router.url.startsWith(passo.rota)) {
      await this.router.navigateByUrl(passo.rota);
    }
  }

  private fecharMenuMobileSeAberto(): void {
    if (typeof window !== 'undefined' && window.innerWidth < 769 && this.sidebarService.aberto()) {
      this.sidebarService.fecharSidebar();
    }
  }
}
