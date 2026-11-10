import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-tema-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tema-component.component.html',
  styleUrl: './tema-component.component.css',
})
export class TemaComponentComponent {
  public temaAtual: 'claro' | 'escuro' = 'claro';

  public selecionarTema(tema: 'claro' | 'escuro'): void {
    this.temaAtual = tema;
  }
}
