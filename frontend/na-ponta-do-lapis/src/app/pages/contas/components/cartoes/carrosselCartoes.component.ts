import { Component } from "@angular/core";


@Component({
  selector: 'app-carrossel-cartoes',
  imports: [],
  template: `
    <!-- CARROSSEL -->
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
                            (click)="selecionarAnterior()"
                            class="w-11 h-11 rounded-full bg-slate-100 hover:bg-slate-200 transition flex items-center justify-center">

                            <i class="pi pi-chevron-left text-slate-700"></i>

                        </button>

                        <button
                            type="button"
                            (click)="selecionarProxima()"
                            class="w-11 h-11 rounded-full bg-slate-100 hover:bg-slate-200 transition flex items-center justify-center">

                            <i class="pi pi-chevron-right text-slate-700"></i>

                        </button>

                    </div>

                </div>

                <!-- ÁREA DO CARROSSEL -->
                <div class="relative h-[280px] flex items-center justify-center overflow-hidden">

                    <ng-container
                    *ngIf="
                        !carregandoContas &&
                        contaFinanceiraDados.length > 0
                    ">

                        <div
                            *ngFor="let conta of contaFinanceiraDados; let i = index"

                            (click)="selecionarConta(i)"

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
                                class="relative w-[400px] max-w-[82vw] h-[220px]
                                       rounded-[2rem] overflow-hidden shadow-2xl"

                                [style.background]="conta.cor">

                                <!-- EFEITOS -->
                                <div
                                    class="absolute top-[-30px] right-[-20px]
                                           w-52 h-52 rounded-full border border-white/20">
                                </div>

                                <div
                                    class="absolute top-[-70px] right-[20px]
                                           w-72 h-72 rounded-full border border-white/10">
                                </div>

                                <div class="absolute inset-0 bg-black/10"></div>

                                <!-- CONTEÚDO -->
                                <div
                                    class="relative z-10 h-full p-7
                                           flex flex-col justify-between text-white">

                                    <!-- TOPO -->
                                    <div class="flex items-start justify-between">

                                        <div class="min-w-0">

                                            <h3 class="text-3xl font-bold mt-2 truncate">
                                                {{ conta.nome }}
                                            </h3>

                                        </div>

                                        <div
                                            class="w-14 h-14 rounded-2xl
                                                   bg-white/20 backdrop-blur-sm
                                                   flex items-center justify-center shrink-0">

                                            <i class="pi pi-wallet text-xl"></i>

                                        </div>

                                    </div>

                                </div>

                            </div>

                        </div>

                    </ng-container>

                    <!-- EMPTY -->
                     <div
                        *ngIf="carregandoContas"
                        class="h-full flex items-center justify-center text-slate-400">

                        Carregando contas...

                    </div>
                    <div
                        *ngIf="
                            !carregandoContas &&
                            contaFinanceiraDados.length === 0
                        "
                        class="h-full flex items-center justify-center text-slate-400">

                        Nenhuma conta cadastrada

                    </div>

                </div>

                <!-- INDICADORES -->
                <div
                    *ngIf="contaFinanceiraDados.length > 1"
                    class="flex justify-center items-center gap-3 mt-5">

                    <button
                        *ngFor="let conta of contaFinanceiraDados; let i = index"

                        type="button"

                        (click)="selecionarConta(i)"

                        class="rounded-full transition-all duration-300"

                        [ngClass]="
                            i === contaSelecionadaIndex
                            ? 'w-10 h-3 bg-slate-800'
                            : 'w-3 h-3 bg-slate-300 hover:bg-slate-400'
                        ">
                    </button>

                </div>

            </div>
  `
})

export class CarrosselCartoes {}
