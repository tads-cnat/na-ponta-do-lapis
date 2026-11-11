import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-medalha-conquista',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './medalha-conquista.component.html',
  styleUrl: './medalha-conquista.component.css'
})
export class MedalhaConquistaComponent {
  @Input() id: string = '';
  @Input() desbloqueada: boolean = true;
  @Input() tamanho: 'sm' | 'md' | 'lg' | 'xl' = 'md';

  get tamanhoPx(): number {
    switch (this.tamanho) {
      case 'sm': return 40;
      case 'md': return 56;
      case 'lg': return 84;
      case 'xl': return 120;
      default: return 56;
    }
  }
}

