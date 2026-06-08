import { Component } from '@angular/core';
import { Button } from "primeng/button";
import { FormsModule } from "@angular/forms";

@Component({
  selector: 'app-familia',
  imports: [Button, FormsModule],
  templateUrl: './familia.component.html',
  styleUrl: './familia.component.css',
})
export class FamiliaComponent {
  nomeFamilia: string = "";
  submitted = false;

  criarFamilia() {
    this.submitted = true;
    if (!this.nomeFamilia.trim()) return;
    
    // lógica de criar família...
  }
  resetNome(){
      this.nomeFamilia = "";
  }
}
