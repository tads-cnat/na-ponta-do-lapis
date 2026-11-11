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
<<<<<<< HEAD
<<<<<<< HEAD
      case 'sm': return 36;
=======
      case 'sm': return 40;
>>>>>>> cc2840e (feat(celebracao): adicionar confetes, modal comemorativo hero, medalhas vetoriais exclusivas e polling reativo de notificacoes)
=======
      case 'sm': return 36;
>>>>>>> 92c4c90 (fix(ui): alinhar icones de notificacao, remover badge de moeda redundante, estilizar select de moedas e adicionar opcoes explicitas de escopo nos forms de meta)
      case 'md': return 56;
      case 'lg': return 84;
      case 'xl': return 120;
      default: return 56;
    }
  }
}

