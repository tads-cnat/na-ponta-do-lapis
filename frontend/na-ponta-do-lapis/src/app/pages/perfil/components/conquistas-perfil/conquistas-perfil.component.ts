import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConquistaService } from '../../../../shared/service/conquista.service';
import { PerfilGamificado, ConquistaItem, CategoriaConquista } from '../../../../model/IConquista.models';
import { MedalhaConquistaComponent } from '../../../../shared/components/medalha-conquista/medalha-conquista.component';

@Component({
  selector: 'app-conquistas-perfil',
  standalone: true,
  imports: [CommonModule, MedalhaConquistaComponent],
  templateUrl: './conquistas-perfil.component.html',
  styleUrl: './conquistas-perfil.component.css'
})
export class ConquistasPerfilComponent implements OnInit {
  public perfil: PerfilGamificado | null = null;
  public carregando = true;
  public erroAoCarregar = false;
  public categoriaFiltro: 'TODAS' | CategoriaConquista = 'TODAS';

  constructor(
    private conquistaService: ConquistaService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregarDados();
  }

  public carregarDados(): void {
    this.carregando = true;
    this.erroAoCarregar = false;

    this.conquistaService.carregarPerfilGamificado().subscribe({
      next: (perfil) => {
        this.perfil = perfil;
        this.carregando = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao carregar perfil gamificado:', err);
        this.erroAoCarregar = true;
        this.carregando = false;
        this.cd.detectChanges();
      }
    });
  }

  public setCategoria(categoria: 'TODAS' | CategoriaConquista): void {
    this.categoriaFiltro = categoria;
  }

  get conquistasFiltradas(): ConquistaItem[] {
    if (!this.perfil) return [];
    if (this.categoriaFiltro === 'TODAS') {
      return this.perfil.conquistas;
    }
    return this.perfil.conquistas.filter((c) => c.categoria === this.categoriaFiltro);
  }

  public obterTextoCategoria(cat: CategoriaConquista): string {
    switch (cat) {
      case 'METAS':
        return 'Metas & Objetivos';
      case 'ECONOMIA':
        return 'Economia & Aportes';
      case 'DISCIPLINA':
        return 'Disciplina';
      case 'FAMILIA':
        return 'Família';
      default:
        return cat;
    }
  }

  public formatarData(dataIso?: string | null): string {
    if (!dataIso) return '';
    try {
      const d = new Date(dataIso);
      return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });
    } catch {
      return '';
    }
  }
}

