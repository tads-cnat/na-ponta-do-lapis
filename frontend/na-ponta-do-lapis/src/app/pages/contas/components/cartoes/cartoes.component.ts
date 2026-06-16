import { Component } from "@angular/core";
import { AcoesCartoesConta } from "./acoes.component";
import { CarrosselCartoes } from "./carrosselCartoes.component";

@Component({
  selector: 'app-cartoes-contas',
  imports: [AcoesCartoesConta, CarrosselCartoes],
  template: `
    <!-- Cartões -->
          <div>
            <!-- HEADER -->
            <div class="flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">

                <div class="min-w-0">
                    <h1 class="text-4xl font-bold text-slate-900">
                        Contas
                    </h1>

                    <p class="text-slate-500 mt-2">
                        Gestão de contas financeiras
                    </p>
                </div>

                <!-- AÇÕES -->
                <app-acoes-cartoes></app-acoes-cartoes>

            <!-- CARROSSEL -->
            <app-carrossel-cartoes></app-carrossel-cartoes>
  `
})

export class CartaoContaComponent {}
