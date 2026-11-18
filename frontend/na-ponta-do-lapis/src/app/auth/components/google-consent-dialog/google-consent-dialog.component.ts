import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Capacitor } from '@capacitor/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';

@Component({
  selector: 'app-google-consent-dialog',
  standalone: true,
  imports: [PrimeNGModuleModule, FormsModule, RouterLink],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="onVisibleChange($event)"
      [modal]="true"
      [draggable]="false"
      [resizable]="false"
      [closable]="!loading"
      header="Confirmar criação de conta com Google"
      [style]="{ width: '28rem', maxWidth: '94vw' }"
      appendTo="body">
      <div class="flex flex-col gap-4 text-sm text-[#413B59]">
        <div class="flex items-center gap-3 p-3 rounded-xl bg-[#F3EEFD] border border-[#DDD0FB]">
          @if (fotoUrl) {
            <img [src]="fotoUrl" [alt]="nome || 'Foto Google'" class="w-10 h-10 rounded-full object-cover border border-[#9F79F2]/40" />
          } @else {
            <div class="w-10 h-10 rounded-full bg-[#9F79F2]/20 flex items-center justify-center text-[#413B59] font-bold">
              {{ (nome || email || 'U').charAt(0).toUpperCase() }}
            </div>
          }
          <div class="min-w-0">
            <p class="font-semibold text-[#0D0D0D] truncate">{{ nome || 'Usuário Google' }}</p>
            <p class="text-xs text-[#413B59]/80 truncate">{{ email }}</p>
          </div>
        </div>

        <p class="leading-relaxed">
          Para criar sua conta no <strong>Na Ponta do Lápis</strong>, utilizaremos apenas os seguintes dados do seu perfil Google:
        </p>

        <ul class="list-disc pl-5 space-y-1 text-xs text-[#413B59]/90">
          <li><strong>Nome completo</strong> para identificar seu perfil</li>
          <li><strong>E-mail verificado</strong> para acesso e recuperação da conta</li>
          <li><strong>Foto de perfil</strong> para exibição na plataforma</li>
        </ul>

        <label class="flex items-start gap-2.5 cursor-pointer select-none pt-1">
          <p-checkbox [(ngModel)]="aceitouTermos" [binary]="true" inputId="googleLgpdTermos"></p-checkbox>
          <span class="text-xs leading-snug text-[#0D0D0D]">
            Li e concordo com os
            <a [routerLink]="['/termos']" [attr.target]="ehAppNativo ? null : '_blank'" [attr.rel]="ehAppNativo ? null : 'noopener noreferrer'" (click)="onAbrirLinkLegal()" class="text-[#7843CD] font-bold underline decoration-[#EEC046] decoration-2 underline-offset-2">Termos de Serviço</a>
            e a
            <a [routerLink]="['/privacidade']" [attr.target]="ehAppNativo ? null : '_blank'" [attr.rel]="ehAppNativo ? null : 'noopener noreferrer'" (click)="onAbrirLinkLegal()" class="text-[#7843CD] font-bold underline decoration-[#EEC046] decoration-2 underline-offset-2">Política de Privacidade (LGPD)</a>
            para criar minha conta.
          </span>
        </label>
      </div>

      <ng-template #footer>
        <div class="flex justify-end gap-2">
          <p-button
            type="button"
            label="Cancelar"
            styleClass="btn-npl-secondary px-4 py-2 text-sm"
            [disabled]="loading"
            (onClick)="cancel.emit()">
          </p-button>
          <p-button
            type="button"
            label="Confirmar e criar conta"
            styleClass="btn-npl-primary px-4 py-2 text-sm"
            [disabled]="!aceitouTermos || loading"
            [loading]="loading"
            (onClick)="confirm.emit()">
          </p-button>
        </div>
      </ng-template>
    </p-dialog>
  `,
})
export class GoogleConsentDialogComponent implements OnChanges {
  @Input() visible = false;
  @Input() nome = '';
  @Input() email = '';
  @Input() fotoUrl: string | null = null;
  @Input() loading = false;

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  readonly ehAppNativo = Capacitor.isNativePlatform();
  aceitouTermos = false;

  onAbrirLinkLegal(): void {
    if (this.ehAppNativo) {
      this.cancel.emit();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue === true) {
      this.aceitouTermos = false;
    }
  }

  onVisibleChange(nextVisible: boolean): void {
    if (!nextVisible && !this.loading) {
      this.cancel.emit();
    }
  }
}

