import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsuarioService } from '../../service/perfil.services'; // Ajuste o caminho até o novo service
import { Usuario } from '../../../../model/IUsuario.models';

@Component({
  selector: 'app-dados-perfil',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dados-perfil-component.component.html',
  styleUrl: './dados-perfil-component.component.css',
})
export class DadosPerfilComponent implements OnInit {

  // Injetando o novo UsuarioService dedicado do Perfil
  constructor(private usuarioService: UsuarioService) {}

  // Voltamos com segurança para a tipagem forte de Usuario!
  public usuarioLogado: Usuario | null = null;

  ngOnInit(): void {
    this.carregarDadosDoPerfil();
  }

  private carregarDadosDoPerfil(): void {
    this.usuarioService.obterPerfilCompleto().subscribe({
      next: (res: Usuario) => {
        this.usuarioLogado = res;
        console.log('Perfil completo carregado com sucesso:', res);
      },
      error: (err: Error) => {
        console.error('Erro ao buscar dados do perfil detalhado:', err);
      }
    });
  }
}