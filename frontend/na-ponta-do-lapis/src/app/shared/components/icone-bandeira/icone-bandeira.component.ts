import { Component, Input, OnChanges, OnInit, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { svgPaisAsync } from '@edusites/bandeiras-paises/core';

@Component({
  selector: 'app-icone-bandeira',
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
        <div class="w-full h-full flex items-center justify-center overflow-hidden rounded-full shadow-2xs border border-slate-200/60 [&>svg]:w-full [&>svg]:h-full [&>svg]:block [&>svg]:object-cover"
             [innerHTML]="svgSafeHtml">
        </div>
      } @else {
        <div class="w-full h-full rounded-full bg-slate-100 text-slate-500 font-bold text-[10px] flex items-center justify-center border border-slate-200">
          {{ codigoPais }}
        </div>
      }
    </div>
  `
})
export class IconeBandeiraComponent implements OnInit, OnChanges {
  @Input() moedaOuPais: string = 'BRL';
  @Input() tamanho: number = 32;

  public svgSafeHtml: SafeHtml | null = null;
  public codigoPais: string = 'BR';

  constructor(
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregarBandeira();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['moedaOuPais'] || changes['tamanho']) {
      this.carregarBandeira();
    }
  }

  private mapearParaIsoPais(moeda: string): string {
    const cod = (moeda || '').trim().toUpperCase();
    switch (cod) {
      case 'BRL':
      case 'BR':
        return 'BR';
      case 'USD':
      case 'US':
        return 'US';
      case 'EUR':
      case 'EU':
      case 'DE':
        return 'DE'; // Alemanha (maior economia da Zona do Euro)
      case 'PT':
        return 'PT';
      case 'GB':
      case 'GBP':
        return 'GB';
      default:
        return cod.length === 2 ? cod : 'BR';
    }
  }

  private carregarBandeira(): void {
    this.codigoPais = this.mapearParaIsoPais(this.moedaOuPais);
    svgPaisAsync({ nome: this.codigoPais, tamanho: this.tamanho, redonda: true })
      .then((svgString: string | null) => {
        if (svgString) {
          this.svgSafeHtml = this.sanitizer.bypassSecurityTrustHtml(svgString);
        } else {
          this.svgSafeHtml = null;
        }
        this.cdr.detectChanges();
      })
      .catch(() => {
        this.svgSafeHtml = null;
        this.cdr.detectChanges();
      });
  }
}

