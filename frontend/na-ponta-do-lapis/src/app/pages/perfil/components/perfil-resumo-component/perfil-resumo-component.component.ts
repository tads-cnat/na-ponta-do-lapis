import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsuarioService } from '../../service/perfil.services';
import { Usuario } from '../../../../model/IUsuario.models';

@Component({
  selector: 'app-perfil-resumo',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './perfil-resumo-component.component.html', // 👈 Corrigido o nome do arquivo HTML
  styleUrl: './perfil-resumo-component.component.css'     // 👈 Corrigido o nome do arquivo CSS
})
export class PerfilResumoComponent implements OnInit {

  constructor(private usuarioService: UsuarioService) {}

  public usuarioLogado: Usuario | null = null;

  ngOnInit(): void {
    this.carregarResumoPerfil();
  }

  private carregarResumoPerfil(): void {
    this.usuarioService.obterPerfilCompleto().subscribe({
      next: (res: Usuario) => {
        this.usuarioLogado = res;
      },
      error: (err: Error) => {
        console.error('Erro ao carregar resumo do perfil:', err);
      }
    });
  }

  public usarFotoPadrao(event: any): void {
    event.target.src = 'user_anonimo.png';
  }
}