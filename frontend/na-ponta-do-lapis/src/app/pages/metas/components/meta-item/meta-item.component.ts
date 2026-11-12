import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MetaResponse } from '@models/IMetas.models';
import { Button } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';
import { TooltipModule } from 'primeng/tooltip';
import { AppMoedaPipe } from '../../../../shared/pipes/app-moeda.pipe';
import { IlustracaoMetaComponent } from '../../../../shared/components/ilustracao-meta/ilustracao-meta.component';

@Component({
  selector: 'app-meta-item',
  imports: [CommonModule, Button, ProgressBarModule, TooltipModule, AppMoedaPipe, IlustracaoMetaComponent],
  templateUrl: './meta-item.component.html',
  styleUrl: './meta-item.component.css',
})
export class MetaItemComponent {
  @Input() meta!: MetaResponse;
  @Output() deletarMeta: EventEmitter<number> = new EventEmitter<number>();
  @Output() editarMeta: EventEmitter<MetaResponse> = new EventEmitter<MetaResponse>();
  @Output() aportarMeta: EventEmitter<MetaResponse> = new EventEmitter<MetaResponse>();
  @Output() verHistorico: EventEmitter<MetaResponse> = new EventEmitter<MetaResponse>();

  onDeletar(): void {
    this.deletarMeta.emit(this.meta.id);
  }

  onEditar(): void {
    this.editarMeta.emit(this.meta);
  }

  onAportar(): void {
    this.aportarMeta.emit(this.meta);
  }

  onVerHistorico(): void {
    this.verHistorico.emit(this.meta);
  }

  get valorFalta(): number {
    return Math.max(0, this.meta.valorTotal - (this.meta.valorAtual || 0));
  }

  get diasRestantes(): number | null {
    if (!this.meta.dataLimite) return null;
    const limite = new Date(this.meta.dataLimite);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    limite.setHours(0, 0, 0, 0);
    const diffMs = limite.getTime() - hoje.getTime();
    return Math.round(diffMs / (1000 * 60 * 60 * 24));
  }

  get ehConcluida(): boolean {
    if (this.meta.tipoMeta === 'GASTO') {
      return this.meta.status === 'CONCLUIDA';
    }
    return this.meta.status === 'CONCLUIDA' || (this.meta.progresso || 0) >= 100 || (this.meta.valorAtual || 0) >= (this.meta.valorTotal || 0);
  }

  get ehEstourada(): boolean {
    return this.meta.status === 'ESTOURADA' || (this.meta.tipoMeta === 'GASTO' && this.meta.progresso >= 100);
  }

  get ehExpirada(): boolean {
    if (this.ehConcluida) return false;
    if (this.meta.status === 'EXPIRADA') return true;
    const dias = this.diasRestantes;
    return dias !== null && dias < 0;
  }

  get ritmoMensal(): number | null {
    if (this.ehConcluida || this.ehExpirada || this.meta.tipoMeta === 'GASTO') return null;
    const dias = this.diasRestantes;
    if (dias === null || dias <= 0) return null;
    const meses = Math.max(1, Math.round(dias / 30.4));
    return Math.ceil(this.valorFalta / meses);
  }

  get progressoLimitado(): number {
    return Math.min(100, Math.round(this.meta.progresso || 0));
  }

  get corBarraProgresso(): string {
    if (this.meta.tipoMeta === 'GASTO') {
      if (this.ehEstourada) return 'linear-gradient(119deg, #C24E4E, #AB3E3E)';
      if (this.meta.progresso >= 80) return 'linear-gradient(119deg, #F59E0B, #D97706)';
      return 'linear-gradient(119deg, #10B981, #059669)';
    }
    if (this.ehConcluida) return 'linear-gradient(119deg, #10B981, #059669)';
    if (this.ehExpirada) return 'linear-gradient(119deg, #F59E0B, #D97706)';
    return 'linear-gradient(119deg, #7843CD, #EEC046)';
  }
}
