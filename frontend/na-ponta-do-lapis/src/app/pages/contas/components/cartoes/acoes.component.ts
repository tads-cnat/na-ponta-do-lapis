import { Component } from "@angular/core";

@Component({
  selector: 'app-acoes-cartoes',
  imports: [],
  template: `
    <div class="flex flex-wrap gap-3">

      <p-button
          label="Adicionar"
          icon="pi pi-plus"
          severity="success"
          class="rounded-xl"
          (click)="abrirDialog()">
      </p-button>

      <p-button
          label="Editar"
          icon="pi pi-pencil"
          severity="contrast"
          class="rounded-xl"
          (click)="editarConta(contaSelecionada)">
      </p-button>

      <p-button
          label="Excluir"
          icon="pi pi-trash"
          severity="danger"
          class="rounded-xl"
          (click)="abrirDialogExcluir(contaSelecionada)">
      </p-button>

  </div>
  `

})

export class AcoesCartoesConta {}
