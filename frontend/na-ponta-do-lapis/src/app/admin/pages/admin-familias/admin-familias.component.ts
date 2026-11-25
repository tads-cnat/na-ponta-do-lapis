import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AdminApiService, AdminFamiliaResumo, AdminPageResponse, extrairMensagemErroAdmin } from '../../services/admin-api.service';

@Component({
  selector: 'app-admin-familias',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex flex-col gap-5">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 class="text-2xl font-bold text-slate-900 m-0">Famílias</h1>
          <p class="text-sm text-slate-500 mt-1 mb-0">Visão agregada dos grupos familiares ativos na plataforma.</p>
        </div>
        <button
          type="button"
          (click)="carregar(pagina()?.page ?? 0)"
          [disabled]="carregando()"
          class="px-4 py-2 rounded-xl bg-white border border-slate-200 hover:bg-slate-50 text-xs font-semibold text-slate-700 flex items-center gap-2 self-start cursor-pointer">
          <i class="pi pi-refresh text-xs" [class.pi-spin]="carregando()"></i>
          <span>Atualizar</span>
        </button>
      </div>

      @if (erro()) {
        <div class="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-sm">{{ erro() }}</div>
      }

      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-xs">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse text-sm">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500">
                <th class="py-3 px-4">ID</th>
                <th class="py-3 px-4">Nome da Família</th>
                <th class="py-3 px-4">Administrador</th>
                <th class="py-3 px-4 text-center">Membros</th>
                <th class="py-3 px-4 text-center">Convites Pendentes</th>
              </tr>
            </thead>
            <tbody>
              @for (f of pagina()?.content ?? []; track f.id) {
                <tr class="border-b border-slate-100 hover:bg-slate-50/70">
                  <td class="py-3 px-4 text-xs text-slate-400">#{{ f.id }}</td>
                  <td class="py-3 px-4 font-semibold text-slate-900">{{ f.nome }}</td>
                  <td class="py-3 px-4 text-xs">
                    <div class="font-medium text-slate-800">{{ f.adminFamiliaNome ?? '—' }}</div>
                    <div class="text-slate-400">{{ f.adminFamiliaEmail ?? '' }}</div>
                  </td>
                  <td class="py-3 px-4 text-center font-semibold">{{ f.totalMembros }}</td>
                  <td class="py-3 px-4 text-center">
                    <span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-700">
                      {{ f.convitesPendentes }}
                    </span>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="py-10 text-center text-slate-400">
                    {{ carregando() ? 'Carregando famílias...' : 'Nenhuma família cadastrada.' }}
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        @if (pagina(); as p) {
          <div class="px-4 py-3 bg-slate-50 border-t border-slate-200 flex items-center justify-between text-xs text-slate-600">
            <span>Total: <strong>{{ p.totalElements }}</strong> família(s)</span>
            <div class="flex gap-2">
              <button type="button" [disabled]="p.page <= 0" (click)="carregar(p.page - 1)" class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white disabled:opacity-40 cursor-pointer">Anterior</button>
              <button type="button" [disabled]="p.page + 1 >= p.totalPages" (click)="carregar(p.page + 1)" class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white disabled:opacity-40 cursor-pointer">Próxima</button>
            </div>
          </div>
        }
      </div>
    </div>
  `,
})
export class AdminFamiliasComponent implements OnInit {
  private readonly adminApi = inject(AdminApiService);
  readonly pagina = signal<AdminPageResponse<AdminFamiliaResumo> | null>(null);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.carregar(0);
  }

  carregar(page: number): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.adminApi.listarFamilias(page, 20).subscribe({
      next: (res) => {
        this.pagina.set(res);
        this.carregando.set(false);
      },
      error: (err) => {
        this.erro.set(extrairMensagemErroAdmin(err, 'Erro ao carregar famílias.'));
        this.carregando.set(false);
      },
    });
  }
}
