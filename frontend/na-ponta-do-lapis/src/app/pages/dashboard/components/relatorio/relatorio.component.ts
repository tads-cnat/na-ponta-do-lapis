import { Component } from "@angular/core";

@Component({
  selector: 'app-relatorio',
  imports: [],
  template: `
    <section class="bg-white rounded-2xl p-6 shadow-sm">

     Cabeçalho da seção
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-lg font-bold text-gray-800">Relatório mensal</h2>
         Seletor de mês
        <select class="text-sm border border-gray-200 rounded-lg px-3 py-1.5 text-gray-600 bg-white focus:outline-none">
          <option>Janeiro</option>
          <option>Fevereiro</option>
          <option>Março</option>
          <option>Abril</option>
          <option>Maio</option>
          <option>Junho</option>
          <option>Julho</option>
          <option>Agosto</option>
          <option>Setembro</option>
          <option>Outubro</option>
          <option>Novembro</option>
          <option>Dezembro</option>
        </select>
      </div>

       Área do gráfico de linha (placeholder)
      <div class="relative w-full h-[260px] bg-gray-50 rounded-xl flex items-center justify-center border border-dashed border-gray-200">
        <div class="text-center text-gray-400">
          <p class="text-sm font-medium">[ Gráfico de linha — Relatório mensal ]</p>
          <p class="text-xs mt-1">Eixo X: valores monetários (5k → 60k) · Eixo Y: percentual (20% → 100%)</p>
          <p class="text-xs mt-1">Tooltip de destaque: <span class="font-semibold text-blue-400">64,3664.77</span> no pico (~20k)</p>
        </div>
      </div>

    </section>
  `
})

export class RelatorioComponent {

}
