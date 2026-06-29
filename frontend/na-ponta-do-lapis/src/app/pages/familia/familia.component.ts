import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button } from "primeng/button";
import { CardSaldoComponent } from '../../shared/components/card-saldo/card-saldo.component';
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
import { ContaFinanceiraService } from '../contas/service/contas.service';
import { IContas } from '../../model/IContas.models';
import { Usuario } from '@models/IUsuario.models';

@Component({
  selector: 'app-familia',
  imports: [Button, FormsModule, InputText, IconComponent, Card, PrimeNGModuleModule, CommonModule],
  templateUrl: './familia.component.html',
  styleUrl: './familia.component.css',
})
export class FamiliaComponent {
  messageService = inject(MessageService);
  transacoesService = inject(TransacoesService);
  contaService = inject(ContaFinanceiraService);
  cdr = inject(ChangeDetectorRef);
  router = inject(Router);
  familiaService = inject(FamiliaService);
  saldoTotal: number = 0;
  userLogado = StorageService.getUsuarioDecodificado();
  nomeFamilia: string = "";
  foto?: string = "";
  convidados: any[] = [];
  membros: Usuario[] = [];
  contas:IContas[] = [];
  rankingGastos: any[] = [];
  submitted: boolean = false;
  value: string = "";
  message: string = "";
  temFamilia: boolean = false;
  familiaId: number | null = null;
  valueMembro: string = "";
  modalAdicionarAberto: boolean = false;
  modalRemoverAberto: boolean = false;
  modalPromoverAberto: boolean = false
  transacoesDados: ITransacoes[] = [];

  ngOnInit() {
    this.carregarDadosFamilia();
  }
  private carregarDadosFamilia() {
    this.familiaService.buscarUsuariosPorNickname(this.userLogado?.sub || "").subscribe(user => {
      this.familiaId = (user as any).idFamilia || null;
      this.temFamilia = this.familiaId != null;
      this.cdr.detectChanges();

      if (this.familiaId) {
        this.familiaService.buscarMembrosPorFamilia(this.familiaId).subscribe({
          next: (res: Usuario[]) => {
            this.membros = res;
            this.cdr.detectChanges();
          }
        });

        this.familiaService.buscarTransacoesPorFamilia(this.familiaId).subscribe({
          next: (res: any[]) => {
            this.transacoesDados = res;
            this.cdr.detectChanges();
          },
          error: (erro: HttpErrorResponse) => {
            this.erroAoSalvar("Erro ao carregar transações", erro);
          }
        });

        this.familiaService.buscarContasPorFamilia(this.familiaId).subscribe({
          next: (res:IContas[]) => {
            this.contas = res;
            console.log(this.contas)
            this.saldoTotal = 0;
            res.forEach(atual =>  this.saldoTotal += atual.saldo);
            this.cdr.detectChanges();
          },
          error: (erro: HttpErrorResponse) => {
            this.erroAoSalvar("Erro ao carregar contas", erro);
          }
        });
      }
    });
  }
  handleModalAdicionarAberto() {
    this.modalAdicionarAberto = !this.modalAdicionarAberto;
    this.submitted = false;
    this.valueMembro = "";
  }
  handleModalRemoverAberto() {
    this.modalRemoverAberto = !this.modalRemoverAberto;
    this.submitted = false;
    this.valueMembro = "";
  }
  handleModalPromoverAberto() {
    this.modalPromoverAberto = !this.modalPromoverAberto;
    this.submitted = false;
    this.valueMembro = "";
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
          this.familiaService.usuarioTemFamilia(nickname).subscribe(temFamilia => {
            if (temFamilia) {
              this.message = "Este usuário já pertence a uma família.";
              this.cdr.detectChanges();
              return;
            }
            this.convidados = [...this.convidados, [nickname, user]];
            this.message = "";
            this.value = "";
            this.cdr.detectChanges();
          });
        });
      }
    });
  }
  removerConvidado(nickname:string) {
    this.convidados = this.convidados.filter(convidado => convidado[0] !== nickname);
    console.log("Usuário removido:", nickname);
    this.cdr.detectChanges();
  }
  adicionarMembro(nickname:string) {
    this.submitted = true;
    if (nickname.trim() === "") {
      this.message = "O nickname não pode ser vazio.";
      this.cdr.detectChanges();
      return;
    }
    if (this.convidados.some(convidado => convidado[0] === nickname)) {
      this.message = "Este usuário já foi adicionado à lista de convidados.";
      this.cdr.detectChanges();
      return;
    }
    if (nickname.trim().toLowerCase() === this.userLogado?.sub?.trim().toLowerCase()) {
      this.message = "Você não pode convidar a si mesmo.";
      this.cdr.detectChanges();
      return;
    }
    this.familiaService.verificarUsuarioExistente(nickname).subscribe(existe => {
      if (!existe) {
        this.message = "Usuário não encontrado.";
        this.cdr.detectChanges();
      }
      else {
        this.familiaService.buscarUsuariosPorNickname(nickname).subscribe(user => {
          this.familiaService.usuarioTemFamilia(nickname).subscribe(temFamilia => {
            if (temFamilia) {
              this.message = "Este usuário já pertence a uma família.";
              this.cdr.detectChanges();
              return;
            }
            this.familiaService.vincularUsuarioAFamilia(nickname, this.familiaId!).subscribe(() => {
              this.sucessoAoSalvar("Usuário adicionado à família com sucesso!");
              this.carregarDadosFamilia();
              this.handleModalAdicionarAberto();
              this.convidados = [...this.convidados, [nickname, user]];
              this.message = "";
              this.value = "";
              this.submitted = false;
              this.cdr.detectChanges();
            });
          });
        });
      }
    });
  }
  removerMembro(nickname:string) {
    this.submitted = true;
    if (nickname.trim() === "") {
      this.message = "O nickname não pode ser vazio.";
      this.cdr.detectChanges();
      return;
    }
    if (nickname.trim().toLowerCase() === this.userLogado?.sub?.trim().toLowerCase()) {
      this.message = "Você não pode se remover da família.";
      this.cdr.detectChanges();
      return;
    }
    this.familiaService.buscarUsuariosPorNickname(nickname).subscribe(user => {
      if (user === null || user === undefined) {
        this.message = "Usuário não encontrado.";
        this.cdr.detectChanges();
        return;
      }
      const userId = (user as any).id;
      if (this.familiaId && userId) {
        this.familiaService.usuarioTemFamilia(nickname).subscribe(temFamilia => {
          if (!temFamilia) {
            this.message = "Este usuário não pertence a uma família.";
            this.cdr.detectChanges();
            return;
        }});
        this.familiaService.removerUsuarioDaFamilia(this.familiaId, nickname).subscribe({
          next: () => {
            this.sucessoAoSalvar("Usuário removido da família com sucesso!");
            this.carregarDadosFamilia();
            this.handleModalRemoverAberto();
            this.convidados = this.convidados.filter(convidado => convidado[0] !== nickname);
            this.message = "";
            this.value = "";
            this.submitted = false;
            this.cdr.detectChanges();
          },
          error: (erro: HttpErrorResponse) => {
            this.erroAoSalvar("Erro ao remover usuário da família", erro);
          }
        });
      }
    });
  }
  criarFamilia() {
    this.submitted = true;
    if (!this.nomeFamilia.trim()) return;
    if (this.convidados.length === 0) {
        this.message = "Adicione pelo menos um convidado para criar a família.";
        return;
    }
    this.familiaService.criarFamilia(this.nomeFamilia, this.foto).subscribe(familiaCriada => {
      const familiaId = familiaCriada.id;
      const convidados = this.convidados.map(([nickname]) => nickname);

      forkJoin(
        convidados.map(nickname =>
          this.familiaService.vincularUsuarioAFamilia(nickname, familiaId)
        )
      ).subscribe(() => {
        this.sucessoAoSalvar("Família criada e usuários convidados com sucesso!");
        this.resetarFormulario();
      });
    });
  }
  private resetarFormulario() {
    this.foto = "";
    this.convidados = [];
    this.submitted = false;
    this.temFamilia = true;
    this.cdr.detectChanges();
    this.carregarDadosFamilia()
  }
  resetNome(){
      this.nomeFamilia = "";
  }
  buscarPorDescricao(descricao:string) :void {
    this.familiaService.buscarTransacoesFamiliaPorDescricao(this.familiaId!, descricao).subscribe({
      next: (res: ITransacoes[]) => {
        this.transacoesDados = res
        this.cdr.detectChanges()
      },
      error: (erro: HttpErrorResponse) => {
        console.error(erro)
        this.erroAoSalvar("Erro ao carregar transação", erro);
      }
    });
  }
  private erroAoSalvar(mensagem: string, erro: any) {
    console.error(erro);
    this.messageService.add({ severity: 'error', summary: mensagem, life: 2000 });
  }
  private sucessoAoSalvar(messagem: string) {
    this.messageService.add({ severity: 'success', summary: messagem, life: 2000 });
  }
}

