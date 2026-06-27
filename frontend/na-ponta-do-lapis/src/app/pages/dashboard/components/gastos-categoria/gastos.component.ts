import { Component } from "@angular/core";

@Component({
  selector: 'app-gastos-categoria',
  imports: [],
  template: `

    <div class="bg-white rounded-2xl p-6 shadow-sm">
        <h2 class="text-lg font-bold text-gray-800 text-center mb-6">Gastos por categoria</h2>

        <div class="flex items-center gap-8">

          <!-- Donut placeholder -->
          <div class="flex-shrink-0 w-52 h-52 rounded-full bg-gray-50 border-[24px] border-gray-200 flex items-center justify-center relative">
            <span class="text-xs text-gray-400 text-center leading-tight">[ Donut<br/>Chart ]</span>
          </div>

          <!-- Legenda -->
          <div class="flex flex-col gap-2 text-sm text-gray-700">
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-purple-500 inline-block"></span>
              <span class="flex-1">Technology and equip...</span>
              <span class="font-semibold text-gray-800 ml-4">10.33%</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-gray-800 inline-block"></span>
              <span class="flex-1">Marketing</span>
              <span class="font-semibold text-gray-800 ml-4">4.19%</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-blue-400 inline-block"></span>
              <span class="flex-1">Finance</span>
              <span class="font-semibold text-gray-800 ml-4">10.33%</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-yellow-400 inline-block"></span>
              <span class="flex-1">Human Resources</span>
              <span class="font-semibold text-gray-800 ml-4">4.19%</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-red-500 inline-block"></span>
              <span class="flex-1">Marketing</span>
              <span class="font-semibold text-gray-800 ml-4">10.33%</span>
            </div>
          </div>

        </div>
      </div>

  `
})

export class GastosCategoriaComponent {

}
