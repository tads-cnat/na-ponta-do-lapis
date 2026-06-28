import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MetaItemComponent } from './components/meta-item/meta-item.component';
import { MetasService } from './services/metas.service';
import { ContaFinanceiraService } from '../contas/service/contas.service';
import { MetaRequest, MetaResponse, TipoMetaResponse } from '@models/IMetas.models';

import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext'; 
import { InputNumber } from 'primeng/inputnumber';
import { DatePicker } from 'primeng/datepicker';
import { Select } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { Textarea } from 'primeng/textarea';

@Component({
  selector: 'app-metas',
  imports: [MetaItemComponent, Button, DialogModule, InputTextModule, ReactiveFormsModule, InputNumber, DatePicker, Select, Textarea, ToastModule],
  templateUrl: './metas.component.html',
  styleUrl: './metas.component.css',
})
export class MetasComponent implements OnInit {

  metas = signal<MetaResponse[]>([]);
  tiposMeta = signal<TipoMetaResponse[]>([]);
  contas = signal<any[]>([]);
  exibirDialog: boolean = false;

  totalMetas = computed(() => {
    return this.metas().reduce((sum, m) => sum + (m.valorTotal || 0), 0);
  });

  totalAcumulado = computed(() => {
    return this.metas().reduce((sum, m) => sum + (m.valorAtual || 0), 0);
  });

  totalMetasFormatado = computed(() =>
    this.totalMetas().toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  );

  totalAcumuladoFormatado = computed(() =>
    this.totalAcumulado().toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  );

  metaForm: FormGroup;
  metaSelecionada: MetaResponse | null = null;

  today = new Date();
  private messageService = inject(MessageService);

  constructor(
    private ContaFinanceiraService: ContaFinanceiraService,
    private metasService: MetasService,
    private fb: FormBuilder
  ) {
    this.metaForm = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      descricao: [''],
      valor: [0, [Validators.required, Validators.min(0.01)]],
      contaId: [0],
      dataLimite: [null, []],
      tipoMeta: [null, Validators.required],
    });
  }

  ngOnInit(): void {
    this.listarMetas();
    this.listarContas();
    this.metasService.listarTiposMeta().subscribe({
      next: (tipos) => this.tiposMeta.set(tipos)
    });
  }

  listarContas(): void {
    this.ContaFinanceiraService.listarContasUsuario().subscribe({
      next: (response) => {
        this.contas.set(response);
      },
      error: (error) => {
        console.error('Erro ao listar contas do usuário:', error);
      }
    });
  }

  listarMetas(): void {
    this.metasService.listarMetas().subscribe({
      next: (response) => {
        this.metas.set(response);
      },
      error: (error) => {
        console.error('Erro ao listar metas:', error);
      }
    });
  }

  deletarMeta(id: number): void {
    this.metasService.deletarMeta(id).subscribe({
      next: () => {
        this.metas.update( metas => metas.filter(meta => meta.id !== id));
        this.messageService.add({ severity: 'success', summary: 'Meta deletada com sucesso!', life: 3000 });
      },
      error: (error) => {
        this.messageService.add({ severity: 'error', summary: 'Erro ao deletar meta', detail: error.error?.message || error.message, life: 5000 });
      }
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
        dataLimite: meta.dataLimite,
        tipoMeta: meta.tipoMeta,
      });
    } else {
      this.metaSelecionada = null;
      this.metaForm.reset();
    }
    this.exibirDialog = true;
  }

  fecharDialog(): void {
    this.exibirDialog = false;
    this.metaSelecionada = null;
    this.metaForm.reset();
  }

  salvarMeta(): void {
    if (this.metaForm.invalid) return;

    const formValue = this.metaForm.value;
    const metaData: MetaRequest = {
      nome: formValue.nome,
      descricao: formValue.descricao,
      valor: formValue.valor,
      contaId: formValue.contaId,
      dataLimite: formValue.dataLimite,
      tipoMeta: formValue.tipoMeta,
    };

    if (this.metaSelecionada) {
      this.metasService.atualizarMeta(this.metaSelecionada.id!, metaData).subscribe({
        next: (updatedMeta) => {
          this.metas.update(metas => metas.map(meta => meta.id === updatedMeta.id ? updatedMeta : meta));
          this.fecharDialog();
          this.messageService.add({ severity: 'success', summary: 'Meta atualizada com sucesso!', life: 3000 });
        },
        error: (error) => {
          this.messageService.add({ severity: 'error', summary: 'Erro ao atualizar meta', detail: error.error?.message || error.message, life: 5000 });
        }
      });
    } else {
      this.metasService.criarMeta(metaData).subscribe({
        next: (newMeta) => {
          this.metas.update(metas => [...metas, newMeta]);
          this.fecharDialog();
          this.messageService.add({ severity: 'success', summary: 'Meta criada com sucesso!', life: 3000 });
        },
        error: (error) => {
          this.messageService.add({ severity: 'error', summary: 'Erro ao criar meta', detail: error.error?.message || error.message, life: 5000 });
        }
      });
    }

  }


 
}
