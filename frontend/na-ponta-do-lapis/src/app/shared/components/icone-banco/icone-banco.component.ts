import { Component, Input, OnChanges, OnInit, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { mapearNomeBanco, obterSvgBancoString } from '../../utils/banco-icon.helper';

@Component({
  selector: 'app-icone-banco',
  standalone: true,
  imports: [CommonModule],
  styles: [`
    :host {
      display: inline-flex;
      vertical-align: middle;
    }
  `],
  template: `
    <div class="inline-flex items-center justify-center shrink-0"
         [style.width.px]="tamanho"
         [style.height.px]="tamanho">
      @if (svgSafeHtml) {
        <div class="w-full h-full flex items-center justify-center overflow-hidden rounded-xl shadow-xs [&>svg]:w-full [&>svg]:h-full [&>svg]:block"
             [innerHTML]="svgSafeHtml">
        </div>
      } @else if (fallbackLogoUrl) {
        <img [src]="fallbackLogoUrl"
             [alt]="nome || 'Banco'"
             class="w-full h-full object-contain rounded-xl bg-white p-1 border border-slate-200 shadow-xs" />
      } @else {
        <div class="w-full h-full rounded-xl bg-slate-100 text-slate-700 flex items-center justify-center border border-slate-200/80 shadow-xs"
             [style.background]="fundo"
             [style.color]="cor">
          <i [class]="fallbackIcon + ' text-xl'"></i>
        </div>
      }
    </div>
  `
})
export class IconeBancoComponent implements OnInit, OnChanges {
  @Input() nome?: string | null;
  @Input() formato: 'quadrado' | 'circulo' | 'sem' = 'quadrado';
  @Input() tamanho: number = 44;
  @Input() cor?: string;
  @Input() fundo?: string;
  @Input() fallbackLogoUrl?: string | null;
  @Input() fallbackIcon: string = 'pi pi-wallet';

  public svgSafeHtml: SafeHtml | null = null;

  constructor(
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.atualizarIcone();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['nome'] || changes['formato'] || changes['tamanho'] || changes['cor'] || changes['fundo'] || changes['fallbackLogoUrl']) {
      this.atualizarIcone();
    }
  }

  private async atualizarIcone(): Promise<void> {
    const resolvido = mapearNomeBanco(this.nome);
    if (resolvido.chave) {
      const svgStr = await obterSvgBancoString({
        nome: resolvido.chave,
        formato: this.formato,
        tamanho: this.tamanho,
        cor: this.cor,
        fundo: this.fundo
      });

      if (svgStr) {
        this.svgSafeHtml = this.sanitizer.bypassSecurityTrustHtml(svgStr);
        this.cdr.markForCheck();
        return;
      }
    }

    this.svgSafeHtml = null;
    this.cdr.markForCheck();
  }
}

