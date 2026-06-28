import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from "primeng/button";
import { FormsModule } from "@angular/forms";
import { InputText } from "primeng/inputtext";
import { IconComponent } from '../../shared/components/icon/icon.component';
import { FamiliaService } from './service/familia.service';
import { inject } from '@angular/core';
import { StorageService } from '../../auth/service/storage.service';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Card } from "primeng/card";
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { ITransacoes } from '../../model/ITransacoes.model';
import { HttpErrorResponse } from '@angular/common/http';
import { TransacoesService } from '../transacoes/service/transacoes.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-familia',
  imports: [Button, FormsModule, InputText, IconComponent, Card, PrimeNGModuleModule, CommonModule],
  templateUrl: './familia.component.html',
  styleUrl: './familia.component.css',
})
export class FamiliaComponent {
  messageService = inject(MessageService);
  transacoesService = inject(TransacoesService);
  userLogado = StorageService.getUsuarioDecodificado();
  cdr = inject(ChangeDetectorRef);
  router = inject(Router);
  familiaService = inject(FamiliaService);
  nomeFamilia: string = "";
  foto?: string = "";
  convidados: any[] = [];
  submitted: boolean = false;
  value: string = "";
  message: string = "";
  temFamilia: boolean = false;
  transacoesDados: ITransacoes[] = [];

  ngOnInit() {
    this.familiaService.usuarioTemFamilia(this.userLogado?.sub || "").subscribe(tem => {
      console.log('temFamilia:', tem);
      console.log('sub do token:', this.userLogado?.sub);
      this.temFamilia = tem;
      this.cdr.detectChanges();
    });
  }
  limparConvidados() {
    this.convidados = [];
    console.log("Lista de convidados limpa");
  }
  limparNomeConvidado(){
    this.value = "";
    this.message = "";
    this.cdr.detectChanges();
  }
  adicionarNaListaConvidados(nickname:string) {
    if (nickname.trim() === "") {
      this.message = "O nickname não pode ser vazio.";
      this.cdr.detectChanges();
      return;
    }

    if (this.convidados.includes(nickname)) {
      this.message = "Este usuário já foi adicionado à lista de convidados.";
      this.cdr.detectChanges();
      return;
    }

    if (nickname.trim().toLowerCase() == this.userLogado?.sub?.trim().toLowerCase()) {
      this.message = "Você não pode convidar a si mesmo.";
      this.cdr.detectChanges();
      return;
    }

    this.familiaService.verificarUsuarioExistente(nickname).subscribe(existe => {
      if (!existe) {
        this.message = "Usuário não encontrado.";
        this.cdr.detectChanges();
      } else if (nickname === this.userLogado?.sub) {
        this.message = "Você não pode convidar a si mesmo.";
        this.cdr.detectChanges();
      } else {
        this.familiaService.buscarUsuariosPorNickname(nickname).subscribe(user => {
          if(this.familiaService.usuarioTemFamilia(nickname)) {
            this.message = "Este usuário já pertence a uma família.";
            this.cdr.detectChanges();
            return;
          }
          this.convidados = [...this.convidados, [nickname, user]];
          this.message = "";
          this.value = "";
          this.cdr.detectChanges()
        });
      }
    });
  }
  removerConvidado(nickname:string) {
    this.convidados = this.convidados.filter(convidado => convidado[0] !== nickname);
    console.log("Usuário removido:", nickname);
    this.cdr.detectChanges();
  }
  criarFamilia() {
    this.submitted = true;
    if (!this.nomeFamilia.trim()) return;

    this.familiaService.criarFamilia(this.nomeFamilia, this.foto).subscribe(familiaCriada => {
      const familiaId = familiaCriada.id;
      const convidados = this.convidados.map(([nickname]) => nickname);

      if (convidados.length === 0) {
        this.resetarFormulario();
        return;
      }

      forkJoin(
        convidados.map(nickname =>
          this.familiaService.vincularUsuarioAFamilia(nickname, familiaId)
        )
      ).subscribe(() => {
        this.resetarFormulario();
      });
    });
  }
  private resetarFormulario() {
    this.nomeFamilia = "";
    this.foto = "";
    this.convidados = [];
    this.submitted = false;
    this.temFamilia = true;
    this.cdr.detectChanges();
  }
  resetNome(){
      this.nomeFamilia = "";
  }
  buscarPorDescricao(descricao:string) :void {
    this.transacoesService.listarTransacoesPorDescricao(descricao).subscribe({
      next: (res: ITransacoes[]) => {
        this.transacoesDados = res
        this.cdr.detectChanges()
      },
      error: (erro: HttpErrorResponse) => {
        console.error(erro)
        this.erroAoSalvar("Erro ao carregar transação", erro);
      }
    })
  }
  private erroAoSalvar(mensagem: string, erro: any) {
    console.error(erro);
    this.messageService.add({ severity: 'error', summary: mensagem, life: 2000 });
  }
}

