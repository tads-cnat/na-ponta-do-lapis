import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface IlustracaoMetaItem {
  id: string;
  nome: string;
  categoria: 'geral' | 'sonhos' | 'despesas' | 'dividas';
}

export const CATALOGO_ILUSTRACOES_METAS: IlustracaoMetaItem[] = [
  { id: 'cofrinho', nome: 'Cofrinho', categoria: 'geral' },
  { id: 'reserva', nome: 'Reserva', categoria: 'geral' },
  { id: 'investimento', nome: 'Investimentos', categoria: 'geral' },
  { id: 'viagem', nome: 'Viagem', categoria: 'sonhos' },
  { id: 'carro', nome: 'Carro', categoria: 'sonhos' },
  { id: 'casa', nome: 'Casa / Imóvel', categoria: 'sonhos' },
  { id: 'educacao', nome: 'Educação', categoria: 'sonhos' },
  { id: 'eletronicos', nome: 'Eletrônicos', categoria: 'sonhos' },
  { id: 'compras_mercado', nome: 'Mercado', categoria: 'despesas' },
  { id: 'lazer', nome: 'Lazer', categoria: 'despesas' },
  { id: 'saude', nome: 'Saúde', categoria: 'despesas' },
  { id: 'divida_quitacao', nome: 'Quitar Dívida', categoria: 'dividas' },
];

@Component({
  selector: 'app-ilustracao-meta',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ilustracao-meta.component.html',
  styleUrl: './ilustracao-meta.component.css'
})
export class IlustracaoMetaComponent {
  @Input() icone: string | null | undefined = 'cofrinho';
  @Input() tamanho: 'sm' | 'md' | 'lg' = 'md';

  get iconeAtivo(): string {
    return this.icone && this.icone.trim() !== '' ? this.icone : 'cofrinho';
  }

  get dimensoesContainer(): string {
    switch (this.tamanho) {
      case 'sm': return 'w-9 h-9 rounded-xl';
      case 'lg': return 'w-14 h-14 rounded-2xl';
      case 'md':
      default: return 'w-12 h-12 rounded-2xl';
    }
  }

  get dimensoesSvg(): string {
    switch (this.tamanho) {
      case 'sm': return 'w-5 h-5';
      case 'lg': return 'w-8 h-8';
      case 'md':
      default: return 'w-7 h-7';
    }
  }
}

