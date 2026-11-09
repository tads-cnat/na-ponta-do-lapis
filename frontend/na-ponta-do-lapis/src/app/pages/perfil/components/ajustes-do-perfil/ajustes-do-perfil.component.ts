import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BotoesAcaoComponent } from '../botoes-acao/botoes-acao.component';
import { FormularioSecaoComponent, CampoFormulario } from '../formulario-secao/formulario-secao.component';
import { IntegracoesPerfilComponent } from '../integracoes-perfil/integracoes-perfil.component';
import { UsuarioService } from '../../service/perfil.services';
import { ChangeDetectorRef } from '@angular/core';
import { StorageService } from '../../../../auth/service/storage.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-ajustes-do-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormularioSecaoComponent, BotoesAcaoComponent, IntegracoesPerfilComponent],
  templateUrl: './ajustes-do-perfil.component.html'
})
export class AjustesPerfilComponent implements OnInit {
  public perfilForm!: FormGroup;
  public abaAtiva: 'dados' | 'integracoes' = 'dados';

  public camposDetalhes: CampoFormulario[] = [];
  public camposSenha: CampoFormulario[] = [];

  get podeAbrirConfirmacao(): boolean {
    const valores = this.perfilForm?.value ?? {};
    const camposSenha = [
      valores.senhaAtual,
      valores.confirmarSenhaAtual,
      valores.novaSenha,
      valores.confirmarNovaSenha
    ];
    const alterandoSenha = camposSenha.some(this.temTexto);

    if (alterandoSenha) {
      return camposSenha.every(this.temTexto)
        && valores.senhaAtual === valores.confirmarSenhaAtual
        && valores.novaSenha === valores.confirmarNovaSenha;
    }

    return [valores.nome, valores.email].some(this.temTexto);
  }

  private readonly temTexto = (valor: unknown): boolean =>
    typeof valor === 'string' && valor.trim() !== '';

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private messageService: MessageService
  ) {
    this.inicializarFormulario();
  }

  ngOnInit(): void {
    this.carregarDadosIniciais();
    this.route.queryParams.subscribe((params) => {
      if (params['aba'] === 'integracoes' || params['tab'] === 'integracoes' || params['aba'] === 'ia' || params['secao'] === 'ia') {
        this.abaAtiva = 'integracoes';
        this.cdr.detectChanges();
      }
    });
  }

  private inicializarFormulario(): void {
    this.perfilForm = this.fb.group({
      nome: [''],
      email: [''],
      senhaAtual: [''],
      confirmarSenhaAtual: [''],
      novaSenha: [''],
      confirmarNovaSenha: ['']
    });

    this.configurarCamposEstaticos();
  }

  private configurarCamposEstaticos(nome = ' ', email = ' '): void {
    this.camposDetalhes = [
      { key: 'nome', label: 'Nome', placeholder: nome },
      { key: 'email', label: 'Email', placeholder: email }
    ];

    this.camposSenha = [
      { key: 'senhaAtual', label: 'Mudar senha', placeholder: 'Coloque senha atual...', type: 'password' },
      { key: 'confirmarSenhaAtual', label: '\u00A0', placeholder: 'Confirme senha...', type: 'password' },
      { key: 'novaSenha', label: 'Nova senha', placeholder: 'Coloque sua nova senha...', type: 'password' },
      { key: 'confirmarNovaSenha', label: '\u00A0', placeholder: 'Confirme nova senha...', type: 'password' }
    ];
  }

  private carregarDadosIniciais(): void {
    this.usuarioService.obterPerfilCompleto().subscribe({
      next: (usuario) => {
        // Removida a necessidade de guardar o usuarioId localmente
        this.configurarCamposEstaticos(
          usuario.nome,
          usuario.email
        );
        this.cdr.detectChanges(); // Força a atualização da view após a mudança de estado
      },
      error: (err) => {
        console.error('Erro ao alimentar os placeholders da tela de ajustes:', err);
      }
    });
  }

  public salvarMudancas(): void {
    const valoresFormulario = this.perfilForm.value;
    const dadosParciaisPatch: any = {};

    // 1. Verifica se o nome foi digitado
    if (valoresFormulario.nome && valoresFormulario.nome.trim() !== '') {
      dadosParciaisPatch.nome = valoresFormulario.nome.trim();
    }

    // 2. Verifica se o e-mail foi digitado
    if (valoresFormulario.email && valoresFormulario.email.trim() !== '') {
      dadosParciaisPatch.email = valoresFormulario.email.trim();
    }

    // 3. Validação e inclusão da senha se qualquer campo de senha foi digitado
    const camposSenha = [
      valoresFormulario.senhaAtual,
      valoresFormulario.confirmarSenhaAtual,
      valoresFormulario.novaSenha,
      valoresFormulario.confirmarNovaSenha
    ];
    const alterandoSenha = camposSenha.some(this.temTexto);

    if (alterandoSenha && !camposSenha.every(this.temTexto)) {
      this.messageService.add({
        severity: 'error',
        summary: 'Dados incompletos',
        detail: 'Preencha todos os campos para alterar a senha.',
        life: 3000
      });
      return;
    }

    if (alterandoSenha && valoresFormulario.senhaAtual !== valoresFormulario.confirmarSenhaAtual) {
      this.messageService.add({
        severity: 'error',
        summary: 'Erro',
        detail: 'A confirmação da senha atual não confere.',
        life: 3000
      });
      return;
    }

    if (alterandoSenha && valoresFormulario.novaSenha !== valoresFormulario.confirmarNovaSenha) {
      this.messageService.add({
        severity: 'error',
        summary: 'Erro',
        detail: 'As senhas não conferem.',
        life: 3000
      });
      return;
    }

    if (alterandoSenha) {
      // Casando com o campo do record UsuarioUpdateDTO no Java
      dadosParciaisPatch.senhaAtual = valoresFormulario.senhaAtual;
      dadosParciaisPatch.senha = valoresFormulario.novaSenha;
    }

    // Se nenhum campo foi digitado, mata a execução e evita requisição desnecessária
    if (Object.keys(dadosParciaisPatch).length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Nenhuma alteração',
        detail: 'Preencha pelo menos um campo para salvar.',
        life: 3000
      });
      return;
    }

    // 4. Dispara a requisição PATCH para /usuario/me (Sem passar ID na URL)
    this.usuarioService.atualizarParcial(dadosParciaisPatch).subscribe({
      next: (res: any) => {
        if (res.token) {
          StorageService.salvarToken(res.token);
        }

        const usuario = res.usuario ?? res;

        this.perfilForm.reset();
        this.configurarCamposEstaticos(usuario.nome, usuario.email);
        this.cdr.detectChanges();
        this.messageService.add({
          severity: 'success',
          summary: 'Alterações salvas com sucesso',
          life: 2000
        });
        setTimeout(() => window.location.reload(), 2100);
      },
      error: (err) => {
        console.error('Erro ao salvar via PATCH:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao salvar alterações',
          detail: typeof err.error === 'string'
            ? err.error
            : err.error?.message || 'Não foi possível atualizar o perfil.',
          life: 4000
        });
      }
    });
  }
} 