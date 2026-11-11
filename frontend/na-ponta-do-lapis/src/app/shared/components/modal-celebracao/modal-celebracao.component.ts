import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CelebracaoService, DadosCelebracao } from '../../service/celebracao.service';
import { MedalhaConquistaComponent } from '../medalha-conquista/medalha-conquista.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-modal-celebracao',
  standalone: true,
  imports: [CommonModule, MedalhaConquistaComponent],
  templateUrl: './modal-celebracao.component.html',
  styleUrl: './modal-celebracao.component.css'
})
export class ModalCelebracaoComponent implements OnInit, OnDestroy {
  public dados: DadosCelebracao | null = null;
  private sub!: Subscription;

  constructor(
    private celebracaoService: CelebracaoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.sub = this.celebracaoService.eventoCelebracao$.subscribe((dados) => {
      this.dados = dados;
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy(): void {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  public fechar(): void {
    this.celebracaoService.fecharModal();
  }
}

