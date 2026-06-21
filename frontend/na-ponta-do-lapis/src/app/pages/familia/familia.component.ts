import { Component } from '@angular/core';
import { Button } from "primeng/button";
import { FormsModule } from "@angular/forms";
import { InputText } from "primeng/inputtext";
import { IconComponent } from '../../shared/components/icon/icon.component';
import { FamiliaService } from './service/familia.service';
import { inject } from '@angular/core';

@Component({
  selector: 'app-familia',
  imports: [Button, FormsModule, InputText, IconComponent],
  templateUrl: './familia.component.html',
  styleUrl: './familia.component.css',
})
export class FamiliaComponent {
  familiaService = inject(FamiliaService);
  nomeFamilia: string = "";
  convidados: string[] = [];
  submitted: boolean = false;
  value: string = "";
  message: string = "";
  limparConvidados() {
    this.convidados = [];
    console.log("Lista de convidados limpa");
  }
  adicionarNaListaConvidados(nickname:string) {
    if (nickname.trim() === "") {
      this.message = "O nickname não pode ser vazio.";
      console.log("Nickname vazio");
      return;
    }
    if (this.convidados.includes(nickname)) {
      this.message = "Este usuário já foi adicionado à lista de convidados.";
      console.log("Usuário já adicionado");
      return;
    }
    if (nickname.trim() !== "") {
      this.familiaService.verificarUsuarioExistente(nickname).subscribe(existe => {
      if (existe) {
          this.message = "Usuário não encontrado.";
      } else {
          this.convidados.push(nickname);
          this.message = "";
        }
      });
    this.value = "";
    console.log("Usuário adicionado:", nickname);
    }
  }
  removerConvidado(nickname:string) {
    this.convidados = this.convidados.filter(convidado => convidado !== nickname);
    console.log("Usuário removido:", nickname);
  }
  criarFamilia() {
    this.submitted = true;
    if (!this.nomeFamilia.trim()) return;
    
    // lógica de criar família...
  }
  resetNome(){
      this.nomeFamilia = "";
  }
}
