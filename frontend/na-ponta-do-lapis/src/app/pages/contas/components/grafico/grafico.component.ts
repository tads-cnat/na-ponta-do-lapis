import { Component } from "@angular/core";


@Component({
  selector: 'app-grafico-conta',
  imports: [],
  template: `
    <!-- GRÁFICO -->
            <div class="bg-white rounded-[2rem] p-6 shadow-sm border border-slate-100">

                <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-6">

                    <div>

                        <h2 class="text-3xl font-bold text-slate-900">
                            Evolução do saldo
                        </h2>

                        <p class="text-slate-500 mt-1">
                            Histórico financeiro da conta selecionada
                        </p>

                    </div>

                    <p-select
                        [options]="[
                            { label: 'Semanal', value: 'week' },
                            { label: 'Mensal', value: 'month' }
                        ]"
                        optionLabel="label"
                        placeholder="Período"
                        styleClass="w-full lg:w-44">
                    </p-select>

                </div>

                <div
                    class="h-[360px] rounded-2xl border border-slate-100 bg-slate-50 flex items-center justify-center">

                    <span class="text-slate-400 text-lg">
                        Gráfico futuramente
                    </span>

                </div>

            </div>
  `
})

export class GraficoComponent {}
