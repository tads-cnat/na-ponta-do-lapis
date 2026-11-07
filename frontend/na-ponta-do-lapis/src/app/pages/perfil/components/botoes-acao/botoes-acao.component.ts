import {Component, Input, Output, EventEmitter} from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimeNGModuleModule } from '../../../../shared/primeNg.module';

@Component({
  selector: 'app-botoes-acao',
  standalone: true,
  imports: [CommonModule, PrimeNGModuleModule],
  templateUrl: './botoes-acao.component.html'
})
export class BotoesAcaoComponent {
  public confirmacaoVisivel = false;
  @Input() podeAbrirConfirmacao = true;

  @Output() cliqueSalvar = new EventEmitter<void>();

  public aoClicarSalvar(): void {
    if (!this.podeAbrirConfirmacao) {
      this.cliqueSalvar.emit();
      return;
    }

    this.confirmacaoVisivel = true;
  }

  public confirmarSalvar(): void {
    this.confirmacaoVisivel = false;
    this.cliqueSalvar.emit();
  }

  public cancelarSalvar(): void {
    this.confirmacaoVisivel = false;
  }
}
