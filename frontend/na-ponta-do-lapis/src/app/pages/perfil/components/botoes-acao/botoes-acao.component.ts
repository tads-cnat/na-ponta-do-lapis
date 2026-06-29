import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-botoes-acao',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './botoes-acao.component.html'
})
export class BotoesAcaoComponent {
  @Output() cliqueSalvar = new EventEmitter<void>();

  public aoClicarSalvar(): void {
    this.cliqueSalvar.emit();
  }
}