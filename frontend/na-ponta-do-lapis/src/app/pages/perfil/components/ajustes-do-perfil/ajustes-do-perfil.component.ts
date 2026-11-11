import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
<<<<<<< HEAD
<<<<<<< HEAD
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
=======
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
import { ActivatedRoute } from '@angular/router';
import { BotoesAcaoComponent } from '../botoes-acao/botoes-acao.component';
import { FormularioSecaoComponent, CampoFormulario } from '../formulario-secao/formulario-secao.component';
import { IntegracoesPerfilComponent } from '../integracoes-perfil/integracoes-perfil.component';
<<<<<<< HEAD
<<<<<<< HEAD
import { ConquistasPerfilComponent } from '../conquistas-perfil/conquistas-perfil.component';
import { IconeBandeiraComponent } from '../../../../shared/components/icone-bandeira/icone-bandeira.component';
=======
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
import { ConquistasPerfilComponent } from '../conquistas-perfil/conquistas-perfil.component';
>>>>>>> 4b9c67c (feat(gamificacao): implementar sistema de conquistas, niveis de saude financeira, xp e aba no perfil)
import { UsuarioService } from '../../service/perfil.services';
import { ChangeDetectorRef } from '@angular/core';
import { StorageService } from '../../../../auth/service/storage.service';
import { MessageService } from 'primeng/api';
<<<<<<< HEAD
<<<<<<< HEAD
import { PreferenciaMoedaService, OpcaoMoeda } from '../../../../shared/service/preferencia-moeda.service';
import { Usuario } from '../../../../model/IUsuario.models';
<<<<<<< HEAD
=======
>>>>>>> 6119e15 (feat: add messages of status from changes in perfil and add logic to send senhaAtual and verifications in frontend.)
=======
import { PreferenciaMoedaService, OpcaoMoeda } from '../../../../shared/service/preferencia-moeda.service';
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)

@Component({
  selector: 'app-ajustes-do-perfil',
  standalone: true,
<<<<<<< HEAD
<<<<<<< HEAD
  imports: [CommonModule, ReactiveFormsModule, FormularioSecaoComponent, BotoesAcaoComponent, IntegracoesPerfilComponent, ConquistasPerfilComponent, IconeBandeiraComponent],
=======
  imports: [CommonModule, ReactiveFormsModule, FormularioSecaoComponent, BotoesAcaoComponent, IntegracoesPerfilComponent],
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
  imports: [CommonModule, ReactiveFormsModule, FormularioSecaoComponent, BotoesAcaoComponent, IntegracoesPerfilComponent, ConquistasPerfilComponent],
>>>>>>> 4b9c67c (feat(gamificacao): implementar sistema de conquistas, niveis de saude financeira, xp e aba no perfil)
  templateUrl: './ajustes-do-perfil.component.html'
})
export class AjustesPerfilComponent implements OnInit {
  public perfilForm!: FormGroup;
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 4b9c67c (feat(gamificacao): implementar sistema de conquistas, niveis de saude financeira, xp e aba no perfil)
  public abaAtiva: 'dados' | 'conquistas' | 'integracoes' = 'dados';
  public moedaOriginalUsuario: string = 'BRL';
  public usuarioLogado: Usuario | null = null;

  // Controle de modo de edição (dados e senha iniciam recolhidos/em modo de leitura)
  public editandoDados: boolean = false;
  public editandoSenha: boolean = false;
<<<<<<< HEAD
=======
  public abaAtiva: 'dados' | 'integracoes' = 'dados';
<<<<<<< HEAD
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
  public moedaOriginalUsuario: string = 'BRL';
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)

  public camposDetalhes: CampoFormulario[] = [];
  public camposSenha: CampoFormulario[] = [];

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
  get opcoesMoeda(): OpcaoMoeda[] {
    return this.preferenciaMoedaService.opcoesMoeda;
  }

<<<<<<< HEAD
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
=======
=======
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
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

<<<<<<< HEAD
    return [valores.nome, valores.email].some(this.temTexto);
>>>>>>> 6119e15 (feat: add messages of status from changes in perfil and add logic to send senhaAtual and verifications in frontend.)
=======
    const alterouMoeda = valores.moedaPadrao && valores.moedaPadrao !== this.moedaOriginalUsuario;
<<<<<<< HEAD
    return [valores.nome, valores.email].some(this.temTexto) || !!alterouMoeda;
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
=======
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
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
  }

  private readonly temTexto = (valor: unknown): boolean =>
    typeof valor === 'string' && valor.trim() !== '';

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
<<<<<<< HEAD
<<<<<<< HEAD
    private cdr: ChangeDetectorRef,
    private messageService: MessageService,
    private preferenciaMoedaService: PreferenciaMoedaService
=======
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
    private cdr: ChangeDetectorRef,
<<<<<<< HEAD
    private messageService: MessageService
>>>>>>> 6119e15 (feat: add messages of status from changes in perfil and add logic to send senhaAtual and verifications in frontend.)
=======
    private messageService: MessageService,
    private preferenciaMoedaService: PreferenciaMoedaService
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
  ) {
    this.inicializarFormulario();
  }

  ngOnInit(): void {
    this.carregarDadosIniciais();
    this.route.queryParams.subscribe((params) => {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 4b9c67c (feat(gamificacao): implementar sistema de conquistas, niveis de saude financeira, xp e aba no perfil)
      if (params['aba'] === 'conquistas' || params['tab'] === 'conquistas') {
        this.abaAtiva = 'conquistas';
        this.cdr.detectChanges();
      } else if (params['aba'] === 'integracoes' || params['tab'] === 'integracoes' || params['aba'] === 'ia' || params['secao'] === 'ia') {
<<<<<<< HEAD
=======
      if (params['aba'] === 'integracoes' || params['tab'] === 'integracoes') {
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
      if (params['aba'] === 'integracoes' || params['tab'] === 'integracoes' || params['aba'] === 'ia' || params['secao'] === 'ia') {
>>>>>>> f17e760 (feat(contas): melhora layout, diferenciacao de cartao/conta e cores de bancos)
=======
>>>>>>> 4b9c67c (feat(gamificacao): implementar sistema de conquistas, niveis de saude financeira, xp e aba no perfil)
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

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
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

<<<<<<< HEAD
=======
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
  public selecionarMoedaPadrao(codigo: string): void {
    this.perfilForm.get('moedaPadrao')?.setValue(codigo);
    this.perfilForm.markAsDirty();
    this.cdr.detectChanges();
  }

  public obterSimboloMoeda(moeda?: string): string {
    return this.preferenciaMoedaService.obterSimboloMoeda(moeda);
  }

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
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
<<<<<<< HEAD
=======
  private configurarCamposEstaticos(nome = ' ', email = ' '): void {
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
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
<<<<<<< HEAD
<<<<<<< HEAD
        this.usuarioLogado = usuario;
=======
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
=======
        this.usuarioLogado = usuario;
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
        if (usuario.moedaPadrao) {
          this.moedaOriginalUsuario = usuario.moedaPadrao;
          this.perfilForm.get('moedaPadrao')?.setValue(usuario.moedaPadrao);
          this.preferenciaMoedaService.definirMoedaPadrao(usuario.moedaPadrao);
        }
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
        this.perfilForm.patchValue({
          nome: usuario.nome || '',
          email: usuario.email || ''
        });
        this.configurarCamposEstaticos(usuario.nome, usuario.email);
<<<<<<< HEAD
=======
        this.configurarCamposEstaticos(
          usuario.nome,
          usuario.email
        );
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
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

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
    // 1. Verifica se o nome foi alterado
    if (this.editandoDados && valoresFormulario.nome && valoresFormulario.nome.trim() !== '') {
      if (valoresFormulario.nome.trim() !== this.usuarioLogado?.nome) {
        dadosParciaisPatch.nome = valoresFormulario.nome.trim();
      }
<<<<<<< HEAD
=======
    // 1. Verifica se o nome foi digitado
    if (valoresFormulario.nome && valoresFormulario.nome.trim() !== '') {
      dadosParciaisPatch.nome = valoresFormulario.nome.trim();
=======
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
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

<<<<<<< HEAD
    if (alterandoSenha) {
      dadosParciaisPatch.senhaAtual = valoresFormulario.senhaAtual;
      dadosParciaisPatch.senha = valoresFormulario.novaSenha;
>>>>>>> 6119e15 (feat: add messages of status from changes in perfil and add logic to send senhaAtual and verifications in frontend.)
=======
        dadosParciaisPatch.senhaAtual = valoresFormulario.senhaAtual;
        dadosParciaisPatch.senha = valoresFormulario.novaSenha;
      }
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
    }

<<<<<<< HEAD
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

=======
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
    // Se nenhum campo foi alterado, emite aviso
    if (Object.keys(dadosParciaisPatch).length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Nenhuma alteração',
<<<<<<< HEAD
<<<<<<< HEAD
        detail: 'Altere pelo menos um campo ou a moeda para salvar.',
=======
        detail: 'Preencha pelo menos um campo para salvar.',
>>>>>>> 6119e15 (feat: add messages of status from changes in perfil and add logic to send senhaAtual and verifications in frontend.)
=======
        detail: 'Altere pelo menos um campo ou a moeda para salvar.',
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
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

<<<<<<< HEAD
<<<<<<< HEAD
        this.editandoDados = false;
        this.editandoSenha = false;
        this.perfilForm.patchValue({
          senhaAtual: '',
=======
        this.perfilForm.patchValue({
          senhaAtual: '',
          confirmarSenhaAtual: '',
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
=======
        this.editandoDados = false;
        this.editandoSenha = false;
        this.perfilForm.patchValue({
          senhaAtual: '',
>>>>>>> b763965 (feat: reformular tela de ajustes com boas praticas de ui e ux e identidade visual)
          novaSenha: '',
          confirmarNovaSenha: ''
        });
        this.configurarCamposEstaticos(usuario.nome, usuario.email);
        this.cdr.detectChanges();
        this.messageService.add({
          severity: 'success',
          summary: 'Alterações salvas com sucesso',
<<<<<<< HEAD
<<<<<<< HEAD
          detail: dadosParciaisPatch.moedaPadrao ? `Moeda padrão atualizada para ${dadosParciaisPatch.moedaPadrao}.` : undefined,
          life: 2000
        });
        setTimeout(() => window.location.reload(), 1500);
=======
          life: 2000
        });
        setTimeout(() => window.location.reload(), 2100);
>>>>>>> 6119e15 (feat: add messages of status from changes in perfil and add logic to send senhaAtual and verifications in frontend.)
=======
          detail: dadosParciaisPatch.moedaPadrao ? `Moeda padrão atualizada para ${dadosParciaisPatch.moedaPadrao}.` : undefined,
          life: 2000
        });
        setTimeout(() => window.location.reload(), 1500);
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
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
