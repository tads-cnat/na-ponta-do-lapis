import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NgClass } from '@angular/common';
import { IContas } from '../../../../model/IContas.models';

@Component({
  selector: 'app-carrossel-cartoes',
  standalone: true,
  imports: [NgClass],
  template: `
    <div class="bg-white rounded-[2rem] p-6 shadow-sm border border-slate-100 overflow-hidden">

      <!-- TOPO -->
      <div class="flex items-center justify-between mb-8">

        <div>
          <h2 class="text-2xl font-bold text-slate-900">
            Suas Contas
          </h2>
          <p class="text-slate-500 mt-1 text-sm">
            Navegue entre as contas cadastradas
          </p>
        </div>

        <!-- SETAS -->
        <div class="flex items-center gap-3">

          <button
            type="button"
            (click)="onAnterior.emit()"
            class="w-11 h-11 rounded-full bg-slate-100 hover:bg-slate-200 transition flex items-center justify-center">
            <i class="pi pi-chevron-left text-slate-700"></i>
          </button>

          <button
            type="button"
            (click)="onProxima.emit()"
            class="w-11 h-11 rounded-full bg-slate-100 hover:bg-slate-200 transition flex items-center justify-center">
            <i class="pi pi-chevron-right text-slate-700"></i>
          </button>

        </div>

      </div>

      <!-- ÁREA DO CARROSSEL -->
      <div class="relative h-[280px] flex items-center justify-center overflow-hidden">

        @if (!carregando && contas.length > 0) {

          @for (conta of contas; track conta.id; let i = $index) {

            <div
              (click)="onContaSelecionada.emit(i)"
              class="absolute transition-all duration-500 ease-in-out cursor-pointer"
              [ngClass]="{
                'z-30 opacity-100 scale-100 translate-x-0':
                  i === contaSelecionadaIndex,
                'z-20 opacity-40 scale-90 -translate-x-[72%]':
                  i === contaAnterior,
                'z-20 opacity-40 scale-90 translate-x-[72%]':
                  i === contaProxima,
                'opacity-0 scale-75 pointer-events-none':
                  i !== contaSelecionadaIndex
                  && i !== contaAnterior
                  && i !== contaProxima
              }">

              <!-- CARD -->
              <div
                class="relative w-[400px] max-w-[82vw] h-[220px] rounded-[2rem] overflow-hidden shadow-2xl"
                [style.background]="conta.cor">

                <!-- EFEITOS -->
                <div class="absolute top-[-30px] right-[-20px] w-52 h-52 rounded-full border border-white/20"></div>
                <div class="absolute top-[-70px] right-[20px] w-72 h-72 rounded-full border border-white/10"></div>
                <div class="absolute inset-0 bg-black/10"></div>

                <!-- CONTEÚDO -->
                <div class="relative z-10 h-full p-7 flex flex-col justify-between text-white">

                  <div class="flex items-start justify-between">

                    <div class="min-w-0">
                      <h3 class="text-3xl font-bold mt-2 truncate">
                        {{ conta.nome }}
                      </h3>
                    </div>

                    <div class="w-14 h-14 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center shrink-0">
                      <i class="pi pi-wallet text-xl"></i>
                    </div>

                  </div>

                </div>

              </div>

            </div>

          }

        }

        @if (carregando) {
          <div class="h-full flex items-center justify-center text-slate-400">
            Carregando contas...
          </div>
        }

        @if (!carregando && contas.length === 0) {
          <div class="h-full flex items-center justify-center text-slate-400">
            Nenhuma conta cadastrada
          </div>
        }

      </div>

      <!-- INDICADORES -->
      @if (contas.length > 1) {
        <div class="flex justify-center items-center gap-3 mt-5">

          @for (conta of contas; track conta.id; let i = $index) {
            <button
              type="button"
              (click)="onContaSelecionada.emit(i)"
              class="rounded-full transition-all duration-300"
              [ngClass]="
                i === contaSelecionadaIndex
                  ? 'w-10 h-3 bg-slate-800'
                  : 'w-3 h-3 bg-slate-300 hover:bg-slate-400'
              ">
            </button>
          }

        </div>
      }

    </div>
  `
})
export class CarrosselCartoesComponent {

  @Input() contas: IContas[] = [];
  @Input() contaSelecionadaIndex: number = 0;
  @Input() contaAnterior: number = 0;
  @Input() contaProxima: number = 0;
  @Input() carregando: boolean = true;

  @Output() onContaSelecionada = new EventEmitter<number>();
  @Output() onAnterior = new EventEmitter<void>();
  @Output() onProxima = new EventEmitter<void>();

}
