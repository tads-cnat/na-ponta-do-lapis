import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TooltipModule } from 'primeng/tooltip';
import { IntegracoesService } from '../../../../service/integracoes.service';
import { IconeBancoComponent } from '../../../../shared/components/icone-banco/icone-banco.component';
import { mapearNomeBanco } from '../../../../shared/utils/banco-icon.helper';
import {
  IChaveIaRequest,
  IChaveIaResponse,
  IOpenFinanceItem,
  IOpenFinanceStatus,
  ProvedorIa
} from '../../../../model/IIntegracoes.models';

@Component({
  selector: 'app-integracoes-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TooltipModule, IconeBancoComponent],
  templateUrl: './integracoes-perfil.component.html',
  styleUrl: './integracoes-perfil.component.css'
})
export class IntegracoesPerfilComponent implements OnInit {
  // Open Finance
  public openFinanceStatus: IOpenFinanceStatus | null = null;
  public openFinanceForm!: FormGroup;
  public carregandoOpenFinance = false;
  public sincronizandoOpenFinance = false;
  public openFinanceMensagem: { tipo: 'sucesso' | 'erro'; texto: string } | null = null;
  public mostrarTutorialConectado = false;

  // Itens Bancários Conectados
  public itensConectados: IOpenFinanceItem[] = [];
  public carregandoItens = false;
  public novoItemId = '';
  public salvandoItem = false;

  // Chaves de IA
  public chavesIa: IChaveIaResponse[] = [];
  public carregandoChavesIa = false;
  public modalNovaChaveAberta = false;
  public novaChaveForm!: FormGroup;
  public salvandoChaveIa = false;
  public chaveIaMensagem: { tipo: 'sucesso' | 'erro'; texto: string } | null = null;

  // Sub-aba ativa (Open Finance vs IA)
  public subabaAtiva: 'bancos' | 'ia' = 'bancos';

  public provedoresDisponiveis: { valor: ProvedorIa; label: string }[] = [
    { valor: 'GEMINI', label: 'Google Gemini' },
    { valor: 'CLAUDE', label: 'Anthropic Claude' },
    { valor: 'OPENAI', label: 'OpenAI (GPT)' },
    { valor: 'DEEPSEEK', label: 'DeepSeek' },
    { valor: 'GROQ', label: 'Groq (Llama / Mixtral)' }
  ];

  constructor(
    private integracoesService: IntegracoesService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      if (params['secao'] === 'ia' || params['aba'] === 'ia' || params['subaba'] === 'ia') {
        this.subabaAtiva = 'ia';
        this.cdr.detectChanges();
      } else if (params['secao'] === 'bancos' || params['secao'] === 'openfinance') {
        this.subabaAtiva = 'bancos';
        this.cdr.detectChanges();
      }
    });

    this.inicializarFormularios();
    this.carregarOpenFinance();
    this.carregarChavesIa();
  }

  private inicializarFormularios(): void {
    this.openFinanceForm = this.fb.group({
      clientId: ['', [Validators.required]],
      clientSecret: ['', [Validators.required]],
      itemIds: ['']
    });

    this.novaChaveForm = this.fb.group({
      apelido: ['', [Validators.required, Validators.maxLength(50)]],
      provedor: ['GEMINI', [Validators.required]],
      apiKey: ['', [Validators.required]],
      padrao: [false]
    });
  }

  // --- Open Finance ---
  public carregarOpenFinance(): void {
    this.carregandoOpenFinance = true;
    this.integracoesService.obterStatusOpenFinance().subscribe({
      next: (status) => {
        this.openFinanceStatus = status;
        this.carregandoOpenFinance = false;
        if (status.conectado) {
          this.carregarItensConectados();
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao carregar status do Open Finance:', err);
        this.carregandoOpenFinance = false;
        this.cdr.detectChanges();
      }
    });
  }

  public carregarItensConectados(): void {
    this.carregandoItens = true;
    this.integracoesService.listarItensOpenFinance().subscribe({
      next: (itens) => {
        this.itensConectados = itens;
        this.carregandoItens = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao listar itens conectados:', err);
        this.carregandoItens = false;
        this.cdr.detectChanges();
      }
    });
  }

  public getNomeBancoFormatado(nome?: string): string {
    return mapearNomeBanco(nome).nomeExibicao;
  }

  public salvarNovoItemPorId(): void {
    if (!this.novoItemId.trim()) {
      return;
    }

    this.salvandoItem = true;
    this.openFinanceMensagem = null;

    this.integracoesService.adicionarItemOpenFinance(this.novoItemId.trim()).subscribe({
      next: (status) => {
        this.openFinanceStatus = status;
        this.salvandoItem = false;
        this.novoItemId = '';
        this.openFinanceMensagem = {
          tipo: 'sucesso',
          texto: 'Banco adicionado com sucesso! Contas e lançamentos sincronizados.'
        };
        this.carregarItensConectados();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao adicionar item por ID:', err);
        this.salvandoItem = false;
        const msg = err.error?.message || 'Item ID não encontrado na sua conta Pluggy. Verifique no Meu Pluggy.';
        this.openFinanceMensagem = { tipo: 'erro', texto: msg };
        this.cdr.detectChanges();
      }
    });
  }

  public removerItemConectado(item: IOpenFinanceItem): void {
    if (!confirm(`Deseja desvincular a conexão com "${item.nomeInstituicao}"?`)) {
      return;
    }

    this.integracoesService.removerItemOpenFinance(item.itemId).subscribe({
      next: () => {
        this.openFinanceMensagem = {
          tipo: 'sucesso',
          texto: `Conexão com "${item.nomeInstituicao}" desvinculada.`
        };
        this.carregarItensConectados();
        this.sincronizarAgora();
      },
      error: (err) => {
        console.error('Erro ao remover item:', err);
        this.openFinanceMensagem = { tipo: 'erro', texto: 'Não foi possível desvincular a conexão.' };
      }
    });
  }

  public conectarOpenFinance(): void {
    if (this.openFinanceForm.invalid) {
      this.openFinanceForm.markAllAsTouched();
      return;
    }

    this.carregandoOpenFinance = true;
    this.openFinanceMensagem = null;

    const payload = this.openFinanceForm.value;
    this.integracoesService.conectarOpenFinance(payload).subscribe({
      next: (status) => {
        this.openFinanceStatus = status;
        this.openFinanceForm.reset();
        this.carregarItensConectados();
        this.openFinanceMensagem = {
          tipo: 'sucesso',
          texto: 'Pluggy conectada com sucesso! Suas contas e transações foram sincronizadas.'
        };
        this.carregandoOpenFinance = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao conectar Open Finance:', err);
        const msg = err.error?.message || 'Falha ao autenticar com as credenciais informadas. Verifique seu Client ID e Client Secret.';
        this.openFinanceMensagem = { tipo: 'erro', texto: msg };
        this.carregandoOpenFinance = false;
        this.cdr.detectChanges();
      }
    });
  }

  public sincronizarAgora(): void {
    if (!this.openFinanceStatus?.conectado || this.sincronizandoOpenFinance) {
      return;
    }

    this.sincronizandoOpenFinance = true;
    this.openFinanceMensagem = null;

    this.integracoesService.sincronizarOpenFinanceManual().subscribe({
      next: (status) => {
        this.openFinanceStatus = status;
        this.carregarItensConectados();
        this.openFinanceMensagem = {
          tipo: 'sucesso',
          texto: 'Sincronização manual concluída! Contas e lançamentos atualizados.'
        };
        this.sincronizandoOpenFinance = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro na sincronização manual:', err);
        const msg = err.error?.message || err.error || 'Limite diário de sincronizações manuais atingido ou erro de conexão.';
        this.openFinanceMensagem = { tipo: 'erro', texto: typeof msg === 'string' ? msg : 'Erro ao sincronizar.' };
        this.sincronizandoOpenFinance = false;
        this.cdr.detectChanges();
      }
    });
  }

  public desconectarOpenFinance(): void {
    if (!confirm('Deseja realmente desconectar a integração com a Pluggy? Suas contas e transações já registradas serão preservadas.')) {
      return;
    }

    this.carregandoOpenFinance = true;
    this.openFinanceMensagem = null;

    this.integracoesService.desconectarOpenFinance().subscribe({
      next: () => {
        this.openFinanceStatus = {
          conectado: false,
          ultimaSincronizacao: null,
          syncsRestantesHoje: 5,
          limiteDiario: 5,
          totalContasSincronizadas: 0
        };
        this.itensConectados = [];
        this.openFinanceMensagem = {
          tipo: 'sucesso',
          texto: 'Integração desconectada com sucesso.'
        };
        this.carregandoOpenFinance = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao desconectar:', err);
        this.openFinanceMensagem = { tipo: 'erro', texto: 'Não foi possível desconectar a integração.' };
        this.carregandoOpenFinance = false;
        this.cdr.detectChanges();
      }
    });
  }

  // --- Chaves de IA ---
  public carregarChavesIa(): void {
    this.carregandoChavesIa = true;
    this.integracoesService.listarChavesIa().subscribe({
      next: (chaves) => {
        this.chavesIa = chaves;
        this.carregandoChavesIa = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao listar chaves de IA:', err);
        this.carregandoChavesIa = false;
        this.cdr.detectChanges();
      }
    });
  }

  public abrirModalNovaChave(): void {
    this.novaChaveForm.reset({
      apelido: '',
      provedor: 'GEMINI',
      apiKey: '',
      padrao: this.chavesIa.length === 0
    });
    this.chaveIaMensagem = null;
    this.modalNovaChaveAberta = true;
  }

  public fecharModalNovaChave(): void {
    this.modalNovaChaveAberta = false;
    this.novaChaveForm.reset();
  }

  public salvarNovaChave(): void {
    if (this.novaChaveForm.invalid) {
      this.novaChaveForm.markAllAsTouched();
      return;
    }

    this.salvandoChaveIa = true;
    this.chaveIaMensagem = null;

    const payload: IChaveIaRequest = {
      apelido: this.novaChaveForm.value.apelido,
      provedor: this.novaChaveForm.value.provedor,
      apiKey: this.novaChaveForm.value.apiKey,
      padrao: !!this.novaChaveForm.value.padrao
    };

    this.integracoesService.cadastrarChaveIa(payload).subscribe({
      next: () => {
        this.salvandoChaveIa = false;
        this.fecharModalNovaChave();
        this.chaveIaMensagem = { tipo: 'sucesso', texto: 'Chave de IA cadastrada com sucesso!' };
        this.carregarChavesIa();
      },
      error: (err) => {
        console.error('Erro ao salvar chave de IA:', err);
        this.salvandoChaveIa = false;
        const msg = err.error?.message || (typeof err.error === 'string' ? err.error : null) || 'Erro ao salvar chave de IA. Verifique se os dados estão corretos.';
        this.chaveIaMensagem = { tipo: 'erro', texto: msg };
        this.cdr.detectChanges();
      }
    });
  }

  public definirChavePadrao(chave: IChaveIaResponse): void {
    if (chave.padrao) return;

    this.integracoesService.definirChaveIaPadrao(chave.id).subscribe({
      next: () => {
        this.chaveIaMensagem = { tipo: 'sucesso', texto: `Chave "${chave.apelido}" definida como padrão.` };
        this.carregarChavesIa();
      },
      error: (err) => {
        console.error('Erro ao definir chave padrão:', err);
        this.chaveIaMensagem = { tipo: 'erro', texto: 'Erro ao definir como chave padrão.' };
      }
    });
  }

  public excluirChave(chave: IChaveIaResponse): void {
    if (!confirm(`Deseja excluir a chave "${chave.apelido}"?`)) {
      return;
    }

    this.integracoesService.excluirChaveIa(chave.id).subscribe({
      next: () => {
        this.chaveIaMensagem = { tipo: 'sucesso', texto: 'Chave excluída com sucesso.' };
        this.carregarChavesIa();
      },
      error: (err) => {
        console.error('Erro ao excluir chave:', err);
        this.chaveIaMensagem = { tipo: 'erro', texto: 'Erro ao excluir chave.' };
      }
    });
  }

  public get provedorSelecionado(): ProvedorIa {
    return this.novaChaveForm?.get('provedor')?.value || 'GEMINI';
  }

  public get tutorialApiKey(): {
    provedorNome: string;
    url: string;
    labelLink: string;
    resumoTooltip: string;
    passos: string[];
  } {
    switch (this.provedorSelecionado) {
      case 'GEMINI':
        return {
          provedorNome: 'Google Gemini',
          url: 'https://aistudio.google.com/app/apikey',
          labelLink: 'Obter chave gratuita',
          resumoTooltip: 'Passo a passo rápido:\n1. Acesse o Google AI Studio com sua conta Google\n2. Clique em "Criar chave de API" (ou "Get API key")\n3. Copie o código da chave gerada',
          passos: [
            'Acesse o Google AI Studio com sua conta Google.',
            'Clique no botão "Criar chave de API" (ou "Get API key" / "Create API key").',
            'Selecione ou crie um projeto (ex: Meu Projeto) e confirme.',
            'Copie a chave gerada e cole no campo de Chave de Acesso.'
          ]
        };
      case 'CLAUDE':
        return {
          provedorNome: 'Anthropic Claude',
          url: 'https://console.anthropic.com/settings/keys',
          labelLink: 'Obter chave Claude',
          resumoTooltip: 'Passo a passo rápido (site em inglês):\n1. Acesse o Anthropic Console\n2. Vá no menu Settings > "API Keys"\n3. Clique em "Create Key" e copie a chave',
          passos: [
            'Acesse o Anthropic Console e entre com sua conta (painel em inglês).',
            'No menu superior ou lateral, vá em Settings > "API Keys".',
            'Clique em "Create Key", digite um nome e confirme.',
            'Copie o código gerado (começa com sk-ant-...) e cole aqui.'
          ]
        };
      case 'OPENAI':
        return {
          provedorNome: 'OpenAI (ChatGPT)',
          url: 'https://platform.openai.com/api-keys',
          labelLink: 'Obter chave OpenAI',
          resumoTooltip: 'Passo a passo rápido (site em inglês):\n1. Acesse platform.openai.com\n2. No menu lateral, acesse "API keys"\n3. Clique em "+ Create new secret key" e copie',
          passos: [
            'Acesse a plataforma da OpenAI e faça login na sua conta.',
            'No menu lateral esquerdo, selecione "API keys".',
            'Clique em "+ Create new secret key", escolha um nome e crie.',
            'Copie o código gerado de imediato e cole aqui.'
          ]
        };
      case 'DEEPSEEK':
        return {
          provedorNome: 'DeepSeek',
          url: 'https://platform.deepseek.com/api_keys',
          labelLink: 'Obter chave DeepSeek',
          resumoTooltip: 'Passo a passo rápido (site em inglês):\n1. Acesse platform.deepseek.com\n2. No menu lateral, acesse "API Keys"\n3. Clique em "Create API Key" e copie o código',
          passos: [
            'Acesse platform.deepseek.com e entre com sua conta (painel em inglês).',
            'No menu lateral esquerdo, clique em "API Keys".',
            'Clique em "Create API Key", confirme o nome e gere.',
            'Copie o código da chave e cole no campo abaixo.'
          ]
        };
      case 'GROQ':
        return {
          provedorNome: 'Groq Cloud',
          url: 'https://console.groq.com/keys',
          labelLink: 'Obter chave Groq',
          resumoTooltip: 'Passo a passo rápido (site em inglês):\n1. Acesse console.groq.com\n2. No menu lateral, acesse "API Keys"\n3. Clique em "Create API Key" e copie',
          passos: [
            'Acesse o Groq Console com sua conta (painel em inglês).',
            'No menu lateral, selecione a seção "API Keys".',
            'Clique no botão "Create API Key", dê um nome e confirme.',
            'Copie o código da chave (começa com gsk_...) e cole aqui.'
          ]
        };
      default:
        return {
          provedorNome: 'Provedor de IA',
          url: 'https://aistudio.google.com/app/apikey',
          labelLink: 'Obter chave',
          resumoTooltip: 'Acesse o painel oficial para gerar sua chave.',
          passos: [
            'Acesse o painel do provedor de IA escolhido.',
            'Navegue até a seção de chaves (API Keys).',
            'Crie uma chave e copie o código para cá.'
          ]
        };
    }
  }

  public get linkApiKeyProvedor() {
    return {
      url: this.tutorialApiKey.url,
      label: this.tutorialApiKey.labelLink,
      dicaLink: this.tutorialApiKey.resumoTooltip,
      provedorNome: this.tutorialApiKey.provedorNome,
      passos: this.tutorialApiKey.passos
    };
  }

  public formatarData(dataStr?: string | null): string {
    if (!dataStr) return 'Nunca sincronizado';
    try {
      const d = new Date(dataStr);
      return d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
    } catch {
      return dataStr;
    }
  }
}
