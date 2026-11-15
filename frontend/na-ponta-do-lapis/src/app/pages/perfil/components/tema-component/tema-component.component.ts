import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TemaApp, ThemeService } from '../../../../service/theme.service';

@Component({
  selector: 'app-tema-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tema-component.component.html',
  styleUrl: './tema-component.component.css',
})
export class TemaComponentComponent {
  private readonly themeService = inject(ThemeService);
  public readonly temaAtual = this.themeService.tema;

  public selecionarTema(tema: TemaApp): void {
    this.themeService.definir(tema);
  }
}
