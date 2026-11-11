import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BotoesAcaoComponent } from '../botoes-acao/botoes-acao.component';
import { FormularioSecaoComponent, CampoFormulario } from '../formulario-secao/formulario-secao.component';
import { IntegracoesPerfilComponent } from '../integracoes-perfil/integracoes-perfil.component';
import { ConquistasPerfilComponent } from '../conquistas-perfil/conquistas-perfil.component';
import { UsuarioService } from '../../service/perfil.services';
import { ChangeDetectorRef } from '@angular/core';
import { StorageService } from '../../../../auth/service/storage.service';
import { MessageService } from 'primeng/api';
import { PreferenciaMoedaService, OpcaoMoeda } from '../../../../shared/service/preferencia-moeda.service';
import { Usuario } from '../../../../model/IUsuario.models';

@Component({
  selector: 'app-ajustes-do-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormularioSecaoComponent, BotoesAcaoComponent, IntegracoesPerfilComponent, ConquistasPerfilComponent],
  templateUrl: './ajustes-do-perfil.component.html'
})
export class AjustesPerfilComponent implements OnInit {
  public perfilForm!: FormGroup;
  public abaAtiva: 'dados' | 'conquistas' | 'integracoes' = 'dados';
  public moedaOriginalUsuario: string = 'BRL';
  public usuarioLogado: Usuario | null = null;

  // Controle de modo de edição (dados e senha iniciam recolhidos/em modo de leitura)
  public editandoDados: boolean = false;
  public editandoSenha: boolean = false;

  public camposDetalhes: CampoFormulario[] = [];
  public camposSenha: CampoFormulario[] = [];

  get opcoesMoeda(): OpcaoMoeda[] {
    return this.preferenciaMoedaService.opcoesMoeda;
  }

  get podeAbrirConfirmacao(): boolean {
    const valores = this.perfilForm?.value ?? {};
    
    // Validação da alteração de senha (3 campos: senhaAtual, novaSenha, confirmarNovaSenha)
    if (this.editandoSenha) {
      const camposSenha = [valores.senhaAtual, valores.novaSenha, valores.confirmarNovaSenha];
      const preencheuAlgum = camposSenha.some(this.temTexto);
      if (preencheuAlgum) {
        return camposSenha.every(this.temTexto)
          && valores.novaSenha === valores.confirmarNovaSenha
          && valores.novaSenha.length >= 6;
      }
    }

    const alterouMoeda = valores.moedaPadrao && valores.moedaPadrao !== this.moedaOriginalUsuario;
    const alterouNome = this.editandoDados && valores.nome?.trim() && valores.nome?.trim() !== this.usuarioLogado?.nome;
    const alterouEmail = this.editandoDados && valores.email?.trim() && valores.email?.trim() !== this.usuarioLogado?.email;

    return !!alterouNome || !!alterouEmail || !!alterouMoeda || this.editandoSenha;
  }

  get temAlteracoesPendentes(): boolean {
    const valores = this.perfilForm?.value ?? {};
    const alterouNome = this.editandoDados && valores.nome?.trim() && valores.nome?.trim() !== this.usuarioLogado?.nome;
    const alterouEmail = this.editandoDados && valores.email?.trim() && valores.email?.trim() !== this.usuarioLogado?.email;
    const alterouMoeda = valores.moedaPadrao && valores.moedaPadrao !== this.moedaOriginalUsuario;
    
    const alterouSenha = this.editandoSenha && (
      this.temTexto(valores.senhaAtual) || 
      this.temTexto(valores.novaSenha) || 
      this.temTexto(valores.confirmarNovaSenha)
    );

    return !!alterouNome || !!alterouEmail || !!alterouMoeda || !!alterouSenha;
  }

  private readonly temTexto = (valor: unknown): boolean =>
    typeof valor === 'string' && valor.trim() !== '';

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private messageService: MessageService,
    private preferenciaMoedaService: PreferenciaMoedaService
  ) {
    this.inicializarFormulario();
  }

  ngOnInit(): void {
    this.carregarDadosIniciais();
    this.route.queryParams.subscribe((params) => {
      if (params['aba'] === 'conquistas' || params['tab'] === 'conquistas') {
        this.abaAtiva = 'conquistas';
        this.cdr.detectChanges();
      } else if (params['aba'] === 'integracoes' || params['tab'] === 'integracoes' || params['aba'] === 'ia' || params['secao'] === 'ia') {
        this.abaAtiva = 'integracoes';
        this.cdr.detectChanges();
      }
    });
  }

  private inicializarFormulario(): void {
    const moedaInicial = this.preferenciaMoedaService.obterMoedaAtiva();
    this.moedaOriginalUsuario = moedaInicial;
    this.perfilForm = this.fb.group({
      nome: [''],
      email: [''],
      senhaAtual: [''],
      novaSenha: [''],
      confirmarNovaSenha: [''],
      moedaPadrao: [moedaInicial]
    });

    this.configurarCamposEstaticos();
  }

  public iniciarEdicaoDados(): void {
    this.editandoDados = true;
    this.perfilForm.patchValue({
      nome: this.usuarioLogado?.nome || '',
      email: this.usuarioLogado?.email || ''
    });
    this.configurarCamposEstaticos(this.usuarioLogado?.nome, this.usuarioLogado?.email);
    this.cdr.detectChanges();
  }

  public cancelarEdicaoDados(): void {
    this.editandoDados = false;
    this.perfilForm.patchValue({
      nome: this.usuarioLogado?.nome || '',
      email: this.usuarioLogado?.email || ''
    });
    this.cdr.detectChanges();
  }

  public alternarEdicaoSenha(): void {
    this.editandoSenha = !this.editandoSenha;
    if (!this.editandoSenha) {
      this.perfilForm.patchValue({
        senhaAtual: '',
        novaSenha: '',
        confirmarNovaSenha: ''
      });
    }
    this.cdr.detectChanges();
  }

  public selecionarMoedaPadrao(codigo: string): void {
    this.perfilForm.get('moedaPadrao')?.setValue(codigo);
    this.perfilForm.markAsDirty();
    this.cdr.detectChanges();
  }

  public obterSimboloMoeda(moeda?: string): string {
    return this.preferenciaMoedaService.obterSimboloMoeda(moeda);
  }

  public descartarAlteracoes(): void {
    this.editandoDados = false;
    this.editandoSenha = false;
    this.perfilForm.reset({
      nome: this.usuarioLogado?.nome || '',
      email: this.usuarioLogado?.email || '',
      senhaAtual: '',
      novaSenha: '',
      confirmarNovaSenha: '',
      moedaPadrao: this.moedaOriginalUsuario
    });
    this.configurarCamposEstaticos(this.usuarioLogado?.nome, this.usuarioLogado?.email);
    this.messageService.add({
      severity: 'info',
      summary: 'Alterações descartadas',
      detail: 'O formulário foi restaurado com os dados originais.',
      life: 2500
    });
    this.cdr.detectChanges();
  }

  private configurarCamposEstaticos(nome = '', email = ''): void {
    this.camposDetalhes = [
      { 
        key: 'nome', 
        label: 'Nome completo', 
        placeholder: nome?.trim() || 'Digite seu nome completo',
        icon: 'pi pi-user'
      },
      { 
        key: 'email', 
        label: 'E-mail de acesso', 
        placeholder: email?.trim() || 'Digite seu e-mail',
        type: 'email',
        icon: 'pi pi-envelope'
      }
    ];

    // Exatamente 3 campos para alteração de senha:
    // Linha 1: Senha atual (largura completa)
    // Linha 2: Nova senha e Confirmar nova senha lado a lado
    this.camposSenha = [
      { 
        key: 'senhaAtual', 
        label: 'Senha atual', 
        placeholder: 'Digite sua senha atual...', 
        type: 'password',
        icon: 'pi pi-lock',
        colSpan: 2
      },
      { 
        key: 'novaSenha', 
        label: 'Nova senha', 
        placeholder: 'Mínimo de 6 caracteres...', 
        type: 'password',
        icon: 'pi pi-key',
        colSpan: 1
      },
      { 
        key: 'confirmarNovaSenha', 
        label: 'Confirmar nova senha', 
        placeholder: 'Repita a nova senha...', 
        type: 'password',
        icon: 'pi pi-key',
        colSpan: 1
      }
    ];
  }

  private carregarDadosIniciais(): void {
    this.usuarioService.obterPerfilCompleto().subscribe({
      next: (usuario) => {
        this.usuarioLogado = usuario;
        if (usuario.moedaPadrao) {
          this.moedaOriginalUsuario = usuario.moedaPadrao;
          this.perfilForm.get('moedaPadrao')?.setValue(usuario.moedaPadrao);
          this.preferenciaMoedaService.definirMoedaPadrao(usuario.moedaPadrao);
        }
        this.perfilForm.patchValue({
          nome: usuario.nome || '',
          email: usuario.email || ''
        });
        this.configurarCamposEstaticos(usuario.nome, usuario.email);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao alimentar tela de ajustes:', err);
      }
    });
  }

  public salvarMudancas(): void {
    const valoresFormulario = this.perfilForm.value;
    const dadosParciaisPatch: any = {};

    // 1. Verifica se o nome foi alterado
    if (this.editandoDados && valoresFormulario.nome && valoresFormulario.nome.trim() !== '') {
      if (valoresFormulario.nome.trim() !== this.usuarioLogado?.nome) {
        dadosParciaisPatch.nome = valoresFormulario.nome.trim();
      }
    }

    // 2. Verifica se o e-mail foi alterado
    if (this.editandoDados && valoresFormulario.email && valoresFormulario.email.trim() !== '') {
      if (valoresFormulario.email.trim() !== this.usuarioLogado?.email) {
        dadosParciaisPatch.email = valoresFormulario.email.trim();
      }
    }

    // 3. Verifica se a moeda padrão foi alterada
    if (valoresFormulario.moedaPadrao && valoresFormulario.moedaPadrao !== this.moedaOriginalUsuario) {
      dadosParciaisPatch.moedaPadrao = valoresFormulario.moedaPadrao;
    }

    // 4. Validação e inclusão da senha se a seção de senha estiver aberta e preenchida
    if (this.editandoSenha) {
      const camposSenha = [
        valoresFormulario.senhaAtual,
        valoresFormulario.novaSenha,
        valoresFormulario.confirmarNovaSenha
      ];
      const alterandoSenha = camposSenha.some(this.temTexto);

      if (alterandoSenha) {
        if (!camposSenha.every(this.temTexto)) {
          this.messageService.add({
            severity: 'error',
            summary: 'Dados incompletos',
            detail: 'Preencha a senha atual e a nova senha para atualizar.',
            life: 3000
          });
          return;
        }

        if (valoresFormulario.novaSenha !== valoresFormulario.confirmarNovaSenha) {
          this.messageService.add({
            severity: 'error',
            summary: 'Erro na nova senha',
            detail: 'A nova senha e a confirmação não conferem.',
            life: 3000
          });
          return;
        }

        if (valoresFormulario.novaSenha.length < 6) {
          this.messageService.add({
            severity: 'error',
            summary: 'Senha curta',
            detail: 'A nova senha deve ter no mínimo 6 caracteres.',
            life: 3000
          });
          return;
        }

        dadosParciaisPatch.senhaAtual = valoresFormulario.senhaAtual;
        dadosParciaisPatch.senha = valoresFormulario.novaSenha;
      }
    }

    // Se nenhum campo foi alterado, emite aviso
    if (Object.keys(dadosParciaisPatch).length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Nenhuma alteração',
        detail: 'Altere pelo menos um campo ou a moeda para salvar.',
        life: 3000
      });
      return;
    }

    // Dispara a requisição PATCH para /usuario/me
    this.usuarioService.atualizarParcial(dadosParciaisPatch).subscribe({
      next: (res: any) => {
        if (res.token) {
          StorageService.salvarToken(res.token);
        }

        const usuario = res.usuario ?? res;
        this.usuarioLogado = usuario;

        if (dadosParciaisPatch.moedaPadrao) {
          this.moedaOriginalUsuario = dadosParciaisPatch.moedaPadrao;
          this.preferenciaMoedaService.definirMoedaPadrao(dadosParciaisPatch.moedaPadrao);
        }

        this.editandoDados = false;
        this.editandoSenha = false;
        this.perfilForm.patchValue({
          senhaAtual: '',
          novaSenha: '',
          confirmarNovaSenha: ''
        });
        this.configurarCamposEstaticos(usuario.nome, usuario.email);
        this.cdr.detectChanges();
        this.messageService.add({
          severity: 'success',
          summary: 'Alterações salvas com sucesso',
          detail: dadosParciaisPatch.moedaPadrao ? `Moeda padrão atualizada para ${dadosParciaisPatch.moedaPadrao}.` : undefined,
          life: 2000
        });
        setTimeout(() => window.location.reload(), 1500);
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
