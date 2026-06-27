import { Component, Output, EventEmitter } from "@angular/core";
import { PrimeNGModuleModule } from './../../../../shared/primeNg.module';

@Component({
  selector: 'app-metas-dashboard',
  imports: [PrimeNGModuleModule],
  template: `
    <div class="bg-white rounded-2xl p-6 shadow-sm">

        <!-- Cabeçalho -->
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-bold text-gray-800">Metas</h2>
          <p-button
          label="Mostrar todas"
          severity="secondary"
          styleClass="rounded-xl!"
          (click)="onIrParaMetas.emit()">
        </p-button>
        </div>

        <!-- Lista de metas -->
        <div class="flex flex-col gap-5">

          <!-- Meta 1 -->
          <div class="flex flex-col gap-1">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <span class="text-xl">🏠</span>
                <span class="text-sm font-semibold text-gray-800">Primeira casa</span>
              </div>
              <div class="text-right">
                <p class="text-sm font-bold text-green-500">R$150,000</p>
                <p class="text-xs text-gray-400">faltam R$100,000</p>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-xs text-yellow-500 font-semibold">33%</span>
              <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                <div class="h-full bg-yellow-400 rounded-full" style="width: 33%"></div>
              </div>
            </div>
          </div>

          <!-- Meta 2 -->
          <div class="flex flex-col gap-1">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <span class="text-xl">💍</span>
                <span class="text-sm font-semibold text-gray-800">Presente de noivado</span>
              </div>
              <div class="text-right">
                <p class="text-sm font-bold text-green-500">R$10,000</p>
                <p class="text-xs text-gray-400">faltam R$9,000</p>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-xs text-yellow-500 font-semibold">10%</span>
              <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                <div class="h-full bg-yellow-400 rounded-full" style="width: 10%"></div>
              </div>
            </div>
          </div>

          <!-- Meta 3 -->
          <div class="flex flex-col gap-1">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <span class="text-xl">🏖️</span>
                <span class="text-sm font-semibold text-gray-800">Feriadão</span>
              </div>
              <div class="text-right">
                <p class="text-sm font-bold text-green-500">R$5,000</p>
                <p class="text-xs text-gray-400">faltam R$2,500</p>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-xs text-yellow-500 font-semibold">50%</span>
              <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                <div class="h-full bg-yellow-400 rounded-full" style="width: 50%"></div>
              </div>
            </div>
          </div>

          <!-- Meta 4 -->
          <div class="flex flex-col gap-1">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <span class="text-xl">🚗</span>
                <span class="text-sm font-semibold text-gray-800">Novo Carro</span>
              </div>
              <div class="text-right">
                <p class="text-sm font-bold text-green-500">R$50,000</p>
                <p class="text-xs text-gray-400">faltam R$45,000</p>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-xs text-yellow-500 font-semibold">8%</span>
              <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                <div class="h-full bg-yellow-400 rounded-full" style="width: 8%"></div>
              </div>
            </div>
          </div>

          <!-- Meta 5 -->
          <div class="flex flex-col gap-1">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <span class="text-xl">💒</span>
                <span class="text-sm font-semibold text-gray-800">Casamento</span>
              </div>
              <div class="text-right">
                <p class="text-sm font-bold text-green-500">R$30,000</p>
                <p class="text-xs text-gray-400">faltam 15,000</p>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-xs text-yellow-500 font-semibold">80%</span>
              <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                <div class="h-full bg-yellow-400 rounded-full" style="width: 80%"></div>
              </div>
            </div>
          </div>

        </div>
      </div>
  `
})

export class MetasDashboardComponent {
  @Output() onIrParaMetas = new EventEmitter<void>();
}
