import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastModule } from 'primeng/toast';
import { ModalCelebracaoComponent } from './shared/components/modal-celebracao/modal-celebracao.component';
import { PwaService } from './shared/service/pwa.service';
import { CapacitorService } from './shared/service/capacitor.service';
import { ConexaoService } from './shared/service/conexao.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastModule, ModalCelebracaoComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('na-ponta-do-lapis');
  protected readonly pwaService = inject(PwaService);
  protected readonly capacitorService = inject(CapacitorService);
  protected readonly conexaoService = inject(ConexaoService);
}
