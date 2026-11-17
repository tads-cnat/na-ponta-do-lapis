import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-privacidade',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="min-h-screen bg-[#F8FAFC] text-slate-800">
      <header class="bg-[#D1C4FF] border-b-4 border-[#EEC046]">
        <div class="max-w-4xl mx-auto px-6 py-6 flex items-center justify-between gap-4">
          <a routerLink="/" class="flex items-center gap-3 no-underline">
            <img src="/logoReduzida.png" alt="Na Ponta do Lápis" class="w-10 h-10 rounded-xl shadow-xs">
            <span class="text-xl font-extrabold text-[#4A2585] tracking-tight">Na Ponta do Lápis</span>
          </a>
          <a routerLink="/"
             class="inline-flex items-center gap-2 text-xs sm:text-sm font-semibold text-[#4A2585] bg-white/80 hover:bg-white px-3.5 py-2 rounded-xl transition-all no-underline shadow-2xs">
            <i class="pi pi-arrow-left text-xs"></i>
            <span>Voltar ao início</span>
          </a>
        </div>
      </header>

      <main class="max-w-4xl mx-auto px-6 py-10 sm:py-12">
        <div class="flex items-center gap-2 mb-6">
          <a routerLink="/privacidade"
             class="px-4 py-2 rounded-xl text-xs sm:text-sm font-bold no-underline transition-colors"
             [class]="!isTermos ? 'bg-[#7843CD] text-white' : 'bg-white text-slate-600 border border-slate-200 hover:text-[#7843CD]'">
            Política de Privacidade
          </a>
          <a routerLink="/termos"
             class="px-4 py-2 rounded-xl text-xs sm:text-sm font-bold no-underline transition-colors"
             [class]="isTermos ? 'bg-[#7843CD] text-white' : 'bg-white text-slate-600 border border-slate-200 hover:text-[#7843CD]'">
            Termos de Serviço
          </a>
        </div>

        <article class="bg-white rounded-2xl border border-slate-200/80 shadow-xs p-6 sm:p-10 space-y-6">
          @if (!isTermos) {
            <div class="border-b border-slate-100 pb-6">
              <span class="inline-block text-xs font-bold uppercase tracking-wider text-[#7843CD] bg-[#F3E8FF] px-3 py-1 rounded-full">
                LGPD &amp; Privacidade
              </span>
              <h1 class="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-3 mb-2">
                Política de Privacidade
              </h1>
              <p class="text-sm text-slate-500 m-0">
                Versão 2.0 · Última atualização: novembro de 2026 · Domínio oficial:
                <a href="https://napontadolapis.app" class="text-[#7843CD] font-semibold hover:underline">napontadolapis.app</a>
              </p>
            </div>

            <section class="space-y-2">
              <h2 class="text-lg font-bold text-[#4A2585]">1. Controlador e finalidade</h2>
              <p class="text-sm sm:text-base text-slate-600 leading-relaxed">
                O <strong>Na Ponta do Lápis</strong> é uma plataforma de controle financeiro pessoal e familiar. Esta Política explica como coletamos, utilizamos, armazenamos e protegemos seus dados pessoais em conformidade com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018 — LGPD).
              </p>
            </section>

            <section class="space-y-2">
              <h2 class="text-lg font-bold text-[#4A2585]">2. Dados coletados</h2>
              <ul class="list-disc pl-5 text-sm sm:text-base text-slate-600 space-y-1.5 leading-relaxed">
                <li><strong>Cadastro tradicional:</strong> nome completo, endereço de e-mail, nome de usuário, telefone (opcional) e senha armazenada com hash criptográfico.</li>
                <li><strong>Login com Google (Google Identity Services):</strong> acessamos exclusivamente seu <em>nome</em>, <em>e-mail verificado</em> e <em>foto de perfil</em> mediante consentimento. Não acessamos sua caixa de e-mails, contatos, arquivos ou outros serviços Google.</li>
                <li><strong>Dados financeiros inseridos por você:</strong> contas, categorias, transações, metas e relatórios cadastrados voluntariamente.</li>
              </ul>
            </section>

            <section class="space-y-2">
              <h2 class="text-lg font-bold text-[#4A2585]">3. Uso e compartilhamento</h2>
              <p class="text-sm sm:text-base text-slate-600 leading-relaxed">
                Utilizamos seus dados apenas para autenticar sua conta, enviar e-mails transacionais (confirmação de cadastro e redefinição de senha) e exibir suas informações financeiras no sistema. <strong>Não vendemos nem compartilhamos seus dados com terceiros para fins comerciais ou publicitários.</strong>
              </p>
            </section>

            <section class="space-y-2">
              <h2 class="text-lg font-bold text-[#4A2585]">4. Seus direitos (LGPD) e exclusão de conta</h2>
              <p class="text-sm sm:text-base text-slate-600 leading-relaxed">
                Você pode consultar, corrigir ou excluir sua conta e todos os seus dados diretamente em <strong>Perfil → Ajustes do Perfil</strong>, ou entrando em contato pelo e-mail
                <a href="mailto:contato@napontadolapis.app" class="text-[#7843CD] font-semibold hover:underline">contato&#64;napontadolapis.app</a>.
              </p>
            </section>
          } @else {
            <div class="border-b border-slate-100 pb-6">
              <span class="inline-block text-xs font-bold uppercase tracking-wider text-[#7843CD] bg-[#F3E8FF] px-3 py-1 rounded-full">
                Regras de Uso
              </span>
              <h1 class="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-3 mb-2">
                Termos de Serviço
              </h1>
              <p class="text-sm text-slate-500 m-0">
                Versão 2.0 · Última atualização: novembro de 2026 · Domínio oficial:
                <a href="https://napontadolapis.app" class="text-[#7843CD] font-semibold hover:underline">napontadolapis.app</a>
              </p>
            </div>

            <section class="space-y-2">
              <h2 class="text-lg font-bold text-[#4A2585]">1. Aceitação dos Termos</h2>
              <p class="text-sm sm:text-base text-slate-600 leading-relaxed">
                Ao criar uma conta ou utilizar o <strong>Na Ponta do Lápis</strong>, você concorda com estes Termos de Serviço e com a nossa <a routerLink="/privacidade" class="text-[#7843CD] font-semibold hover:underline">Política de Privacidade</a>.
              </p>
            </section>

            <section class="space-y-2">
              <h2 class="text-lg font-bold text-[#4A2585]">2. Conta e responsabilidade de acesso</h2>
              <ul class="list-disc pl-5 text-sm sm:text-base text-slate-600 space-y-1.5 leading-relaxed">
                <li>Você é responsável por manter a confidencialidade de suas credenciais de acesso e por fornecer um endereço de e-mail válido.</li>
                <li>O uso de recursos compartilhados (como grupos familiares) pressupõe autorização entre os participantes convidados.</li>
              </ul>
            </section>

            <section class="space-y-2">
              <h2 class="text-lg font-bold text-[#4A2585]">3. Natureza do serviço</h2>
              <p class="text-sm sm:text-base text-slate-600 leading-relaxed">
                O Na Ponta do Lápis é uma ferramenta de organização e acompanhamento financeiro pessoal e familiar. As análises e relatórios exibidos têm caráter exclusivamente informativo e não constituem consultoria financeira, contábil ou de investimentos.
              </p>
            </section>

            <section class="space-y-2">
              <h2 class="text-lg font-bold text-[#4A2585]">4. Encerramento e contato</h2>
              <p class="text-sm sm:text-base text-slate-600 leading-relaxed">
                Você pode encerrar sua conta a qualquer momento em <strong>Perfil → Ajustes do Perfil</strong>. Dúvidas sobre estes termos podem ser enviadas para
                <a href="mailto:contato@napontadolapis.app" class="text-[#7843CD] font-semibold hover:underline">contato&#64;napontadolapis.app</a>.
              </p>
            </section>
          }

          <div class="pt-4 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3">
            <a href="/landing-page/termos_privacidade.pdf" target="_blank" rel="noopener noreferrer"
               class="inline-flex items-center gap-2 text-sm font-semibold text-[#7843CD] hover:text-[#6214CC] hover:underline">
              <i class="pi pi-file-pdf"></i>
              <span>Baixar versão completa em PDF</span>
            </a>
            <a routerLink="/login"
               class="inline-flex items-center gap-2 text-sm font-bold text-white bg-[#7843CD] hover:bg-[#6214CC] px-5 py-2.5 rounded-xl transition-colors no-underline">
              <span>Acessar o sistema</span>
            </a>
          </div>
        </article>
      </main>
    </div>
  `,
})
export class PrivacidadeComponent {
  private readonly router = inject(Router);

  get isTermos(): boolean {
    return this.router.url.split('?')[0].startsWith('/termos');
  }
}
