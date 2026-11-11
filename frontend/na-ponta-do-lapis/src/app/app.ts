import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PrimeNGModuleModule } from "./shared/primeNg.module";
import { ModalCelebracaoComponent } from './shared/components/modal-celebracao/modal-celebracao.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PrimeNGModuleModule, ModalCelebracaoComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('na-ponta-do-lapis');
}
