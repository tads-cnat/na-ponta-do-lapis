import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MetaResponse } from '@models/IMetas.models';
import { Button } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';
import { TooltipModule } from 'primeng/tooltip';
import { AppMoedaPipe } from '../../../../shared/pipes/app-moeda.pipe';

@Component({
  selector: 'app-meta-item',
  imports: [CommonModule, Button, ProgressBarModule, TooltipModule, AppMoedaPipe],
  templateUrl: './meta-item.component.html',
  styleUrl: './meta-item.component.css',
})
export class MetaItemComponent {
  @Input() meta!: MetaResponse;
  @Output() deletarMeta: EventEmitter<number> = new EventEmitter<number>();
  @Output() editarMeta: EventEmitter<MetaResponse> = new EventEmitter<MetaResponse>();
  @Output() aportarMeta: EventEmitter<MetaResponse> = new EventEmitter<MetaResponse>();

  onDeletar(): void {
    this.deletarMeta.emit(this.meta.id);
  }

  onEditar(): void {
    this.editarMeta.emit(this.meta);
  }

  onAportar(): void {
    this.aportarMeta.emit(this.meta);
  }

  get valorFalta(): number {
    return Math.max(0, this.meta.valorTotal - (this.meta.valorAtual || 0));
  }

  get ehConcluida(): boolean {
    return (this.meta.progresso || 0) >= 100 || (this.meta.valorAtual || 0) >= (this.meta.valorTotal || 0);
  }

  get progressoLimitado(): number {
    return Math.min(100, Math.round(this.meta.progresso || 0));
  }

  get corBarraProgresso(): string {
    if (this.meta.tipoMeta === 'GASTO') {
      if (this.meta.progresso >= 100) return 'linear-gradient(119deg, #C24E4E, #AB3E3E)';
      if (this.meta.progresso >= 80) return 'linear-gradient(119deg, #F59E0B, #D97706)';
      return 'linear-gradient(119deg, #10B981, #059669)';
    }
    if (this.meta.progresso >= 100) return 'linear-gradient(119deg, #10B981, #059669)';
    return 'linear-gradient(119deg, #7843CD, #EEC046)';
  }
}
