import { Component } from "@angular/core";

@Component({
  selector: 'app-cards-resumo',
  imports: [],
  template: `
    <section class="grid grid-cols-4 gap-4">

      <!-- Card: Saldo atual -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Saldo atual</span>
          <div class="w-10 h-10 rounded-full bg-purple-100 flex items-center justify-center">
            <!-- ícone placeholder -->
            <span class="text-purple-400 text-lg">👤</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">40,689</span>
        <span class="text-sm text-green-500 font-medium">↑ 8.5% Mais que ontem</span>
      </div>

      <!-- Card: Moeda -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Moeda</span>
          <div class="w-10 h-10 rounded-full bg-yellow-100 flex items-center justify-center">
            <span class="text-yellow-500 text-lg">🪙</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">R$89,000</span>
        <div class="flex items-center gap-3 text-xs text-gray-600">
          <span>🇺🇸 USD $6.12 <span class="text-green-500">↑</span></span>
          <span>🇨🇦 CAD $3.82 <span class="text-green-500">↑</span></span>
          <span>🇵🇹 EUR $3.82 <span class="text-red-500">↓</span></span>
        </div>
      </div>

      <!-- Card: Receita -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Receita</span>
          <div class="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center">
            <span class="text-green-500 text-lg">📈</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">$89,000</span>
        <span class="text-sm text-red-400 font-medium">↓ 4.3% Menos que ontem</span>
      </div>

      <!-- Card: Gastos -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Gastos</span>
          <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center">
            <span class="text-red-400 text-lg">🕐</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">2,040</span>
        <span class="text-sm text-green-500 font-medium">↑ 1.8% Mais que ontem</span>
      </div>

    </section>
  `
})

export class CardsResumoComponent {

}
