import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsuarioService } from '../../service/perfil.services';
import { Usuario } from '../../../../model/IUsuario.models';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-perfil-resumo',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './perfil-resumo-component.component.html', // 👈 Corrigido o nome do arquivo HTML
  styleUrl: './perfil-resumo-component.component.css'     // 👈 Corrigido o nome do arquivo CSS
})
export class PerfilResumoComponent implements OnInit {

  constructor(private usuarioService: UsuarioService, private cdr: ChangeDetectorRef) {}

  public usuarioLogado: Usuario | null = null;

  ngOnInit(): void {
    this.carregarResumoPerfil();
  }

  private carregarResumoPerfil(): void {
    this.usuarioService.obterPerfilCompleto().subscribe({
      next: (res: Usuario) => {
        this.usuarioLogado = res;
        this.cdr.detectChanges(); // Força a atualização da view após a mudança de estado
      },
      error: (err: Error) => {
        console.error('Erro ao carregar resumo do perfil:', err);
      }
    });
  }

  public usarFotoPadrao(event: any): void {
    event.target.src = 'user_anonimo.png';
  }

  public obterPapelFormatado(): string {
    const papel = this.usuarioLogado?.papel?.toUpperCase();
    if (papel === 'ADMIN') return 'Administrador';
    if (papel === 'RESPONSAVEL') return 'Responsável Familiar';
    return 'Membro';
  }

  public obterMoedaFormatada(): string {
    const moeda = this.usuarioLogado?.moedaPadrao?.toUpperCase() || 'BRL';
    if (moeda === 'USD') return '🇺🇸 USD ($)';
    if (moeda === 'EUR') return '🇪🇺 EUR (€)';
    return '🇧🇷 BRL (R$)';
  }
}