import {
  AfterViewInit,
  Component,
  DestroyRef,
  ElementRef,
  NgZone,
  effect,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';

export type MascoteEstado = 'idle' | 'checking' | 'hands-up' | 'peeking' | 'loading' | 'success' | 'error';
export type MascoteGracinha = 'none' | 'wink' | 'dance' | 'joy';

interface SpringState {
  value: number;
  velocity: number;
  target: number;
}

function stepSpring(s: SpringState, stiffness: number, damping: number, dt: number): number {
  const force = (s.target - s.value) * stiffness;
  s.velocity = (s.velocity + force * dt) * Math.exp(-damping * dt);
  s.value += s.velocity * dt;
  return s.value;
}

@Component({
  selector: 'app-mascote-login',
  standalone: true,
  templateUrl: './mascote-login.component.html',
  styleUrl: './mascote-login.component.css',
})
export class MascoteLoginComponent implements AfterViewInit {
  readonly estado = input<MascoteEstado>('idle');
  readonly lookProgress = input<number>(0.5);
  readonly compact = input<boolean>(false);
  readonly eager = input<boolean>(false);
  readonly emailInvalid = input<boolean>(false);

  readonly gracinha = signal<MascoteGracinha>('none');
  readonly clickBadge = signal<'heart' | 'coin'>('heart');
  readonly easterEgg = signal(false);
  readonly easterEggPhrase = signal('Pix caiu?!');
  readonly isBored = signal(false);

  private readonly ngZone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);

  private readonly svgRef = viewChild<ElementRef<SVGSVGElement>>('mascoteSvg');
  private readonly characterRef = viewChild<ElementRef<SVGGElement>>('characterGroup');
  private readonly faceRef = viewChild<ElementRef<SVGGElement>>('faceGroup');
  private readonly pupilsLeftRef = viewChild<ElementRef<SVGGElement>>('pupilLeft');
  private readonly pupilsRightRef = viewChild<ElementRef<SVGGElement>>('pupilRight');
  private readonly eyeLeftRef = viewChild<ElementRef<SVGGElement>>('eyeLeft');
  private readonly eyeRightRef = viewChild<ElementRef<SVGGElement>>('eyeRight');
  private readonly leftArmRef = viewChild<ElementRef<SVGGElement>>('leftArm');
  private readonly rightArmRef = viewChild<ElementRef<SVGGElement>>('rightArm');
  private readonly leftHandRef = viewChild<ElementRef<SVGGElement>>('leftHand');
  private readonly rightHandRef = viewChild<ElementRef<SVGGElement>>('rightHand');
  private readonly eraserRef = viewChild<ElementRef<SVGGElement>>('eraserGroup');
  private readonly browLeftRef = viewChild<ElementRef<SVGPathElement>>('browLeft');
  private readonly browRightRef = viewChild<ElementRef<SVGPathElement>>('browRight');
  private readonly mouthRef = viewChild<ElementRef<SVGPathElement>>('mouthPath');
  private readonly shadowRef = viewChild<ElementRef<SVGEllipseElement>>('groundShadow');

  private rafId: number | null = null;
  private startTime = 0;
  private lastTime = 0;
  private nextBlinkAt = 2.4;
  private blinkProgress = 0;
  private isBlinking = false;

  // Rastreamento do cursor do mouse
  private cursorX = 0;
  private cursorY = 0;
  private hasCursorMoved = false;

  // Espiadinha rápida na senha (enquanto está em hands-up)
  private handsUpSince = -1;
  private nextSneakPeekAt = 3.2;
  private sneakPeekUntil = -1;

  // Gracinha ao clicar e easter egg de 5 cliques
  private gracinhaTimer: ReturnType<typeof setTimeout> | null = null;
  private easterEggTimer: ReturnType<typeof setTimeout> | null = null;
  private clickCount = 0;
  private clickStreak = 0;
  private easterEggIndex = 0;
  private lastClickAt = 0;

  private readonly easterEggPhrases = [
    'E o Pix, nada ainda?',
    'Se não comprar, o desconto é maior!',
    'O boleto não perdoa!',
    'Só no cartão, meu nobre!',
    'Cadê o 13º, patrão?',
  ];

  // Tédio após tempo sem interação
  private lastActivityAt = 0;
  private currentElapsed = 0;

  private readonly lookX: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly lookY: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly leftArmUp: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly rightArmUp: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly rightArmDrop: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly headTilt: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly bounceY: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly shakeX: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly browLift: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly browSad: SpringState = { value: 0, velocity: 0, target: 0 };
  private readonly mouthBaseY: SpringState = { value: 108, velocity: 0, target: 108 };
  private readonly mouthCurve: SpringState = { value: 7, velocity: 0, target: 7 };
  private readonly mouthWidth: SpringState = { value: 8, velocity: 0, target: 8 };
  private readonly leanScale: SpringState = { value: 1, velocity: 0, target: 1 };

  constructor() {
    effect(() => {
      const state = this.estado();
      const progress = Math.max(0, Math.min(1, this.lookProgress()));
      const isEager = this.eager();

      this.lastActivityAt = this.currentElapsed;
      if (state !== 'idle' || isEager) {
        this.isBored.set(false);
      }

      if (state !== 'hands-up') {
        this.handsUpSince = -1;
        this.sneakPeekUntil = -1;
      }

      this.leanScale.target =
        isEager && (state === 'idle' || state === 'checking') ? 1.04 : 1;

      if (state === 'checking') {
        const dir = (progress - 0.5) * 2;
        this.lookX.target = dir;
        this.lookY.target = 0.65;
        this.headTilt.target = dir * 6;
        this.leftArmUp.target = 0;
        this.rightArmUp.target = 0;
        this.rightArmDrop.target = 0;
        this.bounceY.target = 1.5;
        this.browLift.target = 0.8;
        this.browSad.target = 0;
        this.mouthBaseY.target = 109;
        this.mouthCurve.target = 3.5;
        this.mouthWidth.target = 5;
      } else if (state === 'hands-up') {
        this.lookX.target = 0;
        this.lookY.target = -0.15;
        this.headTilt.target = 0;
        this.leftArmUp.target = 1;
        this.rightArmUp.target = 1;
        this.rightArmDrop.target = 0;
        this.bounceY.target = 2;
        this.browLift.target = 0.2;
        this.browSad.target = 0;
        this.mouthBaseY.target = 108;
        this.mouthCurve.target = 6;
        this.mouthWidth.target = 7;
      } else if (state === 'peeking') {
        this.lookX.target = 0.35;
        this.lookY.target = 0.55;
        this.headTilt.target = 3.5;
        this.leftArmUp.target = 1;
        this.rightArmUp.target = 0.80;
        this.rightArmDrop.target = 21;
        this.bounceY.target = 1;
        this.browLift.target = 0.9;
        this.browSad.target = 0;
        this.mouthBaseY.target = 108;
        this.mouthCurve.target = 5;
        this.mouthWidth.target = 6.5;
      } else if (state === 'loading') {
        this.lookX.target = 0;
        this.lookY.target = -0.55;
        this.headTilt.target = 3;
        this.leftArmUp.target = 0.08;
        this.rightArmUp.target = 0.08;
        this.rightArmDrop.target = 0;
        this.bounceY.target = 0;
        this.browLift.target = 0.5;
        this.browSad.target = 0;
        this.mouthBaseY.target = 109;
        this.mouthCurve.target = 2.5;
        this.mouthWidth.target = 5;
      } else if (state === 'success') {
        this.lookX.target = 0;
        this.lookY.target = -0.3;
        this.headTilt.target = 0;
        this.leftArmUp.target = 0;
        this.rightArmUp.target = 0;
        this.rightArmDrop.target = 0;
        this.bounceY.target = -6;
        this.bounceY.velocity = -40;
        this.browLift.target = 1.1;
        this.browSad.target = 0;
        this.mouthBaseY.target = 106;
        this.mouthCurve.target = 14;
        this.mouthWidth.target = 11;
      } else if (state === 'error') {
        this.lookX.target = 0;
        this.lookY.target = 0.6;
        this.headTilt.target = -7;
        this.leftArmUp.target = -0.05;
        this.rightArmUp.target = -0.05;
        this.rightArmDrop.target = 0;
        this.bounceY.target = 5.5;
        this.shakeX.velocity = 95;
        this.browLift.target = 0.2;
        this.browSad.target = 1;
        this.mouthBaseY.target = 114;
        this.mouthCurve.target = -9;
        this.mouthWidth.target = 8;
      } else {
        this.leftArmUp.target = 0;
        this.rightArmUp.target = 0;
        this.rightArmDrop.target = 0;
        this.bounceY.target = isEager ? -2 : 0;
        this.browLift.target = isEager ? 0.85 : 0;
        this.browSad.target = 0;
        this.mouthBaseY.target = isEager ? 107 : 108;
        this.mouthCurve.target = isEager ? 12 : 7;
        this.mouthWidth.target = isEager ? 10 : 8;
        if (isEager) {
          this.lookX.target = 0;
          this.lookY.target = 0.55;
          this.headTilt.target = 0;
        } else if (!this.hasCursorMoved) {
          this.lookX.target = 0;
          this.lookY.target = 0;
          this.headTilt.target = 0;
        }
      }
    });
  }

  public onMascotClick(): void {
    const now = performance.now();
    this.lastActivityAt = this.currentElapsed;
    this.isBored.set(false);

    if (now - this.lastClickAt < 1800) {
      this.clickStreak++;
    } else {
      this.clickStreak = 1;
    }
    this.lastClickAt = now;

    if (this.clickStreak >= 5) {
      this.clickStreak = 0;
      this.easterEggPhrase.set(this.easterEggPhrases[this.easterEggIndex % this.easterEggPhrases.length]);
      this.easterEggIndex++;
      this.easterEgg.set(true);
      this.gracinha.set('dance');
      this.bounceY.velocity = -75;
      this.shakeX.velocity = 140;

      if (this.easterEggTimer) {
        clearTimeout(this.easterEggTimer);
      }
      this.easterEggTimer = setTimeout(() => {
        this.easterEgg.set(false);
      }, 2400);
    } else {
      const options: MascoteGracinha[] = ['wink', 'dance', 'joy'];
      const chosen = options[this.clickCount % options.length];
      this.clickBadge.set(this.clickCount % 2 === 0 ? 'heart' : 'coin');
      this.clickCount++;

      this.gracinha.set(chosen);
      this.bounceY.velocity = -55;

      if (chosen === 'dance') {
        this.shakeX.velocity = 110;
        this.headTilt.velocity = 85;
      } else if (chosen === 'wink') {
        this.headTilt.velocity = -60;
      }
    }

    if (this.gracinhaTimer) {
      clearTimeout(this.gracinhaTimer);
    }
    this.gracinhaTimer = setTimeout(() => {
      this.gracinha.set('none');
    }, 1050);
  }

  ngAfterViewInit(): void {
    const prefersReduced =
      typeof window !== 'undefined' &&
      window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;

    if (prefersReduced) {
      return;
    }

    this.ngZone.runOutsideAngular(() => {
      const onMouseMove = (event: MouseEvent) => {
        const svgEl = this.svgRef()?.nativeElement;
        if (!svgEl) return;

        const rect = svgEl.getBoundingClientRect();
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height * 0.45;

        this.cursorX = Math.max(-1, Math.min(1, (event.clientX - centerX) / 260));
        this.cursorY = Math.max(-1, Math.min(1, (event.clientY - centerY) / 220));
        this.hasCursorMoved = true;
        this.lastActivityAt = this.currentElapsed;
        if (this.isBored()) {
          this.isBored.set(false);
        }
      };

      window.addEventListener('mousemove', onMouseMove, { passive: true });

      const tick = (now: number) => {
        if (!this.startTime) {
          this.startTime = now;
          this.lastTime = now;
        }
        const dt = Math.min(0.05, (now - this.lastTime) / 1000);
        const elapsed = (now - this.startTime) / 1000;
        this.lastTime = now;

        this.updateAnimation(elapsed, dt);
        this.rafId = requestAnimationFrame(tick);
      };
      this.rafId = requestAnimationFrame(tick);

      this.destroyRef.onDestroy(() => {
        window.removeEventListener('mousemove', onMouseMove);
        if (this.rafId !== null) {
          cancelAnimationFrame(this.rafId);
        }
        if (this.gracinhaTimer) {
          clearTimeout(this.gracinhaTimer);
        }
        if (this.easterEggTimer) {
          clearTimeout(this.easterEggTimer);
        }
      });
    });
  }

  private updateAnimation(elapsed: number, dt: number): void {
    const currentState = this.estado();
    this.currentElapsed = elapsed;

    // Tédio após 12s sem interação em idle
    const idleDuration = elapsed - this.lastActivityAt;
    if (currentState === 'idle' && !this.eager() && idleDuration > 12) {
      if (!this.isBored()) {
        this.isBored.set(true);
      }
      this.lookX.target = Math.sin(elapsed * 0.9) * 0.55;
      this.lookY.target = 0.25;
      this.headTilt.target = Math.sin(elapsed * 0.9) * 4;
      this.mouthCurve.target = 1.5;
      this.mouthWidth.target = 6;
    } else if (currentState === 'idle' && this.hasCursorMoved && !this.eager()) {
      // Em idle (sem hover no botão Entrar), os olhos e a cabeça seguem suavemente o cursor do mouse
      this.lookX.target = this.cursorX;
      this.lookY.target = this.cursorY;
      this.headTilt.target = this.cursorX * 5.5;
    } else if (currentState === 'loading') {
      this.lookX.target = Math.sin(elapsed * 2.6) * 0.5;
      this.headTilt.target = Math.sin(elapsed * 2.6) * 3.5;
    }

    // Espiadinha rápida e bem sutil na senha (acontece logo após focar na senha)
    if (currentState === 'hands-up') {
      if (this.handsUpSince < 0) {
        this.handsUpSince = elapsed;
        this.nextSneakPeekAt = elapsed + 0.9 + Math.random() * 0.5;
      }

      if (this.sneakPeekUntil > elapsed) {
        this.rightArmUp.target = 0.85;
        this.rightArmDrop.target = 15;
        this.lookX.target = 0.35;
        this.lookY.target = 0.48;
        this.headTilt.target = 3.2;
      } else {
        this.rightArmUp.target = 1;
        this.rightArmDrop.target = 0;
        this.lookX.target = 0;
        this.lookY.target = -0.15;
        this.headTilt.target = 0;

        if (elapsed >= this.nextSneakPeekAt) {
          this.sneakPeekUntil = elapsed + 0.85;
          this.nextSneakPeekAt = elapsed + 4.0 + Math.random() * 3.0;
        }
      }
    }

    const breathY = Math.sin(elapsed * 2.1) * 2.0;
    const idleSway = currentState === 'idle' ? Math.sin(elapsed * 1.15) * 1.4 : 0;
    const danceWiggle = this.gracinha() === 'dance' ? Math.sin(elapsed * 18) * 6 : 0;
    const eraserSquish = 1 + Math.sin(elapsed * 2.1 - 0.6) * 0.022;

    // Piscada natural
    if (!this.isBlinking && elapsed >= this.nextBlinkAt) {
      this.isBlinking = true;
      this.blinkProgress = 0;
    }
    let eyeScaleY = 1;
    if (this.isBlinking) {
      this.blinkProgress += dt / 0.16;
      if (this.blinkProgress >= 1) {
        this.isBlinking = false;
        this.blinkProgress = 0;
        this.nextBlinkAt = elapsed + 2.6 + Math.random() * 2.4;
      } else {
        const tri = this.blinkProgress < 0.5 ? this.blinkProgress * 2 : (1 - this.blinkProgress) * 2;
        eyeScaleY = Math.max(0.08, 1 - tri * 0.92);
      }
    }

    const lx = stepSpring(this.lookX, 150, 15, dt);
    const ly = stepSpring(this.lookY, 150, 15, dt);
    const lArm = stepSpring(this.leftArmUp, 115, 11.5, dt);
    const rArm = stepSpring(this.rightArmUp, 102, 11, dt);
    const rDrop = stepSpring(this.rightArmDrop, 125, 12, dt);
    const tilt = stepSpring(this.headTilt, 115, 13, dt);
    const bY = stepSpring(this.bounceY, 160, 11.5, dt);
    const sX = stepSpring(this.shakeX, 220, 10, dt);
    const bLift = stepSpring(this.browLift, 140, 14, dt);
    const bSad = stepSpring(this.browSad, 140, 14, dt);
    const mY = stepSpring(this.mouthBaseY, 150, 14, dt);
    const mCurve = stepSpring(this.mouthCurve, 150, 14, dt);
    const mWidth = stepSpring(this.mouthWidth, 150, 14, dt);
    const scaleL = stepSpring(this.leanScale, 140, 13, dt);

    const armIdleSway =
      this.easterEgg()
        ? Math.sin(elapsed * 24) * 28
        : currentState === 'loading'
          ? Math.sin(elapsed * 7.5) * 6.5
          : Math.sin(elapsed * 2.1 - 0.6) * 3.2;

    const charEl = this.characterRef()?.nativeElement;
    if (charEl) {
      const totalY = breathY + bY;
      const totalRot = tilt + idleSway + danceWiggle;
      charEl.setAttribute(
        'transform',
        `translate(${sX.toFixed(2)}, ${totalY.toFixed(2)}) translate(100, 110) scale(${scaleL.toFixed(3)}) translate(-100, -110) rotate(${totalRot.toFixed(2)} 100 110)`
      );
    }

    const shadowEl = this.shadowRef()?.nativeElement;
    if (shadowEl) {
      const lift = Math.max(-10, Math.min(6, breathY + bY));
      const shadowScale = 1 + lift * 0.018;
      const shadowOpacity = Math.max(0.06, Math.min(0.2, 0.14 + lift * 0.008));
      shadowEl.setAttribute(
        'transform',
        `translate(100, 185) scale(${shadowScale.toFixed(3)}, 1) translate(-100, -185)`
      );
      shadowEl.setAttribute('opacity', shadowOpacity.toFixed(3));
    }

    const eraserEl = this.eraserRef()?.nativeElement;
    if (eraserEl) {
      eraserEl.setAttribute(
        'transform',
        `translate(0, ${(breathY * 0.3).toFixed(2)}) scale(1, ${eraserSquish.toFixed(3)})`
      );
    }

    const faceEl = this.faceRef()?.nativeElement;
    if (faceEl) {
      faceEl.setAttribute(
        'transform',
        `translate(${(lx * 4.2).toFixed(2)}, ${(ly * 2.4).toFixed(2)})`
      );
    }

    const px = (lx * 4.8).toFixed(2);
    const py = (ly * 3.4).toFixed(2);
    this.pupilsLeftRef()?.nativeElement.setAttribute('transform', `translate(${px}, ${py})`);
    this.pupilsRightRef()?.nativeElement.setAttribute('transform', `translate(${px}, ${py})`);

    this.eyeLeftRef()?.nativeElement.setAttribute(
      'transform',
      `translate(81, 88) scale(1, ${eyeScaleY.toFixed(3)}) translate(-81, -88)`
    );
    this.eyeRightRef()?.nativeElement.setAttribute(
      'transform',
      `translate(119, 88) scale(1, ${eyeScaleY.toFixed(3)}) translate(-119, -88)`
    );

    const lArmEl = this.leftArmRef()?.nativeElement;
    if (lArmEl) {
      const angle = lArm * 193 + (1 - Math.abs(lArm)) * armIdleSway;
      lArmEl.setAttribute('transform', `rotate(${angle.toFixed(2)} 60 112)`);
    }

    const lHandEl = this.leftHandRef()?.nativeElement;
    if (lHandEl) {
      const wristFlex = Math.max(-22, Math.min(22, -this.leftArmUp.velocity * 4.5));
      lHandEl.setAttribute('transform', `rotate(${wristFlex.toFixed(2)} 44 136)`);
    }

    const rArmEl = this.rightArmRef()?.nativeElement;
    if (rArmEl) {
      const angle = -rArm * 193 - (1 - Math.abs(rArm)) * armIdleSway;
      rArmEl.setAttribute(
        'transform',
        `translate(${(rDrop * 0.25).toFixed(2)}, ${rDrop.toFixed(2)}) rotate(${angle.toFixed(2)} 140 112)`
      );
    }

    const rHandEl = this.rightHandRef()?.nativeElement;
    if (rHandEl) {
      const wristFlex = Math.max(-22, Math.min(22, this.rightArmUp.velocity * 4.5));
      rHandEl.setAttribute('transform', `rotate(${wristFlex.toFixed(2)} 156 136)`);
    }

    // Morph contínuo das sobrancelhas
    const yStart = (72 - bLift * 3 + bSad * 4).toFixed(2);
    const yCtrl = (69 - bLift * 5 - bSad * 2).toFixed(2);
    const yEnd = (72 - bLift * 3 - bSad * 6).toFixed(2);
    this.browLeftRef()?.nativeElement.setAttribute('d', `M71 ${yStart} Q80 ${yCtrl} 88 ${yEnd}`);
    this.browRightRef()?.nativeElement.setAttribute('d', `M129 ${yStart} Q120 ${yCtrl} 112 ${yEnd}`);

    // Morph contínuo da boca
    const isGrin = this.gracinha() !== 'none' || currentState === 'success';
    const targetCurve = isGrin ? Math.max(mCurve, 13) : mCurve;
    const targetWidth = isGrin ? Math.max(mWidth, 10.5) : mWidth;
    const x1 = (100 - targetWidth).toFixed(2);
    const x2 = (100 + targetWidth).toFixed(2);
    const yStr = mY.toFixed(2);
    const yCtrlM = (mY + targetCurve).toFixed(2);
    this.mouthRef()?.nativeElement.setAttribute('d', `M${x1} ${yStr} Q100 ${yCtrlM} ${x2} ${yStr}`);
  }
}
