import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MetaResponse } from "../../../../model/IMetas.models";
import { ProgressBarModule } from "primeng/progressbar";
import { AppMoedaPipe } from "../../../../shared/pipes/app-moeda.pipe";
import { IlustracaoMetaComponent } from "../../../../shared/components/ilustracao-meta/ilustracao-meta.component";

@Component({
  selector: 'app-meta-item-dashboard',
  imports: [CommonModule, ProgressBarModule, AppMoedaPipe, IlustracaoMetaComponent],
  templateUrl: './meta-item-dashboard.html',
})

export class MetaItemDashboardComponent {

  @Input() meta!: MetaResponse;

  get valorFalta(): number {
    return Math.max(0, (this.meta.valorTotal || 0) - (this.meta.valorAtual || 0));
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
    return this.meta.status === 'EXPIRADA';
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
