import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import confetti from 'canvas-confetti';

export interface DadosCelebracao {
  tipo: 'CONQUISTA' | 'META_ATINGIDA';
  titulo: string;
  subtitulo: string;
  pontosXp?: number;
  idConquista?: string;
  metaNome?: string;
  metaValor?: number;
}

@Injectable({
  providedIn: 'root'
})
export class CelebracaoService {

  private eventoCelebracaoSubject = new BehaviorSubject<DadosCelebracao | null>(null);
  public eventoCelebracao$ = this.eventoCelebracaoSubject.asObservable();

  public celebrarConquista(titulo: string, descricao: string, pontosXp: number, idConquista: string): void {
    this.dispararEfeitoConfetes();
    this.tocarSomVitoria();

    this.eventoCelebracaoSubject.next({
      tipo: 'CONQUISTA',
      titulo: 'Nova Conquista Desbloqueada!',
      subtitulo: `${titulo} (+${pontosXp} XP)`,
      pontosXp,
      idConquista
    });
  }

  public celebrarMetaAtingida(nomeMeta: string, valor: number): void {
    this.dispararEfeitoConfetes();
    this.tocarSomVitoria();

    this.eventoCelebracaoSubject.next({
      tipo: 'META_ATINGIDA',
      titulo: 'Meta Concluída!',
      subtitulo: `Parabéns! Você alcançou o objetivo na meta "${nomeMeta}".`,
      metaNome: nomeMeta,
      metaValor: valor
    });
  }

  public fecharModal(): void {
    this.eventoCelebracaoSubject.next(null);
  }

  public dispararEfeitoConfetes(): void {
    try {
      const cores = ['#EEC046', '#7843CD', '#10B981', '#FDE047', '#FFFFFF', '#C084FC'];
      const zIndexConfetes = 999999; // Garante que voe na frente de qualquer modal, overlay ou toast

      // 1. Canhão Central Inicial de Alta Velocidade
      confetti({
        particleCount: 100,
        spread: 100,
        origin: { y: 0.6 },
        colors: cores,
        zIndex: zIndexConfetes,
        disableForReducedMotion: true
      });

      // 2. Canhões Laterais em rajadas cruzadas
      setTimeout(() => {
        confetti({
          particleCount: 60,
          angle: 60,
          spread: 75,
          origin: { x: 0.05, y: 0.7 },
          colors: cores,
          zIndex: zIndexConfetes
        });
        confetti({
          particleCount: 60,
          angle: 120,
          spread: 75,
          origin: { x: 0.95, y: 0.7 },
          colors: cores,
          zIndex: zIndexConfetes
        });
      }, 250);

      // 3. Chuva Dourada Descendente do Topo
      setTimeout(() => {
        confetti({
          particleCount: 80,
          spread: 140,
          origin: { y: 0.3 },
          colors: cores,
          zIndex: zIndexConfetes
        });
      }, 550);
    } catch (e) {
      console.warn('Efeito de confetes não suportado ou bloqueado no ambiente:', e);
    }
  }

  private tocarSomVitoria(): void {
    try {
      const AudioContextClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (!AudioContextClass) return;

      const ctx = new AudioContextClass();
      if (ctx.state === 'suspended') {
        ctx.resume();
      }

      // Arpejo de vitória triunfal: Dó5, Mi5, Sol5, Dó6
      const notas = [523.25, 659.25, 783.99, 1046.50];
      const agora = ctx.currentTime;

      notas.forEach((freq, index) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();

        osc.type = 'triangle';
        osc.frequency.setValueAtTime(freq, agora + index * 0.12);

        gain.gain.setValueAtTime(0.001, agora + index * 0.12);
        gain.gain.exponentialRampToValueAtTime(0.18, agora + index * 0.12 + 0.04);
        gain.gain.exponentialRampToValueAtTime(0.0001, agora + index * 0.12 + 0.38);

        osc.connect(gain);
        gain.connect(ctx.destination);

        osc.start(agora + index * 0.12);
        osc.stop(agora + index * 0.12 + 0.40);
      });
    } catch {
      // Falha silenciosa para navegadores sem áudio ou autoplay restrito
    }
  }
}

