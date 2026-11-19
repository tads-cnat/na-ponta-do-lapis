import { Component, input, output } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';

export interface AutorizacaoDados {
  titulo: string;
  descricao: string;
  autorizar: () => void;
}

@Component({
  selector: 'app-autorizacao-dados',
  standalone: true,
  imports: [ButtonModule, DialogModule],
  template: `
    <p-dialog [header]="solicitacao().titulo" [visible]="true" [modal]="true"
      [closable]="false" [style]="{ width: '30rem', maxWidth: '92vw' }"
      (visibleChange)="!$event && decidir(false)">
      <div class="flex items-start gap-3 py-2 text-sm">
        <i class="pi pi-shield mt-0.5 text-xl" aria-hidden="true"></i>
        <p>{{ solicitacao().descricao }}</p>
      </div>
      <ng-template pTemplate="footer">
        <div class="flex flex-col sm:flex-row gap-2 w-full justify-end">
          <p-button label="Recusar" severity="secondary" [outlined]="true"
            styleClass="rounded-xl! w-full sm:w-auto" (onClick)="decidir(false)"></p-button>
          <p-button label="Autorizar" styleClass="rounded-xl! w-full sm:w-auto btn-npl-primary"
            (onClick)="decidir(true)"></p-button>
        </div>
      </ng-template>
    </p-dialog>
  `
})
export class AutorizacaoDadosComponent {
  readonly solicitacao = input.required<AutorizacaoDados>();
  readonly concluida = output<void>();

  decidir(autorizada: boolean): void {
    const solicitacao = this.solicitacao();
    this.concluida.emit();
    if (autorizada) solicitacao.autorizar();
  }
}
