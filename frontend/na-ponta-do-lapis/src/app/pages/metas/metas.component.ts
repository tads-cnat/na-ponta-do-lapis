import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MetaItemComponent } from './components/meta-item/meta-item.component';
import { MetasService } from './services/metas.service';
import { ContaFinanceiraService } from '../contas/service/contas.service';
import { TransacoesService } from '../transacoes/service/transacoes.service';
import { AuthService } from '../../auth/service/auth.service';
import { MetaRequest, MetaResponse, TipoMetaResponse, AporteMetaRequest } from '@models/IMetas.models';

import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { ReactiveFormsModule, FormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext'; 
import { DatePicker } from 'primeng/datepicker';
import { Select } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { Textarea } from 'primeng/textarea';
import { CheckboxModule } from 'primeng/checkbox';
import { InputMoedaComponent } from '../../shared/components/input-moeda/input-moeda.component';
import { AppMoedaPipe } from '../../shared/pipes/app-moeda.pipe';
import { NotificacaoService } from '../../shared/service/notificacao.service';
import { CelebracaoService } from '../../shared/service/celebracao.service';

@Component({
  selector: 'app-metas',
  imports: [
    CommonModule,
    MetaItemComponent,
    Button,
    DialogModule,
    InputTextModule,
    ReactiveFormsModule,
    FormsModule,
    DatePicker,
    Select,
    Textarea,
    CheckboxModule,
    ToastModule,
    InputMoedaComponent,
    AppMoedaPipe,
  ],
  templateUrl: './metas.component.html',
  styleUrl: './metas.component.css',
})
export class MetasComponent implements OnInit {

  metas = signal<MetaResponse[]>([]);
  tiposMeta = signal<TipoMetaResponse[]>([]);
  contas = signal<any[]>([]);
  categorias = signal<any[]>([]);
  exibirDialog: boolean = false;
  exibirDialogAporte: boolean = false;
  exibirDialogExclusao: boolean = false;
  metaParaAporte: MetaResponse | null = null;
  metaParaExcluir: MetaResponse | null = null;
  
  // Novos Filtros Ricos: Status, Tipo e Origem
  filtroStatus = signal<'todas' | 'em_andamento' | 'concluidas' | 'expiradas'>('todas');
  filtroTipo = signal<'todos' | 'POUPANCA' | 'GASTO' | 'DIVIDA'>('todos');
  filtroOrigem = signal<'todas' | 'individuais' | 'familia'>('todas');
  
  opcoesFiltroTipo = [
    { label: 'Todos os tipos', value: 'todos' },
    { label: 'Juntar Dinheiro', value: 'POUPANCA' },
    { label: 'Teto de Gastos', value: 'GASTO' },
    { label: 'Quitar Dívida', value: 'DIVIDA' },
  ];

  opcoesFiltroOrigem = [
    { label: 'Todas as origens', value: 'todas' },
    { label: 'Minhas Metas', value: 'individuais' },
    { label: 'Metas da Família', value: 'familia' },
  ];

  ehMetaConcluida(m: MetaResponse): boolean {
    if (m.tipoMeta === 'GASTO') {
      return m.status === 'CONCLUIDA';
    }
    return m.status === 'CONCLUIDA' || (m.progresso || 0) >= 100 || (m.valorAtual || 0) >= (m.valorTotal || 0);
  }

  ehMetaExpirada(m: MetaResponse): boolean {
    if (this.ehMetaConcluida(m)) return false;
    if (m.status === 'EXPIRADA') return true;
    if (!m.dataLimite) return false;
    const limite = new Date(m.dataLimite);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    limite.setHours(0, 0, 0, 0);
    return limite.getTime() < hoje.getTime();
  }

  ehMetaEmAndamento(m: MetaResponse): boolean {
    return !this.ehMetaConcluida(m) && !this.ehMetaExpirada(m);
  }

  contagemTodas = computed(() => this.metas().length);
  contagemEmAndamento = computed(() => this.metas().filter(m => this.ehMetaEmAndamento(m)).length);
  contagemConcluidas = computed(() => this.metas().filter(m => this.ehMetaConcluida(m)).length);
  contagemExpiradas = computed(() => this.metas().filter(m => this.ehMetaExpirada(m)).length);

  temFamilia: boolean = false;
  tipoMetaSelecionado = signal<string>('POUPANCA');

  today = new Date();

  opcoesContaForm = computed(() => {
    const tipo = this.tipoMetaSelecionado();
    const contasOriginais = this.contas() || [];
    if (tipo === 'GASTO') {
      return [
        { id: null, nome: 'Todas as contas (Orçamento Global)' },
        ...contasOriginais
      ];
    }
    return [
      { id: null, nome: 'Nenhuma (cofrinho geral)' },
      ...contasOriginais
    ];
  });

  opcoesCategoriaForm = computed(() => {
    const tipo = this.tipoMetaSelecionado();
    const categoriasOriginais = this.categorias() || [];
    if (tipo === 'GASTO') {
      return [
        { id: null, nome: 'Todas as categorias (Orçamento Geral do Mês)' },
        ...categoriasOriginais
      ];
    }
    return [
      { id: null, nome: 'Nenhuma categoria específica' },
      ...categoriasOriginais
    ];
  });

  metasFiltradas = computed(() => {
    let lista = this.metas();

    // 1. Filtro por status
    const status = this.filtroStatus();
    if (status === 'concluidas') {
      lista = lista.filter(m => this.ehMetaConcluida(m));
    } else if (status === 'em_andamento') {
      lista = lista.filter(m => this.ehMetaEmAndamento(m));
    } else if (status === 'expiradas') {
      lista = lista.filter(m => this.ehMetaExpirada(m));
    }

    // 2. Filtro por tipo de meta
    const tipo = this.filtroTipo();
    if (tipo !== 'todos') {
      lista = lista.filter(m => m.tipoMeta === tipo);
    }

    // 3. Filtro por origem (Individual vs Família)
    const origem = this.filtroOrigem();
    if (origem === 'familia') {
      lista = lista.filter(m => m.familiar);
    } else if (origem === 'individuais') {
      lista = lista.filter(m => !m.familiar);
    }

    return lista;
  });

  totalMetas = computed(() => {
    return this.metasFiltradas().reduce((sum, m) => sum + (m.valorTotal || 0), 0);
  });

  totalAcumulado = computed(() => {
    return this.metasFiltradas().reduce((sum, m) => sum + (m.valorAtual || 0), 0);
  });

  totalRestante = computed(() => {
    return Math.max(0, this.totalMetas() - this.totalAcumulado());
  });

  progressoGeral = computed(() => {
    const total = this.totalMetas();
    if (!total || total <= 0) return 0;
    return Math.min(100, Math.round((this.totalAcumulado() / total) * 100));
  });

  metaForm: FormGroup;
  aporteForm: FormGroup;
  metaSelecionada: MetaResponse | null = null;

  private metasService = inject(MetasService);
  private contasService = inject(ContaFinanceiraService);
  private transacoesService = inject(TransacoesService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private messageService = inject(MessageService);
  private notificacaoService = inject(NotificacaoService);
  private celebracaoService = inject(CelebracaoService);
  private fb = inject(FormBuilder);

  constructor() {
    this.metaForm = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      descricao: [''],
      valor: [null, [Validators.required, Validators.min(0.01)]],
      contaId: [null],
      categoriaId: [null],
      familiar: [false],
      dataLimite: [null],
      tipoMeta: ['POUPANCA', Validators.required],
    });

    this.aporteForm = this.fb.group({
      valor: [null, [Validators.required, Validators.min(0.01)]],
      contaOrigemId: [null],
      observacao: [''],
    });
  }

  ngOnInit(): void {
    this.verificarFamiliaUsuario();
    this.carregarMetas();
    this.carregarTiposMeta();
    this.carregarContas();
    this.carregarCategorias();
  }

  verificarFamiliaUsuario(): void {
    this.authService.meusDados().subscribe({
      next: (usuario) => {
        this.temFamilia = usuario.idFamilia != null;
      },
      error: () => {
        this.temFamilia = false;
      },
    });
  }

  irParaFamilia(): void {
    this.fecharDialog();
    this.router.navigateByUrl('/familia');
  }

  carregarTiposMeta(): void {
    this.metasService.listarTiposMeta().subscribe({
      next: (response) => this.tiposMeta.set(response),
      error: () => {},
    });
  }

  carregarContas(): void {
    this.contasService.listarContasUsuario().subscribe({
      next: (response: any) => this.contas.set(response),
      error: () => {},
    });
  }

  carregarCategorias(): void {
    this.transacoesService.listarCategorias().subscribe({
      next: (response) => this.categorias.set(response),
      error: () => {},
    });
  }

  carregarMetas(): void {
    this.metasService.listarMetas().subscribe({
      next: (response) => this.metas.set(response),
      error: (error) => console.error('Erro ao listar metas:', error),
    });
  }

  solicitarExclusao(meta: MetaResponse): void {
    this.metaParaExcluir = meta;
    this.exibirDialogExclusao = true;
  }

  cancelarExclusao(): void {
    this.exibirDialogExclusao = false;
    this.metaParaExcluir = null;
  }

  confirmarExclusao(): void {
    if (!this.metaParaExcluir) return;
    const metaAlvo = this.metaParaExcluir;
    this.metasService.deletarMeta(metaAlvo.id).subscribe({
      next: () => {
        this.metas.update((metas) => metas.filter((m) => m.id !== metaAlvo.id));
        this.messageService.add({ severity: 'success', summary: 'Meta excluída com sucesso!', life: 3000 });
        this.cancelarExclusao();
        this.notificacaoService.recarregarImediatamente();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao excluir meta',
          detail: error.error?.message || error.message,
          life: 5000,
        });
      },
    });
  }

  deletarMeta(id: number): void {
    const meta = this.metas().find(m => m.id === id);
    if (meta) {
      this.solicitarExclusao(meta);
    }
  }

  selecionarTipoMeta(tipo: string): void {
    this.metaForm.get('tipoMeta')?.setValue(tipo);
    this.tipoMetaSelecionado.set(tipo);
    if (this.metaForm.get('contaId')?.value === undefined) {
      this.metaForm.get('contaId')?.setValue(null);
    }
    if (this.metaForm.get('categoriaId')?.value === undefined) {
      this.metaForm.get('categoriaId')?.setValue(null);
    }
  }

  abrirDialog(meta?: MetaResponse): void {
    if (meta) {
      this.metaSelecionada = meta;
      this.tipoMetaSelecionado.set(meta.tipoMeta);
      this.metaForm.patchValue({
        nome: meta.nome,
        descricao: meta.descricao,
        valor: meta.valorTotal,
        contaId: meta.contaId ?? null,
        categoriaId: meta.categoriaId ?? null,
        familiar: meta.familiar,
        dataLimite: meta.dataLimite ? new Date(meta.dataLimite) : null,
        tipoMeta: meta.tipoMeta,
      });
    } else {
      this.metaSelecionada = null;
      this.tipoMetaSelecionado.set('POUPANCA');
      this.metaForm.reset({
        tipoMeta: 'POUPANCA',
        familiar: false,
        contaId: null,
        categoriaId: null,
      });
    }
    this.exibirDialog = true;
  }

  fecharDialog(): void {
    this.exibirDialog = false;
    this.metaSelecionada = null;
    this.metaForm.reset();
  }

  abrirDialogAporte(meta: MetaResponse): void {
    if ((meta.progresso || 0) >= 100 || (meta.valorAtual || 0) >= (meta.valorTotal || 0)) {
      this.messageService.add({
        severity: 'info',
        summary: 'Meta já concluída!',
        detail: `A meta "${meta.nome}" já atingiu 100% do objetivo financeiro.`,
        life: 3000,
      });
      return;
    }
    this.metaParaAporte = meta;
    this.aporteForm.reset();
    this.exibirDialogAporte = true;
  }

  fecharDialogAporte(): void {
    this.exibirDialogAporte = false;
    this.metaParaAporte = null;
    this.aporteForm.reset();
  }

  salvarAporte(): void {
    if (this.aporteForm.invalid || !this.metaParaAporte) return;

    const metaAlvo = this.metaParaAporte;
    const payload: AporteMetaRequest = this.aporteForm.value;
    const valorAporte = Number(payload.valor) || 0;
    const valorAtual = Number(metaAlvo.valorAtual) || 0;
    const valorTotal = Number(metaAlvo.valorTotal) || 0;

    // Só comemora se a meta NÃO estava concluída e atingiu 100% EXATAMENTE com este aporte
    const jaEstavaConcluida = valorAtual >= valorTotal;
    const concluiuAgora = !jaEstavaConcluida && (valorAtual + valorAporte) >= valorTotal;

    this.metasService.registrarAporte(metaAlvo.id, payload).subscribe({
      next: () => {
        if (concluiuAgora) {
          this.celebracaoService.celebrarMetaAtingida(metaAlvo.nome, valorTotal);
        } else {
          this.messageService.add({
            severity: 'success',
            summary: 'Aporte realizado com sucesso!',
            detail: `Valor adicionado à meta "${metaAlvo.nome}"`,
            life: 4000,
          });
        }
        this.fecharDialogAporte();
        this.carregarMetas();
        this.notificacaoService.recarregarImediatamente();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao registrar aporte',
          detail: err.error?.message || err.message,
          life: 5000,
        });
      },
    });
  }

  salvarMeta(): void {
    if (this.metaForm.invalid) return;

    const formValue = this.metaForm.value;
    const metaData: MetaRequest = {
      nome: formValue.nome,
      descricao: formValue.descricao,
      valor: formValue.valor,
      contaId: formValue.contaId,
      categoriaId: formValue.categoriaId,
      familiar: formValue.familiar,
      dataLimite: formValue.dataLimite,
      tipoMeta: formValue.tipoMeta,
    };

    if (this.metaSelecionada) {
      this.metasService.atualizarMeta(this.metaSelecionada.id, metaData).subscribe({
        next: (updatedMeta) => {
          this.metas.update((metas) => metas.map((m) => (m.id === updatedMeta.id ? updatedMeta : m)));
          this.fecharDialog();
          this.messageService.add({ severity: 'success', summary: 'Meta atualizada com sucesso!', life: 3000 });
          this.notificacaoService.recarregarImediatamente();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erro ao atualizar meta',
            detail: error.error?.message || error.message,
            life: 5000,
          });
        },
      });
    } else {
      this.metasService.criarMeta(metaData).subscribe({
        next: (newMeta) => {
          this.metas.update((metas) => [...metas, newMeta]);
          this.fecharDialog();
          this.messageService.add({ severity: 'success', summary: 'Nova meta criada com sucesso!', life: 3000 });
          this.notificacaoService.recarregarImediatamente();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erro ao criar meta',
            detail: error.error?.message || error.message,
            life: 5000,
          });
        },
      });
    }
  }
}

