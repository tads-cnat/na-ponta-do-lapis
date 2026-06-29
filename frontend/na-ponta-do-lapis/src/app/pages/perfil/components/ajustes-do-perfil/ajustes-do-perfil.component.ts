import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BotoesAcaoComponent } from '../botoes-acao/botoes-acao.component';
import { FormularioSecaoComponent, CampoFormulario } from '../formulario-secao/formulario-secao.component';
import { UsuarioService } from '../../service/perfil.services'; // Certifique-se de que este caminho aponta para o service correto

@Component({
  selector: 'app-ajustes-do-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormularioSecaoComponent, BotoesAcaoComponent],
  templateUrl: './ajustes-do-perfil.component.html'
})
export class AjustesPerfilComponent implements OnInit {
  public perfilForm!: FormGroup;

  public camposDetalhes: CampoFormulario[] = [];
  public camposSenha: CampoFormulario[] = [];

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService
  ) {
    this.inicializarFormulario();
  }

  ngOnInit(): void {
    this.carregarDadosIniciais();
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

    // 3. Validação e inclusão da senha se digitada
    if (valoresFormulario.novaSenha && valoresFormulario.novaSenha.trim() !== '') {
      if (valoresFormulario.novaSenha !== valoresFormulario.confirmarNovaSenha) {
        return;
      }
      // Casando com o campo do record UsuarioUpdateDTO no Java
      dadosParciaisPatch.senha = valoresFormulario.novaSenha;
    }

    // Se nenhum campo foi digitado, mata a execução e evita requisição desnecessária
    if (Object.keys(dadosParciaisPatch).length === 0) {
      return;
    }

    // 4. Dispara a requisição PATCH para /usuario/me (Sem passar ID na URL)
    this.usuarioService.atualizarParcial(dadosParciaisPatch).subscribe({
      next: (usuarioAtualizado) => {
        this.perfilForm.reset();
        
        // Atualiza dinamicamente os placeholders na tela com os novos valores retornados pelo banco
        this.configurarCamposEstaticos(usuarioAtualizado.nome, usuarioAtualizado.email);
        
        // Opcional: força o reload se quiser que a barra lateral ou outros componentes escutem a mudança
        window.location.reload();
      },
      error: (err) => {
        console.error('Erro ao salvar via PATCH:', err);
      }
    });
  }
} 