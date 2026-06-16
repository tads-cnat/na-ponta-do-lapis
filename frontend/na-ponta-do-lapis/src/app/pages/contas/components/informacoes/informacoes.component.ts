import { Component } from "@angular/core";


@Component({
  selector: 'app-informacoes-conta',
  imports: [],
  template: `
                <!-- INFORMAÇÕES -->
            <div
                *ngIf="contaSelecionada"
                class="bg-white rounded-[2rem] p-6 shadow-sm border border-slate-100">

                <div class="flex items-center justify-between mb-6">

                    <div>

                        <h2 class="text-2xl font-bold text-slate-900">
                            Informações da Conta
                        </h2>

                        <p class="text-1xl text-slate-700">
                            Resumo da conta selecionada
                        </p>

                    </div>

                </div>

                <!-- GRID -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

                    <!-- SALDO -->
                    <div
                        class="bg-slate-50 border border-slate-100 rounded-2xl p-5 min-w-0">

                        <span class="text-2xl text-slate-700 font-bold">
                            Saldo Atual
                        </span>

                        <div class="mt-3">

                            <span class="text-2xl font-bold text-slate-900 break-words leading-tight">
                                {{
                                    (contaSelecionada.saldo).toLocaleString('pt-BR', {
                                                                            style: 'currency',
                                                                            currency: 'BRL'
                                                                          })
                                }}
                            </span>

                        </div>

                    </div>

                    <!-- COR -->
                    <div
                        class="bg-slate-50 border border-slate-100 rounded-2xl p-5">

                        <span class="text-slate-700 font-bold">
                            Cor da Conta
                        </span>

                        <div class="mt-4 flex items-center">

                    <div
                        class="w-10 h-10 rounded-full border border-slate-200 shrink-0"
                        [style.background]="contaSelecionada.cor">
                    </div>

                        </div>

                    </div>

                </div>

            </div>
  `
})

export class InformacoesComponent {}
