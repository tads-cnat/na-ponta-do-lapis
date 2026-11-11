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
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
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

@Component({
  selector: 'app-metas',
  imports: [
    CommonModule,
    MetaItemComponent,
    Button,
    DialogModule,
    InputTextModule,
    ReactiveFormsModule,
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
  metaParaAporte: MetaResponse | null = null;
  filtroFamiliar: 'todas' | 'individuais' | 'familia' = 'todas';
  temFamilia: boolean = false;

  today = new Date();

  metasFiltradas = computed(() => {
    const lista = this.metas();
    if (this.filtroFamiliar === 'familia') return lista.filter((m) => m.familiar);
    if (this.filtroFamiliar === 'individuais') return lista.filter((m) => !m.familiar);
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

  deletarMeta(id: number): void {
    this.metasService.deletarMeta(id).subscribe({
      next: () => {
        this.metas.update((metas) => metas.filter((m) => m.id !== id));
        this.messageService.add({ severity: 'success', summary: 'Meta excluída com sucesso!', life: 3000 });
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao deletar meta',
          detail: error.error?.message || error.message,
          life: 5000,
        });
      },
    });
  }

  abrirDialog(meta?: MetaResponse): void {
    if (meta) {
      this.metaSelecionada = meta;
      this.metaForm.patchValue({
        nome: meta.nome,
        descricao: meta.descricao,
        valor: meta.valorTotal,
        contaId: meta.contaId,
        categoriaId: meta.categoriaId,
        familiar: meta.familiar,
        dataLimite: meta.dataLimite ? new Date(meta.dataLimite) : null,
        tipoMeta: meta.tipoMeta,
      });
    } else {
      this.metaSelecionada = null;
      this.metaForm.reset({
        tipoMeta: 'POUPANCA',
        familiar: false,
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

    const payload: AporteMetaRequest = this.aporteForm.value;
    this.metasService.registrarAporte(this.metaParaAporte.id, payload).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Aporte realizado com sucesso!',
          detail: `Valor adicionado à meta "${this.metaParaAporte?.nome}"`,
          life: 4000,
        });
        this.fecharDialogAporte();
        this.carregarMetas();
        this.notificacaoService.carregarNotificacoes();
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

