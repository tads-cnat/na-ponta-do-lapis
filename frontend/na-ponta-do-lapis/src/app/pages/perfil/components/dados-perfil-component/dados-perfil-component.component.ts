import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsuarioService } from '../../service/perfil.services';
import { FamiliaService } from '../../../familia/service/familia.service';
import { Usuario } from '../../../../model/IUsuario.models';

@Component({
  selector: 'app-dados-perfil',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dados-perfil-component.component.html',
  styleUrl: './dados-perfil-component.component.css',
})
export class DadosPerfilComponent implements OnInit {

  constructor(
    private usuarioService: UsuarioService,
    private familiaService: FamiliaService,
    private cdr: ChangeDetectorRef
  ) {}

  public usuarioLogado: Usuario | null = null;
  public nomeFamilia: string | null = null;

  ngOnInit(): void {
    this.carregarDadosDoPerfil();
  }

  private carregarDadosDoPerfil(): void {
    this.usuarioService.obterPerfilCompleto().subscribe({
      next: (res: Usuario) => {
        this.usuarioLogado = res;
        if (res.idFamilia) {
          this.familiaService.buscarFamiliaPorId(res.idFamilia).subscribe({
            next: (familia) => {
              this.nomeFamilia = familia?.nome ?? null;
              this.cdr.detectChanges();
            },
            error: () => {
              this.nomeFamilia = null;
              this.cdr.detectChanges();
            }
          });
        } else {
          this.nomeFamilia = null;
        }
        this.cdr.detectChanges();
      },
      error: (err: Error) => {
        console.error('Erro ao buscar dados do perfil detalhado:', err);
      }
    });
  }

  public obterPapel(): string {
    const p = this.usuarioLogado?.papel?.toUpperCase();
    if (p === 'ADMIN_FAMILIA' || p === 'ADMIN') return 'Administrador da Família';
    if (p === 'ADMIN_SITE') return 'Administrador do Sistema';
    if (p === 'RESPONSAVEL') return 'Responsável';
    return this.usuarioLogado?.idFamilia ? 'Membro' : 'Usuário';
  }

  public obterMoeda(): string {
    const m = this.usuarioLogado?.moedaPadrao?.toUpperCase() || 'BRL';
    if (m === 'USD') return 'USD ($)';
    if (m === 'EUR') return 'EUR (€)';
    return 'BRL (R$)';
  }

  public obterFamilia(): string {
    if (!this.usuarioLogado?.idFamilia) return 'Individual';
    return this.nomeFamilia || 'Grupo Familiar';
  }
}